package cz.jskrabal.proxy.config

import java.util.Collections
import java.util.HashMap

import org.junit.Test
import org.junit.runner.RunWith

import cz.jskrabal.proxy.config.enums.ConfigurationParameter
import junitparams.JUnitParamsRunner
import junitparams.Parameters

/**
 * Created by janskrabal on 11/06/16.
 */
@RunWith(JUnitParamsRunner::class)
class ProxyConfigurationHeadersTest : AbstractConfigurationTest() {

    @Test
    @Parameters(method = "provideDefaultValueTestParams")
    fun testDefaultValue(param: ConfigurationParameter) {
        assertDefaultValue(param)
    }

    @Test
    @Parameters(method = "provideSetAndGetTestParams")
    fun testSetAndGet(param: ConfigurationParameter, value: Any) {
        addValueToConfig(param, value)
        assertConfigValue(param, value)
    }

    @Test
    @Parameters(method = "provideSetAndGetPojoTestParams")
    fun testSetAndGetPojo(param: ConfigurationParameter, value: Any) {
        addPojoToConfig(param, value)
        assertConfigValue(param, value)
    }

    @Test
    @Parameters(method = "provideInvalidValueTestParams")
    fun testInvalidValue(param: ConfigurationParameter, invalidValue: Any) {
        addValueToConfig(param, invalidValue)
        assertDefaultValue(param)
    }

    companion object {
        private val CUSTOM_HEADERS: Map<String, String>

        init {
            val headers = HashMap<String, String>()
            headers.put("Via", "VertxProxy")
            headers.put("X-Test-Header", "test value")
            CUSTOM_HEADERS = Collections.unmodifiableMap(headers)
        }

        @JvmStatic
        private fun provideDefaultValueTestParams(): Array<Any> {
            return arrayOf(arrayOf<Any>(ConfigurationParameter.ADD_FORWARDED_BY_HEADERS), arrayOf<Any>(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS), arrayOf<Any>(ConfigurationParameter.ADD_TRANSFER_ID_HEADER), arrayOf<Any>(ConfigurationParameter.REMOVE_RESPONSE_HEADERS), arrayOf<Any>(ConfigurationParameter.REMOVE_REQUEST_HEADERS), arrayOf<Any>(ConfigurationParameter.CUSTOM_RESPONSE_HEADERS), arrayOf<Any>(ConfigurationParameter.CUSTOM_REQUEST_HEADERS))
        }

        @JvmStatic
        private fun provideSetAndGetTestParams(): Array<Any> {
            return arrayOf(arrayOf(ConfigurationParameter.ADD_FORWARDED_BY_HEADERS, true), arrayOf(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS, true), arrayOf(ConfigurationParameter.ADD_TRANSFER_ID_HEADER, true))
        }

        @JvmStatic
        private fun provideSetAndGetPojoTestParams(): Array<Any> {
            return arrayOf(arrayOf(ConfigurationParameter.REMOVE_RESPONSE_HEADERS, CUSTOM_HEADERS.keys), arrayOf(ConfigurationParameter.REMOVE_REQUEST_HEADERS, CUSTOM_HEADERS.keys), arrayOf(ConfigurationParameter.CUSTOM_RESPONSE_HEADERS, CUSTOM_HEADERS), arrayOf(ConfigurationParameter.CUSTOM_REQUEST_HEADERS, CUSTOM_HEADERS))
        }

        @JvmStatic
        private fun provideInvalidValueTestParams(): Array<Any> {
            return arrayOf(arrayOf(ConfigurationParameter.ADD_FORWARDED_BY_HEADERS, AbstractConfigurationTest.INVALID_NUMBER_VALUE), arrayOf(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS, AbstractConfigurationTest.INVALID_NUMBER_VALUE), arrayOf(ConfigurationParameter.ADD_TRANSFER_ID_HEADER, AbstractConfigurationTest.INVALID_NUMBER_VALUE), arrayOf(ConfigurationParameter.REMOVE_RESPONSE_HEADERS, AbstractConfigurationTest.INVALID_NUMBER_VALUE), arrayOf(ConfigurationParameter.REMOVE_REQUEST_HEADERS, AbstractConfigurationTest.INVALID_NUMBER_VALUE), arrayOf(ConfigurationParameter.CUSTOM_RESPONSE_HEADERS, AbstractConfigurationTest.INVALID_NUMBER_VALUE), arrayOf(ConfigurationParameter.CUSTOM_REQUEST_HEADERS, AbstractConfigurationTest.INVALID_NUMBER_VALUE))
        }
    }
}
