package cz.jskrabal.proxy.config

import org.junit.Test
import org.junit.runner.RunWith

import cz.jskrabal.proxy.config.enums.ConfigurationParameter
import cz.jskrabal.proxy.dto.NetworkSettings
import junitparams.JUnitParamsRunner
import junitparams.Parameters

/**
 * Created by janskrabal on 11/06/16.
 */
@RunWith(JUnitParamsRunner::class)
class ProxyConfigurationProxyChainingTest : AbstractConfigurationTest() {

    private fun provideDefaultValueTestParams(): Array<Any> {
        return arrayOf(arrayOf<Any>(ConfigurationParameter.NEXT_TUNNEL_PROXY), arrayOf<Any>(ConfigurationParameter.NEXT_HTTP_PROXY))
    }

    @Test
    @Parameters(method = "provideDefaultValueTestParams")
    fun testDefaultValue(param: ConfigurationParameter) {
        assertDefaultValue(param)
    }

    private fun provideSetAndGetPojoTestParams(): Array<Any> {
        return arrayOf(arrayOf(ConfigurationParameter.NEXT_TUNNEL_PROXY, NetworkSettings(AbstractConfigurationTest.LOCALHOST_IP, AbstractConfigurationTest.HTTP_RESERVED_PORT)), arrayOf(ConfigurationParameter.NEXT_HTTP_PROXY, NetworkSettings(AbstractConfigurationTest.LOCALHOST_IP, AbstractConfigurationTest.HTTP_RESERVED_PORT)))
    }

    @Test
    @Parameters(method = "provideSetAndGetPojoTestParams")
    fun testSetAndGetPojo(param: ConfigurationParameter, value: Any) {
        addPojoToConfig(param, value)
        assertConfigValue(param, value)
    }

    private fun provideInvalidPojoValueTestParams(): Array<Any> {
        return arrayOf(arrayOf(ConfigurationParameter.NEXT_TUNNEL_PROXY, NetworkSettings(null, AbstractConfigurationTest.HTTP_RESERVED_PORT)), arrayOf(ConfigurationParameter.NEXT_TUNNEL_PROXY, NetworkSettings(AbstractConfigurationTest.LOCALHOST_IP, AbstractConfigurationTest.INVALID_NUMBER_VALUE)), arrayOf(ConfigurationParameter.NEXT_HTTP_PROXY, NetworkSettings(null, AbstractConfigurationTest.HTTP_RESERVED_PORT)), arrayOf(ConfigurationParameter.NEXT_HTTP_PROXY, NetworkSettings(AbstractConfigurationTest.LOCALHOST_IP, AbstractConfigurationTest.INVALID_NUMBER_VALUE)))
    }

    @Test
    @Parameters(method = "provideInvalidPojoValueTestParams")
    fun testInvalidPojoValue(param: ConfigurationParameter, invalidValue: Any) {
        addPojoToConfig(param, invalidValue)
        assertDefaultValue(param)
    }

    private fun provideInvalidValueTestParams(): Array<Any> {
        return arrayOf(arrayOf(ConfigurationParameter.NEXT_TUNNEL_PROXY, AbstractConfigurationTest.INVALID_STRING_VALUE), arrayOf(ConfigurationParameter.NEXT_HTTP_PROXY, AbstractConfigurationTest.INVALID_STRING_VALUE))
    }

    @Test
    @Parameters(method = "provideInvalidValueTestParams")
    fun testInvalidValue(param: ConfigurationParameter, invalidValue: Any) {
        addValueToConfig(param, invalidValue)
        assertDefaultValue(param)
    }

}
