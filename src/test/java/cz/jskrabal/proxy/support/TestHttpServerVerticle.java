package cz.jskrabal.proxy.support;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.streams.Pump;

/**
 * Created by janskrabal on 06/07/16.
 */
public class TestHttpServerVerticle extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		vertx.createHttpServer().requestHandler(upstreamRequest -> {
			ResponseType responseType = getExpectedResponseType(upstreamRequest);

			switch (responseType) {
				case RESPOND:
					processRequest(upstreamRequest);
					break;
				case DO_NOT_RESPOND:
					break;
				case SNAP_CONNECTION:
					upstreamRequest.response().close();
			}
		}).listen(ProxyTestUtils.HTTP_SERVER_PORT);
	}

	private void processRequest(HttpServerRequest upstreamRequest) {
		if(ProxyTestUtils.canHaveBody(upstreamRequest.method())) {
			respondWithBody(upstreamRequest);
		} else {
			respond(upstreamRequest);
		}
	}

	private ResponseType getExpectedResponseType(HttpServerRequest request) {
		String expectedResponseType = request.getParam(ProxyTestUtils.URL_PARAM_RESPONSE_TYPE);

		return expectedResponseType == null ? ResponseType.RESPOND : ResponseType.valueOf(expectedResponseType);
	}

	private void respondWithBody(HttpServerRequest upstreamRequest) {
		int httpCode = getExpectedResponseCode(upstreamRequest);

		upstreamRequest.response()
				.setStatusCode(httpCode)
				.setChunked(true);

		Pump.pump(upstreamRequest, upstreamRequest.response()).start();
		upstreamRequest.endHandler(
				voidEvent -> upstreamRequest.response().end()
		);
	}

	private void respond(HttpServerRequest request) {
		String response = request.getParam(ProxyTestUtils.URL_PARAM_RESPONSE);
		response = response == null ? "" : response;
		int httpCode = getExpectedResponseCode(request);

		request.response()
				.setStatusCode(httpCode)
				.end(response);
	}

	private int getExpectedResponseCode(HttpServerRequest request) {
		String responseCode = request.getParam(ProxyTestUtils.URL_PARAM_RESPONSE_CODE);
		return responseCode == null ? 200 : Integer.parseInt(responseCode);
	}
}
