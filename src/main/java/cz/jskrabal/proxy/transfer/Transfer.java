package cz.jskrabal.proxy.transfer;

import cz.jskrabal.proxy.util.ProxyUtils;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

/**
 * Created by janskrabal on 01/06/16.
 */
public abstract class Transfer {
    protected final HttpServerRequest upstreamRequest;

    public Transfer(HttpServerRequest upstreamRequest) {
        this.upstreamRequest = upstreamRequest;
    }

    public abstract void start();

	protected HttpServerResponse setServerResponseByClientResponse(HttpServerResponse serverResponse,
			HttpClientResponse clientResponse) {
		serverResponse
                .setStatusCode(clientResponse.statusCode())
                .setStatusMessage(clientResponse.statusMessage())
				.setChunked(ProxyUtils.isChunked(clientResponse))
                .headers()
                    .setAll(clientResponse.headers());

		return serverResponse;
	}

	protected HttpClientRequest setClientRequestByServerRequest(HttpClientRequest clientRequest,
			HttpServerRequest serverRequest) {
		clientRequest
                .setChunked(ProxyUtils.isChunked(serverRequest))
                .headers()
                    .setAll(serverRequest.headers());

		return clientRequest;
	}
}
