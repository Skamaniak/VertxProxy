package cz.jskrabal.proxy

import cz.jskrabal.proxy.model.ContentType
import io.vertx.core.MultiMap
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpServerRequest

/**
 * Created by janskrabal on 01/06/16.
 */

fun HttpServerRequest.isChunked(): Boolean {
    return ProxyUtils.isChunked(headers())
}

fun HttpClientResponse.isChunked(): Boolean {
    return ProxyUtils.isChunked(headers())
}

fun HttpClientResponse.isGzipped(): Boolean {
    return ProxyUtils.isGzipped(headers())
}

fun HttpClientResponse.getContentType(): ContentType? {
    return ProxyUtils.getContentType(headers())
}

internal object ProxyUtils {
    private const val HEADER_TRANSFER_ENCODING = "Transfer-Encoding"
    private const val HEADER_CONTENT_ENCODING = "Content-Encoding"
    private const val HEADER_CONTENT_TYPE = "Content-Type"
    private const val ACCEPT_ENCODING_CHUNKED = "chunked"
    private const val CONTENT_ENCODING_GZIP = "gzip"

    fun isChunked(headers: MultiMap) =
            isHeaderEqualTo(headers, HEADER_TRANSFER_ENCODING, ACCEPT_ENCODING_CHUNKED)

    fun isGzipped(headers: MultiMap) =
            isHeaderEqualTo(headers, HEADER_CONTENT_ENCODING, CONTENT_ENCODING_GZIP)

    fun getContentType(headers: MultiMap): ContentType? =
            ContentType.fromContentType(headers.get(HEADER_CONTENT_TYPE))

    private fun isHeaderEqualTo(headers: MultiMap, header: String, expValue: String) =
            headers.get(header) == expValue
}
