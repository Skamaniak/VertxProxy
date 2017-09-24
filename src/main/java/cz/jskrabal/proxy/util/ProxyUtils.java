package cz.jskrabal.proxy.util;

import java.util.Optional;

import cz.jskrabal.proxy.model.ContentType;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;

/**
 * Created by janskrabal on 01/06/16.
 */
public class ProxyUtils {
    private static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String ACCEPT_ENCODING_CHUNKED = "chunked";
    private static final String CONTENT_ENCODING_GZIP = "gzip";

    public static boolean isChunked(HttpServerRequest request) {
        return isChunked(request.headers());
    }

    public static boolean isChunked(HttpClientResponse response) {
        return isChunked(response.headers());
    }

    public static boolean isChunked(MultiMap headers) {
        return isHeaderEqualTo(headers, HEADER_TRANSFER_ENCODING, ACCEPT_ENCODING_CHUNKED);
    }

    public static boolean isGzipped(HttpClientResponse response) {
        return isGzipped(response.headers());
    }

    public static boolean isGzipped(MultiMap headers) {
        return isHeaderEqualTo(headers, HEADER_CONTENT_ENCODING, CONTENT_ENCODING_GZIP);
    }

    public static Optional<ContentType> getContentType(HttpClientResponse response) {
        return getContentType(response.headers());
    }

    public static Optional<ContentType> getContentType(MultiMap headers) {
        String value = headers.get(HEADER_CONTENT_TYPE);
        return Optional.ofNullable(ContentType.Companion.fromContentType(value));
    }

    private static boolean isHeaderEqualTo(MultiMap headers, String header, String expValue) {
        String headerValue = headers.get(header);
        return headerValue != null && headerValue.equals(expValue);
    }
}
