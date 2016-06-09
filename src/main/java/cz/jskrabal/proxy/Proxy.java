package cz.jskrabal.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
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

		vertx.createHttpServer(new HttpServerOptions()).requestHandler(upstreamRequest -> {
			Transfer transfer;
			if (upstreamRequest.method() == HttpMethod.CONNECT) {
				transfer = new TunnelTransfer(vertx, configuration, upstreamRequest);
			} else {
				transfer = new HttpTransfer(vertx, configuration, upstreamRequest);
			}
			transfer.start();

		}).listen(getProxyPort(), getProxyHost());
	}

	private void loadConfig() {
		LOGGER.info("Loaded configuration '{}'", config().encodePrettily());
		configuration = new ProxyConfiguration(config());
	}

	private Integer getProxyPort() {
		return configuration.getValue(ConfigurationParameter.NETWORK_PORT, Integer.class);
	}

	private String getProxyHost() {
		return configuration.getValue(ConfigurationParameter.NETWORK_HOST, String.class);
	}

}
