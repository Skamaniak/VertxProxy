package cz.jskrabal.proxy.model

import io.vertx.core.json.JsonObject

interface Jsonable {
    fun toJson(): JsonObject {
        return JsonObject.mapFrom(this)
    }
}

data class Proxy(val id: String, val port: Int = 7000, val deploymentId: String = "") : Jsonable