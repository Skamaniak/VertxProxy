package cz.jskrabal.proxy

import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetClientOptions
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.locks.ReentrantLock


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

class ResourcePool<T>(private val size: Int, private val creator: () -> T) {
    private val pool: BlockingQueue<T> = ArrayBlockingQueue<T>(size, true)
    private val lock = ReentrantLock()
    private var createdObjects = 0

    fun acquire(): T {
        if (!lock.isLocked) {
            if (lock.tryLock()) {
                try {
                    ++createdObjects
                    return creator()
                } finally {
                    if (createdObjects < size) lock.unlock()
                }
            }
        }
        return pool.take()
    }

    fun recycle(resource: T) {
        // Will throws Exception when the queue is full,
        // but it should never happen.
        pool.add(resource)
    }

}