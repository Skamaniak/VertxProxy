package cz.jskrabal.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jskrabal.proxy.config.ProxyConfiguration;
import cz.jskrabal.proxy.transfer.HttpTransfer;
import cz.jskrabal.proxy.transfer.Transfer;
import cz.jskrabal.proxy.transfer.TunnelTransfer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;

public class Proxy extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);
	private ProxyConfiguration configuration;

	@Override
	public void start() throws Exception {
		loadConfig();

		vertx.createHttpServer(createHttpServerOptions()).requestHandler(upstreamRequest -> {
			Transfer transfer;
			if (upstreamRequest.method() == HttpMethod.CONNECT) {
				transfer = new TunnelTransfer(vertx, configuration, upstreamRequest);
			} else {
				transfer = new HttpTransfer(vertx, configuration, upstreamRequest);
			}
			transfer.start();

		}).listen(configuration.getProxyPort(), configuration.getProxyHost());
	}

	private HttpServerOptions createHttpServerOptions() {
		return new HttpServerOptions()
				.setLogActivity(configuration.isNetworkLayerLoggingEnabled());
	}

	private void loadConfig() {
		LOGGER.info("Loaded configuration '{}'", config().encodePrettily());
		configuration = new ProxyConfiguration(config());
	}

}
