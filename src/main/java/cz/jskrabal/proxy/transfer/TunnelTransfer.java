package cz.jskrabal.proxy.transfer;

import cz.jskrabal.proxy.config.NetworkConfig;
import cz.jskrabal.proxy.config.ProxyConfig;
import cz.jskrabal.proxy.pump.DataPump;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by janskrabal on 31/05/16.
 */
public class TunnelTransfer extends Transfer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TunnelTransfer.class);
	private static final String STATUS_CONNECTION_ESTABLISHED = "Connection established";
	private static final String HOST_PORT_SEPARATOR = ":";
	private static final int INDEX_HOST = 0;
	private static final int INDEX_PORT = 1;

	public TunnelTransfer(Vertx vertx, HttpClient client, ProxyConfig configuration,
			HttpServerRequest connectRequest) {
		super(vertx, client, configuration, connectRequest);
	}

	public void start() {
		LOGGER.debug("'{}' connect request from '{}' to '{}' received", id, upstreamRequest.remoteAddress(),
				upstreamRequest.uri());

		NetworkConfig nextTunnelProxy = getNextTunnelProxySettings();
		if (nextTunnelProxy != null) {
			createTunnelThroughAnotherProxy(nextTunnelProxy);
		} else {
			createDirectTunnel();
		}
	}

	private void createTunnelThroughAnotherProxy(NetworkConfig nextTunnelProxy) {
		resendConnect(nextTunnelProxy, downstreamResponse -> {
			int responseStatusCode = downstreamResponse.statusCode();

			if (responseStatusCode == HttpResponseStatus.OK.code()) {
				LOGGER.debug("'{}' remote proxy '{}' has established the connection to '{}'", id, nextTunnelProxy,
						upstreamRequest.uri());
				tunnelTo(downstreamResponse.netSocket());
			} else {
				LOGGER.debug("{} connection to the remote proxy {} has failed ({} {})", id, nextTunnelProxy,
						downstreamResponse.statusCode(), downstreamResponse.statusMessage());

				configureServerResponseByClientResponse(upstreamRequest.response(), downstreamResponse).end();
			}
		}, throwable -> {
			LOGGER.warn("{} connection to the remote proxy {} has failed. Responding by error to the client's " +
					"connect request", id, nextTunnelProxy, throwable);

			respondConnectionFailed(throwable);
		});
	}

	private NetworkConfig getNextTunnelProxySettings() {
		return configuration.getNextTunnelProxy();
	}

	private void resendConnect(NetworkConfig nextProxySettings, Handler<HttpClientResponse> responseHandler,
			Handler<Throwable> exceptionHandler) {
		LOGGER.debug("'{}' resending connect request from '{}' to '{}' to the next proxy in chain '{}'", id,
				upstreamRequest.remoteAddress(), upstreamRequest.uri(), nextProxySettings);

		HttpClientRequest downstreamConnectRequest = client.request(HttpMethod.CONNECT, nextProxySettings.getPort(),
				nextProxySettings.getHost(), upstreamRequest.uri(), responseHandler);
		addRequestTimeout(downstreamConnectRequest);

		configureClientRequestByServerRequest(downstreamConnectRequest, upstreamRequest)
				.exceptionHandler(exceptionHandler)
				.end();
	}

	private void createDirectTunnel() {
		String uri = upstreamRequest.uri();
		String[] hostAndPort = uri.split(HOST_PORT_SEPARATOR);
		int port = Integer.parseInt(hostAndPort[INDEX_PORT]);
		String host = hostAndPort[INDEX_HOST];

		vertx.createNetClient().connect(port, host, event -> {
			if (event.succeeded()) {
				tunnelTo(event.result());
			} else {
				LOGGER.warn("'{}' connection to the remote server has failed. "
						+ "Responding by error to the client's connect request ({})", id, event.cause());
				respondConnectionFailed(event.cause());
			}
		});
	}

	private void tunnelTo(NetSocket downstreamSocket) {
		respondConnectionEstablished();

		NetSocket upstreamSocket = upstreamRequest.netSocket();
		upstreamSocket.pause();
		createUpstreamHandlers(upstreamSocket, downstreamSocket);
		createDownstreamHandlers(upstreamSocket, downstreamSocket);
		upstreamSocket.resume();

		LOGGER.debug("'{}' tunnel between '{}' and '{}' was established", id, upstreamSocket.remoteAddress(),
				downstreamSocket.remoteAddress());
	}

	private void respondConnectionEstablished() {
		upstreamRequest.response()
				.setStatusCode(HttpResponseStatus.OK.code())
				.setStatusMessage(STATUS_CONNECTION_ESTABLISHED)
				.end();
	}

	private void createUpstreamHandlers(NetSocket upstreamSocket, NetSocket downstreamSocket) {
		DataPump.Companion.create(upstreamSocket, downstreamSocket, data ->
				LOGGER.debug("'{}' proxying upstream data (length '{}')", id, data.length())
		).start();

		upstreamSocket.closeHandler(voidEvent -> {
			LOGGER.debug("'{}' closed (upstream)", id);
			downstreamSocket.close();
		});

		upstreamSocket.endHandler(voidEvent -> {
			LOGGER.trace("'{}' ended (upstream)", id);
			downstreamSocket.end();
		});
	}

	private void createDownstreamHandlers(NetSocket upstreamSocket, NetSocket downstreamSocket) {
		DataPump.Companion.create(downstreamSocket, upstreamSocket, data ->
				LOGGER.debug("'{}' proxying downstream data (length '{}')", id, data.length())
		).start();

		downstreamSocket.closeHandler(voidEvent -> {
			LOGGER.debug("'{}' closed (downstream)", id);
			upstreamSocket.close();
		});

		downstreamSocket.endHandler(voidEvent -> {
			LOGGER.trace("'{}' ended (downstream)", id);
			upstreamSocket.end();
		});
	}
}
