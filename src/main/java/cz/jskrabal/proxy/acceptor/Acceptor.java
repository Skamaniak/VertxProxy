package cz.jskrabal.proxy.acceptor;

import cz.jskrabal.proxy.config.ProxyConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpConnection;

public class Acceptor {
	private final Vertx vertx;
	private final HttpClient httpClient;
	private final ProxyConfiguration configuration;
	private final HttpConnection httpConnection;

	public Acceptor(Vertx vertx, HttpClient httpClient, ProxyConfiguration configuration,
			HttpConnection httpConnection) {
		this.vertx = vertx;
		this.httpClient = httpClient;
		this.configuration = configuration;
		this.httpConnection = httpConnection;
	}

	public void start() {
		//TODO
	}
}
