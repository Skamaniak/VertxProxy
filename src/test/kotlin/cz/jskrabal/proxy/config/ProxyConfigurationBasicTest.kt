package cz.jskrabal.proxy.config

import org.junit.Assert.assertEquals

import org.junit.Test
import org.junit.runner.RunWith

import cz.jskrabal.proxy.config.enums.ConfigurationParameter
import cz.jskrabal.proxy.config.enums.IdGeneratorType
import junitparams.JUnitParamsRunner
import junitparams.Parameters

/**
 * Created by janskrabal on 10/06/16.
 */
@RunWith(JUnitParamsRunner::class)
internal class ProxyConfigurationBasicTest : AbstractConfigurationTest() {

    @Test
    fun initProxyConfigWithExistingValidJson() {
        addValueToConfig(ConfigurationParameter.NETWORK_PORT, AbstractConfigurationTest.HTTP_RESERVED_PORT)
        val proxyConfiguration = ProxyConfiguration(config)

        val port = proxyConfiguration.getValue(ConfigurationParameter.NETWORK_PORT, Int::class.javaObjectType)
        assertEquals(AbstractConfigurationTest.HTTP_RESERVED_PORT.toLong(), port.toLong())
    }

    @Test
    fun initProxyConfigWithExistingInvalidJson() {
        addValueToConfig(ConfigurationParameter.NETWORK_PORT, AbstractConfigurationTest.INVALID_STRING_VALUE)
        val proxyConfiguration = ProxyConfiguration(config)

        val port = proxyConfiguration.getValue(ConfigurationParameter.NETWORK_PORT, Int::class.javaObjectType)
        assertEquals(ConfigurationParameter.NETWORK_PORT.defaultValue, port)
    }

    @Test(expected = ClassCastException::class)
    fun getConfigValueTypeMismatchTest() {
        proxyConfig.getValue(ConfigurationParameter.NETWORK_PORT, String::class.java)
    }

    private fun provideDefaultValueTestParams(): Array<Any> {
        return arrayOf(arrayOf<Any>(ConfigurationParameter.ID_GENERATOR), arrayOf<Any>(ConfigurationParameter.NETWORK_HOST), arrayOf<Any>(ConfigurationParameter.NETWORK_PORT))
    }

    @Test
    @Parameters(method = "provideDefaultValueTestParams")
    fun testDefaultValue(param: ConfigurationParameter) {
        assertDefaultValue(param)
    }

    private fun provideSetAndGetTestParams(): Array<Any> {
        return arrayOf(arrayOf(ConfigurationParameter.ID_GENERATOR, IdGeneratorType.SEQUENCE.name), arrayOf(ConfigurationParameter.NETWORK_HOST, AbstractConfigurationTest.LOCALHOST_IP), arrayOf(ConfigurationParameter.NETWORK_PORT, AbstractConfigurationTest.HTTP_RESERVED_PORT))
    }

    @Test
    @Parameters(method = "provideSetAndGetTestParams")
    fun testSetAndGet(param: ConfigurationParameter, value: Any) {
        addValueToConfig(param, value)
        assertConfigValue(param, value)
    }

    private fun provideInvalidValueTestParams(): Array<Any> {
        return arrayOf(arrayOf(ConfigurationParameter.ID_GENERATOR, AbstractConfigurationTest.INVALID_NUMBER_VALUE), arrayOf(ConfigurationParameter.NETWORK_HOST, AbstractConfigurationTest.INVALID_NUMBER_VALUE), arrayOf(ConfigurationParameter.NETWORK_PORT, AbstractConfigurationTest.INVALID_STRING_VALUE), arrayOf(ConfigurationParameter.NETWORK_PORT, AbstractConfigurationTest.INVALID_NUMBER_VALUE))
    }

    @Test
    @Parameters(method = "provideInvalidValueTestParams")
    fun testInvalidValue(param: ConfigurationParameter, invalidValue: Any) {
        addValueToConfig(param, invalidValue)
        assertDefaultValue(param)
    }

}
