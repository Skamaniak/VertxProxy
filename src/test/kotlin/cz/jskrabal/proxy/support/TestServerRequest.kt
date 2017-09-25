package cz.jskrabal.proxy.support

import io.vertx.core.http.HttpMethod
import io.vertx.ext.unit.TestContext

/**
 * Created by janskrabal on 08/07/16.
 */
class TestServerRequest(val context: TestContext, val httpMethod: HttpMethod, val response: String, val responseCode: Int,
                        val responseType: ResponseType) {

    class Builder(private val context: TestContext, private val httpMethod: HttpMethod) {
        private var response: String = ""
        private var responseCode = 200
        private var responseType = ResponseType.RESPOND

        fun withResponse(response: String): Builder {
            this.response = response
            return this
        }

        fun withResponseCode(responseCode: Int): Builder {
            this.responseCode = responseCode
            return this
        }

        fun withResponseType(responseType: ResponseType): Builder {
            this.responseType = responseType
            return this
        }

        fun build(): TestServerRequest {
            return TestServerRequest(context, httpMethod, response, responseCode, responseType)
        }
    }

    companion object {

        fun create(context: TestContext, httpMethod: HttpMethod): Builder {
            return Builder(context, httpMethod)
        }
    }
}
