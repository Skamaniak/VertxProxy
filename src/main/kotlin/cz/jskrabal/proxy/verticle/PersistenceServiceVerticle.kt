package cz.jskrabal.proxy.verticle

import cz.jskrabal.proxy.config.PersistenceConfig
import cz.jskrabal.proxy.service.PersistenceService
import cz.jskrabal.proxy.service.PersistenceServiceFactory

class PersistenceServiceVerticle(private val config: PersistenceConfig) : ServiceVerticle<PersistenceService>() {
    override val serviceClass = PersistenceService::class
    override val address: String = SERVICE_ADDRESS
    override val service: PersistenceService
        get() {
            return PersistenceServiceFactory.create(vertx, config)
        }

    companion object {
        const val SERVICE_ADDRESS = "persistence-service"
    }
}