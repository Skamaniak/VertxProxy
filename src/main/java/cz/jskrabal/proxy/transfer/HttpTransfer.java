package cz.jskrabal.proxy.transfer;

import cz.jskrabal.proxy.util.IdUtils;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by janskrabal on 01/06/16.
 */
public class HttpTransfer implements Transfer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTransfer.class);
    private static final String ACCEPT_ENCODING_CHUNKED = "chunked";
    private static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";

    private final Vertx vertx;
    private final HttpServerRequest upstreamRequest;
    private final String id = IdUtils.generateId();

    public HttpTransfer(Vertx vertx, HttpServerRequest upstreamRequest) {
        this.vertx = vertx;
        this.upstreamRequest = upstreamRequest;
    }

    @Override
    public void start() {
        HttpClient client = vertx.createHttpClient();
        HttpMethod method = upstreamRequest.method();
        String uri = upstreamRequest.uri();

        LOGGER.debug("HttpTransfer {} proxying request {} {}", id, method, uri);
        HttpClientRequest downstreamRequest = client.requestAbs(method, uri, downstreamResponse -> {
            int responseCode = downstreamResponse.statusCode();
            LOGGER.debug("HttpTransfer {} proxying response with code {}", id, responseCode);
            upstreamRequest.response().setStatusCode(responseCode);
            upstreamRequest.response().setChunked(isChunked(downstreamResponse));
            upstreamRequest.response().headers().setAll(downstreamResponse.headers());

            downstreamResponse.handler(data -> {
                LOGGER.debug("HttpTransfer {} proxying response data (length {})", id, data.length());
                upstreamRequest.response().write(data);
            });
            downstreamResponse.endHandler((v) -> upstreamRequest.response().end());
        });

        downstreamRequest.setChunked(isChunked(upstreamRequest));
        downstreamRequest.headers().setAll(upstreamRequest.headers());
        upstreamRequest.handler(data -> {
            LOGGER.debug("HttpTransfer {} proxying request data (length {})", id, data.length());
            downstreamRequest.write(data);
        });
        upstreamRequest.endHandler((v) -> downstreamRequest.end());
    }

    private static boolean isChunked(HttpServerRequest request) {
        return isChunked(request.headers());
    }

    private static boolean isChunked(HttpClientResponse response) {
        return isChunked(response.headers());
    }

    private static boolean isChunked(MultiMap headers) {
        String acceptEncoding = headers.get(HEADER_TRANSFER_ENCODING);
        return ACCEPT_ENCODING_CHUNKED.equals(acceptEncoding);
    }
}
