package cz.jskrabal.proxy.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jskrabal.proxy.util.IdUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.NetSocket;

/**
 * Created by janskrabal on 31/05/16.
 */
public class SslTransfer implements Transfer {
	private static final Logger LOGGER = LoggerFactory.getLogger(SslTransfer.class);
	private static final String STATUS_CONNECTION_ESTABLISHED = "Connection established";
	private static final String HOST_PORT_SEPARATOR = ":";
	private static final int INDEX_HOST = 0;
	private static final int INDEX_PORT = 1;

	private final Vertx vertx;
	private final HttpServerRequest upstreamRequest;

	private final String id = IdUtils.generateId();

	public SslTransfer(Vertx vertx, HttpServerRequest connectRequest) {
		this.vertx = vertx;
		this.upstreamRequest = connectRequest;
	}

	public void start() {
		String uri = upstreamRequest.uri();
		String[] hostAndPort = uri.split(HOST_PORT_SEPARATOR);
		int port = Integer.parseInt(hostAndPort[INDEX_PORT]);
		String host = hostAndPort[INDEX_HOST];

		LOGGER.debug("{} connect request from {} to {} received", id, upstreamRequest.remoteAddress(), uri);
		respondConnectionEstablished(upstreamRequest);
		NetSocket upstreamSocket = upstreamRequest.netSocket();
		upstreamSocket.pause();

		vertx.createNetClient().connect(port, host, event -> {
			if (event.succeeded()) {
				final NetSocket downstreamSocket = event.result();
				createDataHandlers(upstreamSocket, downstreamSocket);
				createEndHandlers(upstreamSocket, downstreamSocket);
				upstreamSocket.resume();

				LOGGER.debug("{} ssl tunnel between {} and {} was established", id,
						upstreamSocket.remoteAddress(), downstreamSocket.remoteAddress());
			}
		});
	}

	private void respondConnectionEstablished(HttpServerRequest upstreamRequest) {
		upstreamRequest.response().setStatusCode(HttpResponseStatus.OK.code())
				.setStatusMessage(STATUS_CONNECTION_ESTABLISHED).end();
	}

	private void createDataHandlers(NetSocket upstreamSocket, NetSocket downstreamSocket) {
		upstreamSocket.handler(data -> {
			downstreamSocket.write(data);
			LOGGER.debug("{} proxying upstream data (length {})", id, data.length());
		});

		downstreamSocket.handler(data -> {
			upstreamSocket.write(data);
			LOGGER.debug("{} proxying downstream data (length {})", id, data.length());
		});
	}

	private void createEndHandlers(NetSocket upSocket, NetSocket downSocket) {
		upSocket.endHandler(event1 -> {
			downSocket.close();
			LOGGER.debug("{} closed (up)", id);
		});

		downSocket.endHandler(event1 -> {
			upSocket.close();
			LOGGER.debug("{} closed (down)", id);
		});
	}
}
