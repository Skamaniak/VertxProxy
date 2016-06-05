package cz.jskrabal.proxy.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jskrabal.proxy.config.ConfigurationParameter;
import cz.jskrabal.proxy.config.ProxyConfiguration;
import cz.jskrabal.proxy.config.pojo.NetworkSettings;
import cz.jskrabal.proxy.util.ProxyUtils;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;

/**
 * Created by janskrabal on 01/06/16.
 */
public class HttpTransfer extends Transfer {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTransfer.class);

	private final Vertx vertx;
	private final ProxyConfiguration configuration;
	private final String id = ProxyUtils.generateId();

	public HttpTransfer(Vertx vertx, ProxyConfiguration configuration, HttpServerRequest upstreamRequest) {
		super(upstreamRequest);
		this.vertx = vertx;
		this.configuration = configuration;
	}

	@Override
	public void start() {
		HttpClient client = vertx.createHttpClient();
		HttpMethod method = upstreamRequest.method();
		String uri = upstreamRequest.uri();

		NetworkSettings nextProxy = configuration.getValue(ConfigurationParameter.NEXT_HTTP_PROXY,
				NetworkSettings.class);
		HttpClientRequest downstreamRequest;

		if (nextProxy != null) {
			LOGGER.debug("'{}' proxying request '{}' '{}' to next HTTP proxy {}", id, method, uri, nextProxy);
			//TODO handle connection refused throwable
			downstreamRequest = client.request(method, nextProxy.getPort(), nextProxy.getHost(), uri,
					downstreamResponseHandler());
		} else {
			LOGGER.debug("'{}' proxying request '{}' '{}'", id, method, uri);
			//TODO handle connection refused throwable
			downstreamRequest = client.requestAbs(method, uri, downstreamResponseHandler());
		}
		setClientRequestByServerRequest(downstreamRequest, upstreamRequest);

		upstreamRequest.handler(data -> {
			LOGGER.debug("'{}' proxying request data (length '{}')", id, data.length());
			downstreamRequest.write(data);
		});
		upstreamRequest.endHandler((v) -> {
			LOGGER.trace("'{}' closed (up)", id);
			downstreamRequest.end();
		});
	}

	private Handler<HttpClientResponse> downstreamResponseHandler() {
		return downstreamResponse -> {
			int responseCode = downstreamResponse.statusCode();
			LOGGER.debug("'{}' proxying response with code '{}'", id, responseCode);

			setServerResponseByClientResponse(upstreamRequest.response(), downstreamResponse);

			downstreamResponse.handler(data -> {
				LOGGER.debug("'{}' proxying response data (length '{}')", id, data.length());
				upstreamRequest.response().write(data);
			});

			downstreamResponse.endHandler((v) -> {
				LOGGER.trace("'{}' closed (down)", id);
				upstreamRequest.response().end();
			});
		};
	}
}
