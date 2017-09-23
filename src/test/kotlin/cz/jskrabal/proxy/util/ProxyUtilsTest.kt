package cz.jskrabal.proxy.util

import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.mockito.Mockito.`when`

import java.util.Optional

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

import io.vertx.core.MultiMap
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpServerRequest

/**
 * Created by janskrabal on 15/06/16.
 */
@RunWith(MockitoJUnitRunner::class)
class ProxyUtilsTest {

    @Mock
    private val clientResponse: HttpClientResponse? = null

    @Mock
    private val serverRequest: HttpServerRequest? = null

    @Mock
    private val headers: MultiMap? = null

    @Before
    fun setup() {
        `when`(clientResponse!!.headers()).thenReturn(headers)
        `when`(serverRequest!!.headers()).thenReturn(headers)
    }

    @Test
    fun isChunkedTest() {
        `when`(headers!!.get(HEADER_TRANSFER_ENCODING)).thenReturn(ACCEPT_ENCODING_CHUNKED)

        assertTrue(ProxyUtils.isChunked(headers))
        assertTrue(ProxyUtils.isChunked(serverRequest))
        assertTrue(ProxyUtils.isChunked(clientResponse))
    }

    @Test
    fun isChunkedNotChunkedTest() {
        assertFalse(ProxyUtils.isChunked(headers))
        assertFalse(ProxyUtils.isChunked(serverRequest))
        assertFalse(ProxyUtils.isChunked(clientResponse))
    }

    @Test
    fun isGzippedTest() {
        `when`(headers!!.get(HEADER_CONTENT_ENCODING)).thenReturn(CONTENT_ENCODING_GZIP)

        assertTrue(ProxyUtils.isGzipped(headers))
        assertTrue(ProxyUtils.isGzipped(clientResponse))
    }

    @Test
    fun isGzippedNotGzippedTest() {
        assertFalse(ProxyUtils.isGzipped(headers))
        assertFalse(ProxyUtils.isGzipped(clientResponse))
    }

    @Test
    fun getContentTypeTest() {
        for (type in ContentType.values()) {
            assertContentTypeHeader(type)
        }
    }

    @Test
    fun getUnknownContentType() {
        `when`(headers!!.get(HEADER_CONTENT_TYPE)).thenReturn(CONTENT_TYPE_JSON)
        var contentType = ProxyUtils.getContentType(headers)
        assertFalse(contentType.isPresent)

        contentType = ProxyUtils.getContentType(clientResponse)
        assertFalse(contentType.isPresent)
    }

    private fun assertContentTypeHeader(type: ContentType) {
        `when`(headers!!.get(HEADER_CONTENT_TYPE)).thenReturn(type.contentType)

        var contentType = ProxyUtils.getContentType(headers)
        assertTrue(contentType.isPresent)
        assertEquals(type, contentType.get())

        contentType = ProxyUtils.getContentType(clientResponse)
        assertTrue(contentType.isPresent)
        assertEquals(type, contentType.get())
    }

    companion object {
        private val HEADER_TRANSFER_ENCODING = "Transfer-Encoding"
        private val HEADER_CONTENT_ENCODING = "Content-Encoding"
        private val HEADER_CONTENT_TYPE = "Content-Type"

        private val ACCEPT_ENCODING_CHUNKED = "chunked"
        private val CONTENT_ENCODING_GZIP = "gzip"
        private val CONTENT_TYPE_JSON = "application/json"
    }

}
