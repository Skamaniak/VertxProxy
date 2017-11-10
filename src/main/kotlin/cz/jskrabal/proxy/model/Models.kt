package cz.jskrabal.proxy.model

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get

interface Jsonable {
    fun toJson(): JsonObject {
        return JsonObject.mapFrom(this)
    }
}

data class Proxy(val id: String, val port: Int = 7000, val deploymentId: String = "") : Jsonable {
    constructor(js: JsonObject) : this(
            id = js["id"],
            port = js["port"],
            deploymentId = js["deploymentId"]
    )
}