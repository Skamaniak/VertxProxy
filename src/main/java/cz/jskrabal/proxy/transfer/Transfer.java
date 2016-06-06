package cz.jskrabal.proxy.transfer;

import java.nio.channels.UnresolvedAddressException;

import cz.jskrabal.proxy.util.ProxyUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
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

	protected HttpServerResponse configureServerResponseByClientResponse(HttpServerResponse serverResponse,
			HttpClientResponse clientResponse) {
		serverResponse
                .setStatusCode(clientResponse.statusCode())
                .setStatusMessage(clientResponse.statusMessage())
				.setChunked(ProxyUtils.isChunked(clientResponse))
                .headers()
                    .setAll(clientResponse.headers());

		return serverResponse;
	}

	protected HttpClientRequest configureClientRequestByServerRequest(HttpClientRequest clientRequest,
			HttpServerRequest serverRequest) {
		clientRequest
                .setChunked(ProxyUtils.isChunked(serverRequest))
                .headers()
                    .setAll(serverRequest.headers());

		return clientRequest;
	}

	protected void respondConnectionFailed(Throwable throwable) {
		String errorMessage = getErrorMessage(throwable);

		HttpResponseStatus status;
		if(throwable instanceof UnresolvedAddressException) {
			status = HttpResponseStatus.NOT_FOUND;
		} else {
			status =  HttpResponseStatus.BAD_GATEWAY;
		}

		upstreamRequest.response()
				.setStatusCode(status.code())
				.setStatusMessage(errorMessage)
				.end();
	}

	//FIXME possible security issue - revealing implementation details to the proxy client.
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
}
