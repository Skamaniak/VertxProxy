package cz.jskrabal.proxy.integration;

import java.util.EnumSet;

import org.junit.Test;

import cz.jskrabal.proxy.support.ResponseType;
import cz.jskrabal.proxy.support.TestServerRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * Created by janskrabal on 08/07/16.
 */
public class HttpIntegrationTest extends AbstractProxyTest {
	private static String TEST_MESSAGE = "Everything is awesome!";

	public HttpIntegrationTest() {
		super("/config/basicConfiguration.json");
	}

	@Test
	public void testProxyHttpRequest(TestContext context) {
		EnumSet<HttpMethod> notTestedMethods = EnumSet.of(HttpMethod.CONNECT, HttpMethod.OTHER);
		EnumSet<HttpMethod> httpMethods = EnumSet.complementOf(notTestedMethods);

		for (HttpMethod httpMethod : httpMethods) {
			sendRequestWithExpectedResponseCode(context, httpMethod, 200);
			sendRequestWithExpectedResponseCode(context, httpMethod, 301);
			sendRequestWithExpectedResponseCode(context, httpMethod, 404);
			sendRequestWithExpectedResponseCode(context, httpMethod, 500);
		}
	}

	@Test
	public void testProxyHttpRequestWhenServerNotResponding(TestContext context) {
		final Async async = context.async();

		TestServerRequest request = TestServerRequest.create(context, HttpMethod.GET)
				.withResponseType(ResponseType.DO_NOT_RESPOND)
				.build();

		executeRequest(request,
				response -> {
					assertErrorStatus(context, response, HttpResponseStatus.GATEWAY_TIMEOUT);
					response.endHandler(e -> async.complete());
				},
				context::fail);

	}

	@Test
	public void testProxyHttpRequestWhenServerSnapsConnection(TestContext context) throws InterruptedException {
		final Async async = context.async();
		TestServerRequest request = TestServerRequest.create(context, HttpMethod.GET)
				.withResponseType(ResponseType.SNAP_CONNECTION)
				.build();

		executeRequest(request,
				response -> {
					assertErrorStatus(context, response, HttpResponseStatus.BAD_GATEWAY);
					response.endHandler(e -> async.complete());
				},
				context::fail);
	}

	private void assertErrorStatus(TestContext context, HttpClientResponse response,
			HttpResponseStatus status) {

		context.assertTrue(response.statusCode() == status.code());
	}

	private void sendRequestWithExpectedResponseCode(TestContext context, HttpMethod method, int responseCode) {
		final Async async = context.async();
		TestServerRequest request = TestServerRequest.create(context, method)
				.withResponse(TEST_MESSAGE)
				.withResponseCode(responseCode)
				.build();

		executeRequest(request, response -> {
			context.assertEquals(response.statusCode(), responseCode);
			response.handler(body -> context.assertTrue(body.toString().contains(TEST_MESSAGE)));
			response.endHandler(event -> async.complete());
		});
	}

}
