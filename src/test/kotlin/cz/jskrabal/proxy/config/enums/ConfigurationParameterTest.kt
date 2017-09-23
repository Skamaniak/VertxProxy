package cz.jskrabal.proxy.config.enums

import org.junit.Assert.assertEquals

import org.junit.Test

/**
 * Created by janskrabal on 15/06/16.
 */
class ConfigurationParameterTest {

    @Test
    fun testGetFullJsonKey() {
        var fullKeyParts: Array<String>
        var keyParts: List<String>

        for (parameter in ConfigurationParameter.values()) {
            keyParts = parameter.jsonKeyParts
            fullKeyParts = parameter.fullJsonKey.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            assertEquals(keyParts.size.toLong(), fullKeyParts.size.toLong())

            for (i in keyParts.indices) {
                assertEquals(keyParts[i], fullKeyParts[i])
            }
        }
    }

}
