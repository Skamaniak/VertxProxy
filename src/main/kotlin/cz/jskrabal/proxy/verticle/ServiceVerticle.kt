package cz.jskrabal.proxy.verticle

import cz.jskrabal.proxy.loggerFor
import io.vertx.core.AbstractVerticle
import io.vertx.serviceproxy.ProxyHelper
import kotlin.reflect.KClass

abstract class ServiceVerticle<S : Any> : AbstractVerticle() {
    abstract val service: S
    abstract val address: String
    abstract val serviceClass: KClass<S>

    override fun start() {
        ProxyHelper.registerService(
                serviceClass.java,
                vertx,
                service,
                address)
        loggerFor(javaClass).info("Started ${javaClass.simpleName} instance. DeploymentID: ${deploymentID()} Address: $address")
    }
}