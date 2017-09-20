package cz.jskrabal.proxy.transfer;

import cz.jskrabal.proxy.config.ProxyConfiguration;
import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import cz.jskrabal.proxy.dto.NetworkSettings;
import cz.jskrabal.proxy.pump.DataPump;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by janskrabal on 01/06/16.
 */
public class HttpTransfer extends Transfer {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTransfer.class);

	public HttpTransfer(Vertx vertx, HttpClient client, ProxyConfiguration configuration,
			HttpServerRequest upstreamRequest) {
		super(vertx, client, configuration, upstreamRequest);
	}

	@Override
	public void start() {
		HttpMethod method = upstreamRequest.method();
		String uri = upstreamRequest.uri();

		NetworkSettings nextProxy = configuration.getValue(ConfigurationParameter.NEXT_HTTP_PROXY,
				NetworkSettings.class);

		HttpClientRequest downstreamRequest;
		if (nextProxy != null) {
			LOGGER.debug("'{}' proxying request '{}' '{}' to next HTTP proxy {}", id, method, uri, nextProxy);
			downstreamRequest = client.request(method, nextProxy.getPort(), nextProxy.getHost(), uri);
		} else {
			LOGGER.debug("'{}' proxying request '{}' '{}'", id, method, uri);
			downstreamRequest = client.requestAbs(method, uri);
		}

		downstreamRequest.handler(downstreamResponseHandler());
		downstreamRequest.exceptionHandler(downstreamExceptionHandler(method, uri));

		configureClientRequestByServerRequest(downstreamRequest, upstreamRequest);
		createRequestHandler(downstreamRequest);
	}

	private Handler<Throwable> downstreamExceptionHandler(HttpMethod method, String uri) {
		return throwable -> {
			LOGGER.warn("{} request '{}' '{}' has failed. Responding by error to the client's HTTP request",
					id, method, uri);

			respondConnectionFailed(throwable);
		};
	}

	private Handler<HttpClientResponse> downstreamResponseHandler() {
		return downstreamResponse -> {
			int responseCode = downstreamResponse.statusCode();
			LOGGER.debug("'{}' proxying response with code '{}'", id, responseCode);

			configureServerResponseByClientResponse(upstreamRequest.response(), downstreamResponse);
			createResponseHandlers(downstreamResponse);
		};
	}

	private void createResponseHandlers(HttpClientResponse downstreamResponse) {
		new DataPump<>(downstreamResponse, upstreamRequest.response(), data ->
				LOGGER.debug("'{}' proxying response data (length '{}')", id, data.length())
		).start();

		downstreamResponse.endHandler(voidEvent -> {
			LOGGER.debug("'{}' ended (downstream)", id);
			upstreamRequest.response().end();
		});
	}

	private void createRequestHandler(HttpClientRequest downstreamRequest) {
		new DataPump<>(upstreamRequest, downstreamRequest, data ->
				LOGGER.debug("'{}' proxying request data (length '{}')", id, data.length())
		).start();

		upstreamRequest.endHandler(voidEvent -> {
			LOGGER.debug("'{}' ended (upstream)", id);
			downstreamRequest.end();
		});
	}
}
