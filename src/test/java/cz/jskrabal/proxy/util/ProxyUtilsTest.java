package cz.jskrabal.proxy.util;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;

/**
 * Created by janskrabal on 15/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyUtilsTest {
	private static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
	private static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";

	private static final String ACCEPT_ENCODING_CHUNKED = "chunked";
	private static final String CONTENT_ENCODING_GZIP = "gzip";
	private static final String CONTENT_TYPE_JSON = "application/json";

	@Mock
	private HttpClientResponse clientResponse;

	@Mock
	private HttpServerRequest serverRequest;

	@Mock
	private MultiMap headers;

	@Before
	public void setup() {
		when(clientResponse.headers()).thenReturn(headers);
		when(serverRequest.headers()).thenReturn(headers);
	}

	@Test
	public void isChunkedTest() {
		when(headers.get(HEADER_TRANSFER_ENCODING)).thenReturn(ACCEPT_ENCODING_CHUNKED);

		assertTrue(ProxyUtils.isChunked(headers));
		assertTrue(ProxyUtils.isChunked(serverRequest));
		assertTrue(ProxyUtils.isChunked(clientResponse));
	}

	@Test
	public void isChunkedNotChunkedTest() {
		assertFalse(ProxyUtils.isChunked(headers));
		assertFalse(ProxyUtils.isChunked(serverRequest));
		assertFalse(ProxyUtils.isChunked(clientResponse));
	}

	@Test
	public void isGzippedTest() {
		when(headers.get(HEADER_CONTENT_ENCODING)).thenReturn(CONTENT_ENCODING_GZIP);

		assertTrue(ProxyUtils.isGzipped(headers));
		assertTrue(ProxyUtils.isGzipped(clientResponse));
	}

	@Test
	public void isGzippedNotGzippedTest() {
		assertFalse(ProxyUtils.isGzipped(headers));
		assertFalse(ProxyUtils.isGzipped(clientResponse));
	}

	@Test
	public void getContentTypeTest() {
		for (ContentType type : ContentType.values()) {
			assertContentTypeHeader(type);
		}
	}

	@Test
	public void getUnknownContentType() {
		when(headers.get(HEADER_CONTENT_TYPE)).thenReturn(CONTENT_TYPE_JSON);
		Optional<ContentType> contentType = ProxyUtils.getContentType(headers);
		assertFalse(contentType.isPresent());

		contentType = ProxyUtils.getContentType(clientResponse);
		assertFalse(contentType.isPresent());
	}

	private void assertContentTypeHeader(ContentType type) {
		when(headers.get(HEADER_CONTENT_TYPE)).thenReturn(type.getContentType());

		Optional<ContentType> contentType = ProxyUtils.getContentType(headers);
		assertTrue(contentType.isPresent());
		assertEquals(type, contentType.get());

		contentType = ProxyUtils.getContentType(clientResponse);
		assertTrue(contentType.isPresent());
		assertEquals(type, contentType.get());
	}

}
