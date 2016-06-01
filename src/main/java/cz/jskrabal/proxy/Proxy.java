package cz.jskrabal.proxy;

import cz.jskrabal.proxy.transfer.HttpTransfer;
import cz.jskrabal.proxy.transfer.Transfer;
import cz.jskrabal.proxy.transfer.SslTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jskrabal.proxy.config.Configuration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;

public class Proxy extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

	@Override
	public void start() throws Exception {
		LOGGER.info("Loaded configuration {}", config());

		vertx.createHttpServer(new HttpServerOptions()).requestHandler(upstreamRequest -> {
			Transfer transfer;
			if (upstreamRequest.method() == HttpMethod.CONNECT) {
				transfer = new SslTransfer(vertx, upstreamRequest);
			} else {
				transfer = new HttpTransfer(vertx, upstreamRequest);
			}
			transfer.start();

		}).listen(getProxyPort(), getProxyHost());
	}

	private int getProxyPort(){
		return config().getInteger(Configuration.NETWORK_PORT, Configuration.NETWORK_PORT_DEFAULT);
	}

	private String getProxyHost(){
		return config().getString(Configuration.NETWORK_HOST, Configuration.NETWORK_HOST_DEFAULT);
	}

}
