package cz.jskrabal.proxy

import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus


fun Vertx.deployVerticleFuture(verticle: Verticle): Future<String> {
    return deployVerticleFuture(verticle, DeploymentOptions())
}

fun Vertx.deployVerticleFuture(name: String): Future<String> {
    return deployVerticleFuture(name, DeploymentOptions())
}

fun Vertx.deployVerticleFuture(verticle: Verticle, options: DeploymentOptions): Future<String> {
    val future = Future.future<String>()
    deployVerticle(verticle, options, future.completer())
    return future
}

fun Vertx.deployVerticleFuture(name: String, options: DeploymentOptions): Future<String> {
    val future = Future.future<String>()
    deployVerticle(name, options, future.completer())
    return future
}

fun EventBus.sendAction(address: String, action: String): EventBus {
    return this.send(address, null, DeliveryOptions().addHeader("action", action))
}