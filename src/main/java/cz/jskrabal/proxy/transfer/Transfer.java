package cz.jskrabal.proxy.transfer;

import java.nio.channels.UnresolvedAddressException;
import java.util.Map;
import java.util.Set;

import cz.jskrabal.proxy.config.ProxyConfiguration;
import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import cz.jskrabal.proxy.config.enums.IdGeneratorType;
import cz.jskrabal.proxy.util.ProxyUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.SocketAddress;

/**
 * Created by janskrabal on 01/06/16.
 */
public abstract class Transfer {
	protected final String id;
	protected final Vertx vertx;
	protected final HttpServerRequest upstreamRequest;
	protected final ProxyConfiguration configuration;


	protected Transfer(Vertx vertx, ProxyConfiguration configuration, HttpServerRequest upstreamRequest) {
		this.vertx = vertx;
		this.configuration = configuration;
		this.upstreamRequest = upstreamRequest;

		String idGenerator = configuration.getValue(ConfigurationParameter.ID_GENERATOR, String.class);
		id = IdGeneratorType.valueOf(idGenerator).generateId();
	}

	public abstract void start();

	protected HttpServerResponse configureServerResponseByClientResponse(HttpServerResponse serverResponse,
			HttpClientResponse clientResponse) {
		serverResponse
                .setStatusCode(clientResponse.statusCode())
                .setStatusMessage(clientResponse.statusMessage())
				.setChunked(ProxyUtils.isChunked(clientResponse));

		MultiMap headers = serverResponse.headers()
                    .setAll(clientResponse.headers())
					.addAll(getCustomResponseHeaders());

		getBlockedResponseHeaders().stream().forEach(headers::remove);

		return serverResponse;
	}

	protected HttpClientRequest configureClientRequestByServerRequest(HttpClientRequest clientRequest,
			HttpServerRequest serverRequest) {
		clientRequest.setChunked(ProxyUtils.isChunked(serverRequest));

		MultiMap headers = clientRequest.headers()
                    .setAll(serverRequest.headers())
					.addAll(getCustomRequestHeaders());

		getBlockedRequestHeaders().stream().forEach(headers::remove);

		return clientRequest;
	}

	protected void respondConnectionFailed(Throwable throwable) {
		String errorMessage = getErrorMessage(throwable);

		HttpResponseStatus status;
		if(throwable instanceof UnresolvedAddressException) {
			status = HttpResponseStatus.NOT_FOUND;
		} else {
			status =  HttpResponseStatus.BAD_GATEWAY;
		}

		upstreamRequest.response()
				.setStatusCode(status.code())
				.setStatusMessage(errorMessage)
				.headers()
					.addAll(getCustomResponseHeaders());

		upstreamRequest.response().end();
	}

	private Map<String, String> getCustomResponseHeaders(){
		@SuppressWarnings("unchecked")
		Map<String, String> customHeaders = configuration.getValue(ConfigurationParameter.CUSTOM_RESPONSE_HEADERS,
				Map.class);
		addResponseDynamicHeaders(customHeaders);

		return customHeaders;
	}

	private Map<String, String> getCustomRequestHeaders(){
		@SuppressWarnings("unchecked")
		Map<String, String> customHeaders = configuration.getValue(ConfigurationParameter.CUSTOM_REQUEST_HEADERS,
				Map.class);
		addRequestDynamicHeaders(customHeaders);

		return customHeaders;
	}

	private Set<String> getBlockedRequestHeaders(){
		@SuppressWarnings("unchecked")
		Set<String> blockedHeaders = (Set<String>) configuration.getValue(
				ConfigurationParameter.REMOVE_REQUEST_HEADERS, Set.class);

		return blockedHeaders;
	}

	private Set<String> getBlockedResponseHeaders(){
		@SuppressWarnings("unchecked")
		Set<String> blockedHeaders = (Set<String>) configuration.getValue(
				ConfigurationParameter.REMOVE_RESPONSE_HEADERS, Set.class);

		return blockedHeaders;
	}

	private void addCommonDynamicHeaders(Map<String, String> headers) {
		if (configuration.getValue(ConfigurationParameter.ADD_TRANSFER_ID_HEADER, Boolean.class)) {
			headers.put("X-Transfer-Id", id);
		}

		if (configuration.getValue(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS, Boolean.class)) {
			SocketAddress localAddress = upstreamRequest.localAddress();
			headers.put("X-Forwarded-By-Ip", String.valueOf(localAddress.host()));
			headers.put("X-Forwarded-By-Port", String.valueOf(localAddress.port()));
		}
	}

	private void addRequestDynamicHeaders(Map<String, String> headers) {
		addCommonDynamicHeaders(headers);

		if (configuration.getValue(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS, Boolean.class)) {
			SocketAddress remoteAddress = upstreamRequest.remoteAddress();
			headers.put("X-Forwarded-For-Ip", String.valueOf(remoteAddress.host()));
			headers.put("X-Forwarded-For-Port", String.valueOf(remoteAddress.port()));
		}
	}

	private void addResponseDynamicHeaders(Map<String, String> headers) {
		addCommonDynamicHeaders(headers);
	}

	//FIXME possible security issue - revealing implementation details to the proxy client.
	private String getErrorMessage(Throwable throwable) {
		String errorMessage = "Connection to remote server has failed due to ";
		if(throwable == null) {
			errorMessage += "unknown error";
		} else {
			String throwableMessage = throwable.getMessage();
			if(throwableMessage == null) {
				errorMessage += "'" + throwable.getClass() + "' with no message";
			} else {
				errorMessage += "nested exception " + throwableMessage;
			}
		}
		return errorMessage;
	}
}
