package cz.jskrabal.proxy.support;

import io.vertx.core.http.HttpMethod;

import java.util.EnumSet;

/**
 * Created by janskrabal on 06/07/16.
 */
public class ProxyTestUtils {
	private ProxyTestUtils() {}

	public static final int HTTP_SERVER_PORT = 7070;
	public static final int PROXY_PORT = 8080;
	public static final String PROXY_HOST = "localhost";
	public static final String BASE_REQUEST_URI = "http://" + PROXY_HOST + ':' + HTTP_SERVER_PORT + '/';

	//Test HTTP server constants
	public static final String URL_PARAM_RESPONSE = "response";
	public static final String URL_PARAM_RESPONSE_CODE = "responseCode";
	public static final String URL_PARAM_RESPONSE_TYPE = "responseType";

	public static boolean canHaveBody(HttpMethod method) {
		EnumSet methodsThatRestrictsRequestBody = EnumSet.of(
				HttpMethod.OPTIONS,
				HttpMethod.CONNECT,
				HttpMethod.GET,
				HttpMethod.HEAD,
				HttpMethod.TRACE);

		return !methodsThatRestrictsRequestBody.contains(method);
	}
}
