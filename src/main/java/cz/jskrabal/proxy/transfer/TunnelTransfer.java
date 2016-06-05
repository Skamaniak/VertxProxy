package cz.jskrabal.proxy.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jskrabal.proxy.config.ConfigurationParameter;
import cz.jskrabal.proxy.config.ProxyConfiguration;
import cz.jskrabal.proxy.config.pojo.NetworkSettings;
import cz.jskrabal.proxy.util.ProxyUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.NetSocket;

/**
 * Created by janskrabal on 31/05/16.
 */
public class TunnelTransfer extends Transfer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TunnelTransfer.class);
	private static final String STATUS_CONNECTION_ESTABLISHED = "Connection established";
	private static final String HOST_PORT_SEPARATOR = ":";
	private static final int INDEX_HOST = 0;
	private static final int INDEX_PORT = 1;

	private final Vertx vertx;
	private final ProxyConfiguration configuration;
	private final String id = ProxyUtils.generateId();

	public TunnelTransfer(Vertx vertx, ProxyConfiguration configuration, HttpServerRequest connectRequest) {
		super(connectRequest);
		this.vertx = vertx;
		this.configuration = configuration;
	}

	public void start() {
		LOGGER.debug("'{}' connect request from '{}' to '{}' received", id, upstreamRequest.remoteAddress(),
				upstreamRequest.uri());

		NetworkSettings nextTunnelProxy = getNextTunnelProxySettings();
		if (nextTunnelProxy != null) {
			createTunnelThroughAnotherProxy(nextTunnelProxy);
		} else {
			createDirectTunnel();
		}
	}

	private void createTunnelThroughAnotherProxy(NetworkSettings nextTunnelProxy) {
		resendConnect(nextTunnelProxy, downstreamResponse -> {
            int responseStatusCode = downstreamResponse.statusCode();

            if (responseStatusCode == HttpResponseStatus.OK.code()) {
                LOGGER.debug("'{}' remote proxy '{}' has established the connection to '{}'", id, nextTunnelProxy,
                        upstreamRequest.uri());
                tunnelTo(downstreamResponse.netSocket());
            } else {
                LOGGER.debug("{} connection to the remote proxy {} has failed ({} {})", id, nextTunnelProxy,
                        downstreamResponse.statusCode(), downstreamResponse.statusMessage());

                setServerResponseByClientResponse(upstreamRequest.response(), downstreamResponse).end();
            }
        }, throwable -> {
            LOGGER.warn("{} connection to the remote proxy {} has failed. Responding by status 404 to the clients " +
					"connect request", id, nextTunnelProxy, throwable);

            respondConnectionFailed(throwable);
        });
	}

	private NetworkSettings getNextTunnelProxySettings() {
		return configuration.getValue(ConfigurationParameter.NEXT_TUNNEL_PROXY,
				NetworkSettings.class);
	}

	private void resendConnect(NetworkSettings nextProxySettings, Handler<HttpClientResponse> responseHandler,
			Handler<Throwable> exceptionHandler) {
		LOGGER.debug("'{}' resending connect request from '{}' to '{}' to the next proxy in chain '{}'", id,
				upstreamRequest.remoteAddress(), upstreamRequest.uri(), nextProxySettings);

		HttpClient client = vertx.createHttpClient();
		HttpClientRequest downstreamConnectRequest = client.request(HttpMethod.CONNECT, nextProxySettings.getPort(),
				nextProxySettings.getHost(), upstreamRequest.uri(), responseHandler);

		setClientRequestByServerRequest(downstreamConnectRequest, upstreamRequest).exceptionHandler(exceptionHandler)
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
						+ "Responding by status 404 to the clients connect request ({})", id, event.cause());
				respondConnectionFailed(event.cause());
			}
		});
	}

	private void tunnelTo(NetSocket downstreamSocket) {
		respondConnectionEstablished();

		NetSocket upstreamSocket = upstreamRequest.netSocket();
		upstreamSocket.pause();
		createDataHandlers(upstreamSocket, downstreamSocket);
		createEndHandlers(upstreamSocket, downstreamSocket);
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

	private void respondConnectionFailed(Throwable throwable) {
		String errorMessage = getErrorMessage(throwable);
		upstreamRequest.response()
				.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
				.setStatusMessage(errorMessage)
				.end();
	}

	private String getErrorMessage(Throwable throwable) {
		String errorMessage = "Connection to remote server has failed due to ";
		if(throwable == null) {
			errorMessage += "unknown error";
		} else {
			String throwableMessage = throwable.getMessage();
			if(throwableMessage == null) {
				errorMessage += "'" + throwable.getClass() + "' with no message";
			} else {
				errorMessage += "nested exception " + throwableMessage;
			}
		}
		return errorMessage;
	}

	private void createDataHandlers(NetSocket upstreamSocket, NetSocket downstreamSocket) {
		upstreamSocket.handler(data -> {
			LOGGER.debug("'{}' proxying upstream data (length '{}')", id, data.length());
			downstreamSocket.write(data);
		});

		downstreamSocket.handler(data -> {
			LOGGER.debug("'{}' proxying downstream data (length '{}')", id, data.length());
			upstreamSocket.write(data);
		});
	}

	private void createEndHandlers(NetSocket upSocket, NetSocket downSocket) {
		upSocket.endHandler(event1 -> {
			LOGGER.trace("'{}' closed (up)", id);
			downSocket.close();
		});

		downSocket.endHandler(event1 -> {
			LOGGER.trace("'{}' closed (down)", id);
			upSocket.close();
		});
	}
}
