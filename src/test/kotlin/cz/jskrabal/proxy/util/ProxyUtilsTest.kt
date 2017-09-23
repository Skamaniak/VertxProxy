package cz.jskrabal.proxy.util

import com.nhaarman.mockito_kotlin.whenever
import cz.jskrabal.proxy.ProxyUtils
import cz.jskrabal.proxy.getContentType
import cz.jskrabal.proxy.isChunked
import cz.jskrabal.proxy.isGzipped
import io.vertx.core.MultiMap
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpServerRequest
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

/**
 * Created by janskrabal on 15/06/16.
 */
@RunWith(MockitoJUnitRunner::class)
class ProxyUtilsTest {

    @Mock
    private lateinit var clientResponse: HttpClientResponse

    @Mock
    private lateinit var serverRequest: HttpServerRequest

    @Mock
    private lateinit var headers: MultiMap

    @Before
    fun setup() {
        whenever(clientResponse.headers()).thenReturn(headers)
        whenever(serverRequest.headers()).thenReturn(headers)
    }

    @Test
    fun isChunkedTest() {
        whenever(headers.get(HEADER_TRANSFER_ENCODING)).thenReturn(ACCEPT_ENCODING_CHUNKED)

        assertTrue(ProxyUtils.isChunked(headers))
        assertTrue(serverRequest.isChunked())
        assertTrue(clientResponse.isChunked())
    }

    @Test
    fun isChunkedNotChunkedTest() {
        assertFalse(ProxyUtils.isChunked(headers))
        assertFalse(serverRequest.isChunked())
        assertFalse(clientResponse.isChunked())
    }

    @Test
    fun isGzippedTest() {
        whenever(headers.get(HEADER_CONTENT_ENCODING)).thenReturn(CONTENT_ENCODING_GZIP)

        assertTrue(ProxyUtils.isGzipped(headers))
        assertTrue(clientResponse.isGzipped())
    }

    @Test
    fun isGzippedNotGzippedTest() {
        assertFalse(ProxyUtils.isGzipped(headers))
        assertFalse(clientResponse.isGzipped())
    }

    @Test
    fun getContentTypeTest() {
        for (type in ContentType.values()) {
            assertContentTypeHeader(type)
        }
    }

    @Test
    fun getUnknownContentType() {
        whenever(headers.get(HEADER_CONTENT_TYPE)).thenReturn(CONTENT_TYPE_JSON)
        var contentType = ProxyUtils.getContentType(headers)
        assertFalse(contentType != null)

        contentType = clientResponse.getContentType()
        assertFalse(contentType != null)
    }

    private fun assertContentTypeHeader(type: ContentType) {
        whenever(headers.get(HEADER_CONTENT_TYPE)).thenReturn(type.contentType)

        var contentType = ProxyUtils.getContentType(headers)
        assertTrue(contentType != null)
        assertEquals(type, contentType)

        contentType = clientResponse.getContentType()
        assertTrue(contentType != null)
        assertEquals(type, contentType)
    }

    companion object {
        private const val HEADER_TRANSFER_ENCODING = "Transfer-Encoding"
        private const val HEADER_CONTENT_ENCODING = "Content-Encoding"
        private const val HEADER_CONTENT_TYPE = "Content-Type"

        private const val ACCEPT_ENCODING_CHUNKED = "chunked"
        private const val CONTENT_ENCODING_GZIP = "gzip"
        private const val CONTENT_TYPE_JSON = "application/json"
    }

}
