package cz.jskrabal.proxy.verticle

import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.service.ProxyService
import cz.jskrabal.proxy.service.ProxyServiceFactory

class ProxyServiceVerticle(private val config: ProxyConfig) : ServiceVerticle<ProxyService>() {
    override val serviceClass = ProxyService::class
    override val address: String = SERVICE_ADDRESS
    override val service: ProxyService
        get() {
            return ProxyServiceFactory.create(vertx, config)
        }

    companion object {
        const val SERVICE_ADDRESS = "proxy-service"
    }
}