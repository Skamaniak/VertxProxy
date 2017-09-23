package cz.jskrabal.proxy.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import cz.jskrabal.proxy.Proxy;
import cz.jskrabal.proxy.support.ProxyTestUtils;
import cz.jskrabal.proxy.support.ResponseType;
import cz.jskrabal.proxy.support.TestHttpServerVerticle;
import cz.jskrabal.proxy.support.TestServerRequest;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Created by janskrabal on 15/06/16.
 */
@RunWith(VertxUnitRunner.class)
public abstract class AbstractProxyTest {
	private final String configPath;
	protected Vertx vertx;

	public AbstractProxyTest(String proxyConfigPath) {
		this.configPath = proxyConfigPath;
	}

	@Before
	public void setUp(TestContext context) throws IOException {
		vertx = Vertx.vertx();
		Async async = context.async(2);

		JsonObject configuration = readTestConfig(configPath);
		DeploymentOptions options = new DeploymentOptions().setConfig(configuration);
		vertx.deployVerticle(Proxy.class.getName(), options, result -> {
			context.<String>asyncAssertSuccess().handle(result);
			async.countDown();
		});
		vertx.deployVerticle(TestHttpServerVerticle.class.getName(), result -> {
			context.<String>asyncAssertSuccess().handle(result);
			async.countDown();
		});

		async.awaitSuccess();
	}

	@After
	public void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	protected void executeRequest(TestServerRequest request, Handler<HttpClientResponse> responseHandler) {
		executeRequest(request, responseHandler, event -> request.getContext().fail(event));
	}

	protected void executeRequest(TestServerRequest request, Handler<HttpClientResponse> responseHandler,
			Handler<Throwable> exceptionHandler) {

		if (ProxyTestUtils.canHaveBody(request.getHttpMethod())) {
			executeWithBody(request, responseHandler, exceptionHandler);
		} else {
			executeWithoutBody(request, responseHandler, exceptionHandler);
		}
	}

	private JsonObject readTestConfig(String path) throws IOException {
		InputStream configStream = AbstractProxyTest.class.getResourceAsStream(path);
		String configurationJson = IOUtils.toString(configStream);

		return new JsonObject(configurationJson);
	}

	private void executeWithBody(TestServerRequest request, Handler<HttpClientResponse> responseHandler,
			Handler<Throwable> exceptionHandler) {
		String uriWithoutResponse = uriWithoutResponse(request.getResponseCode(), request.getResponseType());

		HttpClient client = vertx.createHttpClient();
		client.request(request.getHttpMethod(), ProxyTestUtils.PROXY_PORT, ProxyTestUtils.PROXY_HOST,
				uriWithoutResponse, responseHandler)
				.exceptionHandler(exceptionHandler)
				.end(request.getResponse());
	}

	private void executeWithoutBody(TestServerRequest request, Handler<HttpClientResponse> responseHandler,
			Handler<Throwable> exceptionHandler) {
		String uriWithResponse = uriWithResponse(request.getResponse(), request.getResponseCode(),
				request.getResponseType());

		HttpClient client = vertx.createHttpClient();
		client.request(request.getHttpMethod(), ProxyTestUtils.PROXY_PORT, ProxyTestUtils.PROXY_HOST,
				uriWithResponse, responseHandler)
				.exceptionHandler(exceptionHandler)
				.end();
	}



	private static String uriWithResponse(String response, int responseCode, ResponseType responseType) {
		String uriWithoutResponse = uriWithoutResponse(responseCode, responseType);
		if (response == null) {
			return uriWithoutResponse;
		}

		String urlEncodedResponse;
		try {
			urlEncodedResponse = URLEncoder.encode(response, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Can't url-encode response due to nested exception", e);
		}

		return uriWithoutResponse + '&' + ProxyTestUtils.URL_PARAM_RESPONSE + '=' + urlEncodedResponse;
	}

	private static String uriWithoutResponse(int responseCode, ResponseType responseType) {
		return ProxyTestUtils.BASE_REQUEST_URI +
				'?' + ProxyTestUtils.URL_PARAM_RESPONSE_CODE + '=' + responseCode +
				'&' + ProxyTestUtils.URL_PARAM_RESPONSE_TYPE + '=' + responseType;
	}

}
