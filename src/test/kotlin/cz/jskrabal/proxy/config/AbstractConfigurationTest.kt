package cz.jskrabal.proxy.config

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat

import org.junit.Before

import cz.jskrabal.proxy.config.enums.ConfigurationParameter
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * Created by janskrabal on 11/06/16.
 */
abstract class AbstractConfigurationTest {

    protected lateinit var config: JsonObject
    protected lateinit var proxyConfig: ProxyConfiguration

    @Before
    fun setup() {
        config = JsonObject()
        proxyConfig = ProxyConfiguration(config)
    }

    protected fun assertDefaultValue(parameter: ConfigurationParameter) {
        assertConfigValue(parameter, parameter.defaultValue)
    }

    protected fun assertConfigValue(parameter: ConfigurationParameter, expected: Any?) {
        assertThat(proxyConfig.getValue(parameter, parameter.type), `is`(expected))
    }

    protected fun addValueToConfig(parameter: ConfigurationParameter, value: Any) {
        addToConfig(config, parameter.jsonKeyParts, value)
    }

    protected fun addPojoToConfig(parameter: ConfigurationParameter, value: Any) {
        val jsonString = Json.encode(value)
        val json: Any
        if (value is Collection<*>) {
            json = JsonArray(jsonString)
        } else {
            json = JsonObject(jsonString)
        }
        addToConfig(config, parameter.jsonKeyParts, json)
    }

    private fun addToConfig(config: JsonObject, key: List<String>, value: Any) {
        var relativeJsonObject = config
        var keyPart: String

        for (i in key.indices) {
            keyPart = key[i]
            if (i == key.size - 1) {
                relativeJsonObject.put(keyPart, value)
            } else {
                if (!relativeJsonObject.containsKey(keyPart)) {
                    val subJson = JsonObject()
                    relativeJsonObject.put(keyPart, subJson)
                    relativeJsonObject = subJson
                }
            }
        }
    }

    companion object {
        const val INVALID_STRING_VALUE = "invalidValue"
        const val INVALID_NUMBER_VALUE = Integer.MAX_VALUE
        const val LOCALHOST_IP = "127.0.0.1"
        const val DEBUG_LOGGING = true
        const val HTTP_RESERVED_PORT = 80
    }
}
