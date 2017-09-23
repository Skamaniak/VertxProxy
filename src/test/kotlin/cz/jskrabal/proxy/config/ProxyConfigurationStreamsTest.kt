package cz.jskrabal.proxy.config

import org.junit.Test
import org.junit.runner.RunWith

import cz.jskrabal.proxy.config.enums.ConfigurationParameter
import junitparams.JUnitParamsRunner
import junitparams.Parameters

@RunWith(JUnitParamsRunner::class)
class ProxyConfigurationStreamsTest : AbstractConfigurationTest() {

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
    @Parameters(method = "provideInvalidValueTestParams")
    fun testInvalidValue(param: ConfigurationParameter, invalidValue: Any) {
        addValueToConfig(param, invalidValue)
        assertDefaultValue(param)
    }

    companion object {

        private val TIMEOUT = 1234

        @JvmStatic
        private fun provideDefaultValueTestParams(): Array<Any> {
            return arrayOf(arrayOf<Any>(ConfigurationParameter.STREAM_DOWNSTREAM_CONNECTION_TIMEOUT_MILLIS), arrayOf<Any>(ConfigurationParameter.STREAM_DOWNSTREAM_IDLE_TIMEOUT_MILLIS), arrayOf<Any>(ConfigurationParameter.STREAM_UPSTREAM_IDLE_TIMEOUT_MILLIS), arrayOf<Any>(ConfigurationParameter.STREAM_DOWNSTREAM_REQUEST_TIMEOUT_MILLIS), arrayOf<Any>(ConfigurationParameter.STREAM_DOWNSTREAM_DEBUG_LOGGING), arrayOf<Any>(ConfigurationParameter.STREAM_UPSTREAM_DEBUG_LOGGING))
        }

        @JvmStatic
        private fun provideSetAndGetTestParams(): Array<Any> {
            return arrayOf(arrayOf(ConfigurationParameter.STREAM_DOWNSTREAM_CONNECTION_TIMEOUT_MILLIS, TIMEOUT), arrayOf(ConfigurationParameter.STREAM_DOWNSTREAM_IDLE_TIMEOUT_MILLIS, TIMEOUT), arrayOf(ConfigurationParameter.STREAM_UPSTREAM_IDLE_TIMEOUT_MILLIS, TIMEOUT), arrayOf(ConfigurationParameter.STREAM_DOWNSTREAM_REQUEST_TIMEOUT_MILLIS, TIMEOUT), arrayOf(ConfigurationParameter.STREAM_DOWNSTREAM_DEBUG_LOGGING, true), arrayOf(ConfigurationParameter.STREAM_UPSTREAM_DEBUG_LOGGING, true))
        }

        @JvmStatic
        private fun provideInvalidValueTestParams(): Array<Any> {
            return arrayOf(arrayOf(ConfigurationParameter.STREAM_DOWNSTREAM_CONNECTION_TIMEOUT_MILLIS, AbstractConfigurationTest.INVALID_STRING_VALUE), arrayOf(ConfigurationParameter.STREAM_DOWNSTREAM_IDLE_TIMEOUT_MILLIS, AbstractConfigurationTest.INVALID_STRING_VALUE), arrayOf(ConfigurationParameter.STREAM_UPSTREAM_IDLE_TIMEOUT_MILLIS, AbstractConfigurationTest.INVALID_STRING_VALUE), arrayOf(ConfigurationParameter.STREAM_DOWNSTREAM_DEBUG_LOGGING, AbstractConfigurationTest.INVALID_STRING_VALUE), arrayOf(ConfigurationParameter.STREAM_DOWNSTREAM_REQUEST_TIMEOUT_MILLIS, AbstractConfigurationTest.INVALID_STRING_VALUE), arrayOf(ConfigurationParameter.STREAM_UPSTREAM_DEBUG_LOGGING, AbstractConfigurationTest.INVALID_STRING_VALUE))
        }
    }
}
