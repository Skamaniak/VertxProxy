package cz.jskrabal.proxy.support

import java.util.EnumSet

import io.vertx.core.http.HttpMethod

/**
 * Created by janskrabal on 06/07/16.
 */
object ProxyTestUtils {

    val HTTP_SERVER_PORT = 7070
    val PROXY_PORT = 7000
    val PROXY_HOST = "localhost"
    val BASE_REQUEST_URI = "http://$PROXY_HOST:$HTTP_SERVER_PORT/"

    //Test HTTP server constants
    val URL_PARAM_RESPONSE = "response"
    val URL_PARAM_RESPONSE_CODE = "responseCode"
    val URL_PARAM_RESPONSE_TYPE = "responseType"

    fun canHaveBody(method: HttpMethod): Boolean {
        val methodsThatRestrictsRequestBody = EnumSet.of(
                HttpMethod.OPTIONS,
                HttpMethod.CONNECT,
                HttpMethod.GET,
                HttpMethod.HEAD,
                HttpMethod.TRACE)

        return !methodsThatRestrictsRequestBody.contains(method)
    }
}
