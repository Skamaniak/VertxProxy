package cz.jskrabal.proxy;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

/**
 * Created by janskrabal on 31/05/16.
 */
public class TcpTunnel {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpTunnel.class);

	private final Vertx vertx;
	private final NetSocket upstreamSocket;

	private final String identifier;

	public TcpTunnel(Vertx vertx, NetSocket upstreamSocket, String idFragment) {
		this.vertx = vertx;
		this.upstreamSocket = upstreamSocket;
		this.identifier = idFragment + "_" + UUID.randomUUID().toString();
	}

	public void tunnel(int port, String host) {
		upstreamSocket.pause();
		vertx.createNetClient().connect(port, host, event -> {
			if (event.succeeded()) {
				final NetSocket downstreamSocket = event.result();

				upstreamSocket.handler(data -> {
					downstreamSocket.write(data);
					LOGGER.debug("Tunnel {} proxying upstream data (length {})", identifier, data.length());
				});
				downstreamSocket.handler(data -> {
					upstreamSocket.write(data);
					LOGGER.debug("Tunnel {} proxying downstream data (length {})", identifier, data.length());
				});
				createEndHandlers(upstreamSocket, downstreamSocket);

				upstreamSocket.resume();
				LOGGER.debug("Tunnel between {} and {} was established", upstreamSocket.remoteAddress(),
						downstreamSocket.remoteAddress());
			}
		});
	}

	private void createEndHandlers(NetSocket upSocket, NetSocket downSocket) {
		upSocket.endHandler(event1 -> {
			downSocket.close();
			LOGGER.debug("Tunnel {} closed (up)", identifier);
		});

		downSocket.endHandler(event1 -> {
			upSocket.close();
			LOGGER.debug("Tunnel {} closed (down)", identifier);
		});
	}
}
