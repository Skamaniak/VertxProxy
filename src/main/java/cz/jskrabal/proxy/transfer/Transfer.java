package cz.jskrabal.proxy.transfer;

import cz.jskrabal.proxy.config.ProxyConfig;
import cz.jskrabal.proxy.util.ProxyUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.SocketAddress;

import java.nio.channels.UnresolvedAddressException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Created by janskrabal on 01/06/16.
 */
public abstract class Transfer {
	protected final String id;
	protected final Vertx vertx;
	protected final HttpServerRequest upstreamRequest;
	protected final HttpClient client;
	protected final ProxyConfig configuration;

	protected Transfer(Vertx vertx, HttpClient client, ProxyConfig configuration,
			HttpServerRequest upstreamRequest) {
		this.vertx = vertx;
		this.configuration = configuration;
		this.upstreamRequest = upstreamRequest;
		this.client = client;

		id = configuration.getIdGenerator().generateId();
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

		getBlockedResponseHeaders().forEach(headers::remove);

		return serverResponse;
	}

	protected HttpClientRequest configureClientRequestByServerRequest(HttpClientRequest clientRequest,
			HttpServerRequest serverRequest) {
		clientRequest.setChunked(ProxyUtils.isChunked(serverRequest));

		MultiMap headers = clientRequest.headers()
				.setAll(serverRequest.headers())
				.addAll(getCustomRequestHeaders());

		getBlockedRequestHeaders().forEach(headers::remove);

		return clientRequest;
	}

	protected HttpClientRequest addRequestTimeout(HttpClientRequest request) {
		long requestTimeout = configuration.getStream().getDownstream().getHttpRequestTimeoutMillis();
		if (requestTimeout > 0) {
			request.setTimeout(requestTimeout);
		}
		return request;
	}

	protected void respondConnectionFailed(Throwable throwable) {
		if (!upstreamRequest.response().headWritten()) {
			String errorMessage = getErrorMessage(throwable);

			HttpResponseStatus status = exceptionToHttpStatus(throwable);
			upstreamRequest.response()
					.setStatusCode(status.code())
					.setStatusMessage(errorMessage)
					.headers()
					.addAll(getCustomResponseHeaders());

			upstreamRequest.response().end();
		}
	}

	private static HttpResponseStatus exceptionToHttpStatus(Throwable throwable) {
		HttpResponseStatus status;
		if (throwable instanceof UnresolvedAddressException) {
			status = HttpResponseStatus.NOT_FOUND;
		} else if (throwable instanceof TimeoutException) {
			status = HttpResponseStatus.GATEWAY_TIMEOUT;
		} else {
			status = HttpResponseStatus.BAD_GATEWAY;
		}
		return status;
	}

	private Map<String, String> getCustomResponseHeaders() {
		@SuppressWarnings("unchecked")
		Map<String, String> customHeaders = configuration.getCustomHeaders().getAppendToResponse();
		addResponseDynamicHeaders(customHeaders);

		return customHeaders;
	}

	private Map<String, String> getCustomRequestHeaders() {
		@SuppressWarnings("unchecked")
		Map<String, String> customHeaders = configuration.getCustomHeaders().getAppendToRequest();
		addRequestDynamicHeaders(customHeaders);

		return customHeaders;
	}

	private Set<String> getBlockedRequestHeaders() {
		@SuppressWarnings("unchecked")
		Set<String> blockedHeaders = new HashSet<>(configuration.getCustomHeaders().getRemoveFromRequest());

		return blockedHeaders;
	}

	private Set<String> getBlockedResponseHeaders() {
		@SuppressWarnings("unchecked")
		Set<String> blockedHeaders = new HashSet<>(configuration.getCustomHeaders().getRemoveFromResponse());

		return blockedHeaders;
	}

	private void addCommonDynamicHeaders(Map<String, String> headers) {
		if (configuration.getCustomHeaders().getAddTransferIdHeader()) {
			headers.put("X-Transfer-Id", id);
		}

		if (configuration.getCustomHeaders().getAddForwardedByHeaders()) {
			SocketAddress localAddress = upstreamRequest.localAddress();
			headers.put("X-Forwarded-By-Ip", String.valueOf(localAddress.host()));
			headers.put("X-Forwarded-By-Port", String.valueOf(localAddress.port()));
		}
	}

	private void addRequestDynamicHeaders(Map<String, String> headers) {
		addCommonDynamicHeaders(headers);

		if (configuration.getCustomHeaders().getAddForwardedForHeaders()) {
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
		if (throwable == null) {
			errorMessage += "unknown error";
		} else {
			String throwableMessage = throwable.getMessage();
			if (throwableMessage == null) {
				errorMessage += "'" + throwable.getClass() + "' with no message";
			} else {
				errorMessage += "nested exception " + throwableMessage;
			}
		}
		return errorMessage;
	}
}
