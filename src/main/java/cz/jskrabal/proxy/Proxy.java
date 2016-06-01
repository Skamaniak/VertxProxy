package cz.jskrabal.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;

public class Proxy extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);
	private static final String STATUS_CONNECTION_ESTABLISHED = "Connection established";
	private static final String HOST_PORT_SEPARATOR = ":";
	private static final int INDEX_HOST = 0;
	private static final int INDEX_PORT = 1;
	private static final String ACCEPT_ENCODING_CHUNKED = "chunked";
	private static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";

	private static boolean isChunked(HttpServerRequest request) {
		return isChunked(request.headers());
	}

	private static boolean isChunked(HttpClientResponse response) {
		return isChunked(response.headers());
	}

	private static boolean isChunked(MultiMap headers) {
		String acceptEncoding = headers.get(HEADER_TRANSFER_ENCODING);
		return ACCEPT_ENCODING_CHUNKED.equals(acceptEncoding);
	}

	@Override
	public void start() throws Exception {
		vertx.createHttpServer(new HttpServerOptions()).requestHandler(upstreamRequest -> {

			if (upstreamRequest.method() == HttpMethod.CONNECT) {
				String uri = upstreamRequest.uri();
				LOGGER.debug("Connect request from {} to {} received", upstreamRequest.remoteAddress(), uri);
				String[] hostAndPort = uri.split(HOST_PORT_SEPARATOR);
				int port = Integer.parseInt(hostAndPort[INDEX_PORT]);
				String host = hostAndPort[INDEX_HOST];

				tunnelConnection(upstreamRequest, port, host);
			} else {
				handleHttpRequest(upstreamRequest);
			}
		}).listen(8080);

	}

	private void handleHttpRequest(HttpServerRequest upstreamRequest) {
		HttpClient client = vertx.createHttpClient();
		HttpMethod method = upstreamRequest.method();
		String uri = upstreamRequest.uri();

		LOGGER.debug("Proxying request {} {}", method, uri);
		HttpClientRequest downstreamRequest = client.requestAbs(method, uri, downstreamResponse -> {
			int responseCode = downstreamResponse.statusCode();
			LOGGER.debug("Proxying response with code {}", responseCode);
			upstreamRequest.response().setStatusCode(responseCode);
			upstreamRequest.response().setChunked(isChunked(downstreamResponse));
			upstreamRequest.response().headers().setAll(downstreamResponse.headers());

			downstreamResponse.handler(data -> {
				LOGGER.debug("Proxying response data (length {})", data.length());
				upstreamRequest.response().write(data);
			});
			downstreamResponse.endHandler((v) -> upstreamRequest.response().end());
		});

		downstreamRequest.setChunked(isChunked(upstreamRequest));
		downstreamRequest.headers().setAll(upstreamRequest.headers());
		upstreamRequest.handler(data -> {
			LOGGER.debug("Proxying request data (length {})", data.length());
			downstreamRequest.write(data);
		});
		upstreamRequest.endHandler((v) -> downstreamRequest.end());
	}

	private void tunnelConnection(HttpServerRequest upstreamRequest, int port, String host) {
		respondConnectionEstablished(upstreamRequest);
		TcpTunnel tunnel = new TcpTunnel(vertx, upstreamRequest.netSocket(), host);
		tunnel.tunnel(port, host);
	}

	private void respondConnectionEstablished(HttpServerRequest upstreamRequest) {
		upstreamRequest.response().setStatusCode(HttpResponseStatus.OK.code())
				.setStatusMessage(STATUS_CONNECTION_ESTABLISHED).end();
	}
}
