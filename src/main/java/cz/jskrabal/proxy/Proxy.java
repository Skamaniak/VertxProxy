package cz.jskrabal.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jskrabal.proxy.acceptor.Acceptor;
import cz.jskrabal.proxy.config.ProxyConfiguration;
import cz.jskrabal.proxy.transfer.HttpTransfer;
import cz.jskrabal.proxy.transfer.Transfer;
import cz.jskrabal.proxy.transfer.TunnelTransfer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;

import java.util.concurrent.TimeUnit;

public class Proxy extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);
	private ProxyConfiguration configuration;
	private HttpClient httpClient;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		vertx.executeBlocking(event -> {
			configuration = loadConfig();
			httpClient = vertx.createHttpClient(createHttpClientOptions());
			vertx.createHttpServer(createHttpServerOptions())
					.requestHandler(this::transfer)
					.connectionHandler(this::connect)
					.listen(configuration.getProxyPort(), configuration.getProxyHost(), result -> {
						if (result.succeeded()) {
							event.complete();
						} else {
							event.fail(result.cause());
						}
					});
		}, startFuture.completer());
	}

	private HttpServerOptions createHttpServerOptions() {
		return new HttpServerOptions()
				.setLogActivity(configuration.isUpstreamDebugLoggingEnabled())
				.setIdleTimeout(configuration.getUpstreamIdleTimeout());
	}

	private HttpClientOptions createHttpClientOptions() {
		return new HttpClientOptions()
				.setLogActivity(configuration.isDownstreamDebugLoggingEnabled())
				.setConnectTimeout(configuration.getDownstreamConnectionTimeout())
				.setIdleTimeout((int) TimeUnit.MILLISECONDS.toSeconds(configuration.getDownstreamIdleTimeout()));
	}

	private ProxyConfiguration loadConfig() {
		LOGGER.info("Loaded configuration '{}'", config().encodePrettily());
		return new ProxyConfiguration(config());
	}

	private void connect(HttpConnection httpConnection) {
		new Acceptor(vertx, httpClient, configuration, httpConnection).start();
	}

	private void transfer(HttpServerRequest upstreamRequest) {
		Transfer transfer;
		if (upstreamRequest.method() == HttpMethod.CONNECT) {
			transfer = new TunnelTransfer(vertx, httpClient, configuration, upstreamRequest);
		} else {
			transfer = new HttpTransfer(vertx, httpClient, configuration, upstreamRequest);
		}
		transfer.start();
	}
}
