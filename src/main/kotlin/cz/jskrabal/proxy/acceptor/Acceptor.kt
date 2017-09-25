package cz.jskrabal.proxy.acceptor

import cz.jskrabal.proxy.config.ProxyConfig
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpConnection

class Acceptor(private val vertx: Vertx, private val httpClient: HttpClient, private val configuration: ProxyConfig,
               private val httpConnection: HttpConnection) {

    fun start() {
        //TODO
    }
}
