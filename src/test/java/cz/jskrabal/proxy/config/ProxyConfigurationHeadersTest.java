package cz.jskrabal.proxy.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;

/**
 * Created by janskrabal on 11/06/16.
 */
public class ProxyConfigurationHeadersTest extends AbstractConfigurationTest {
    private static final Map<String, String> CUSTOM_HEADERS;

    static {
        Map<String, String> headers = new HashMap<>();
        headers.put("Via", "VertxProxy");
        headers.put("X-Test-Header", "test value");
        CUSTOM_HEADERS = Collections.unmodifiableMap(headers);
    }

    @Test
    public void getCustomRequestHeaders(){
        addPojoToConfig(ConfigurationParameter.CUSTOM_REQUEST_HEADERS, CUSTOM_HEADERS);
        assertConfigValue(ConfigurationParameter.CUSTOM_REQUEST_HEADERS, CUSTOM_HEADERS);
    }

    @Test
    public void getCustomRequestDefaultValueHeaders(){
        assertDefaultValue(ConfigurationParameter.CUSTOM_REQUEST_HEADERS);
    }

    @Test
    public void getCustomRequestInvalidValueHeaders(){
        addValueToConfig(ConfigurationParameter.CUSTOM_REQUEST_HEADERS, INVALID_NUMBER_VALUE);
        assertDefaultValue(ConfigurationParameter.CUSTOM_REQUEST_HEADERS);
    }

    @Test
    public void getCustomResponseHeaders(){
        addPojoToConfig(ConfigurationParameter.CUSTOM_RESPONSE_HEADERS, CUSTOM_HEADERS);
        assertConfigValue(ConfigurationParameter.CUSTOM_RESPONSE_HEADERS, CUSTOM_HEADERS);
    }

    @Test
    public void getCustomResponseDefaultValueHeaders(){
        assertDefaultValue(ConfigurationParameter.CUSTOM_RESPONSE_HEADERS);
    }

    @Test
    public void getCustomResponseInvalidValueHeaders(){
        addValueToConfig(ConfigurationParameter.CUSTOM_RESPONSE_HEADERS, INVALID_NUMBER_VALUE);
        assertDefaultValue(ConfigurationParameter.CUSTOM_RESPONSE_HEADERS);
    }

    @Test
    public void getRemoveRequestHeadersTest() {
        addPojoToConfig(ConfigurationParameter.REMOVE_REQUEST_HEADERS, CUSTOM_HEADERS.keySet());
        assertConfigValue(ConfigurationParameter.REMOVE_REQUEST_HEADERS, CUSTOM_HEADERS.keySet());
    }

    @Test
    public void getRemoveRequestHeadersDefaultValue() {
        assertDefaultValue(ConfigurationParameter.REMOVE_REQUEST_HEADERS);
    }

    @Test
    public void getRemoveRequestHeadersInvalidValueTest() {
        addValueToConfig(ConfigurationParameter.REMOVE_REQUEST_HEADERS, INVALID_NUMBER_VALUE);
        assertDefaultValue(ConfigurationParameter.REMOVE_REQUEST_HEADERS);
    }

    @Test
    public void getRemoveResponseHeadersTest() {
        addPojoToConfig(ConfigurationParameter.REMOVE_RESPONSE_HEADERS, CUSTOM_HEADERS.keySet());
        assertConfigValue(ConfigurationParameter.REMOVE_RESPONSE_HEADERS, CUSTOM_HEADERS.keySet());
    }

    @Test
    public void getRemoveResponseHeadersDefaultValue() {
        assertDefaultValue(ConfigurationParameter.REMOVE_RESPONSE_HEADERS);
    }

    @Test
    public void getRemoveResponseHeadersInvalidValueTest() {
        addValueToConfig(ConfigurationParameter.REMOVE_RESPONSE_HEADERS, INVALID_NUMBER_VALUE);
        assertDefaultValue(ConfigurationParameter.REMOVE_RESPONSE_HEADERS);
    }

    @Test
    public void getAddTransferIdHeaderFlagTest(){
        addValueToConfig(ConfigurationParameter.ADD_TRANSFER_ID_HEADER, true);
        assertConfigValue(ConfigurationParameter.ADD_TRANSFER_ID_HEADER, true);
    }

    @Test
    public void getAddTransferIdHeaderFlagDefaultValueTest(){
        assertDefaultValue(ConfigurationParameter.ADD_TRANSFER_ID_HEADER);
    }

    @Test
    public void getAddTransferIdHeaderFlagInvalidValueTest(){
        addValueToConfig(ConfigurationParameter.ADD_TRANSFER_ID_HEADER, INVALID_NUMBER_VALUE);
        assertDefaultValue(ConfigurationParameter.ADD_TRANSFER_ID_HEADER);
    }

    @Test
    public void getAddForwardedForHeaderFlagTest(){
        addValueToConfig(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS, true);
        assertConfigValue(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS, true);
    }

    @Test
    public void getAddForwardedForHeaderFlagDefaultValueTest(){
        assertDefaultValue(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS);
    }

    @Test
    public void getAddForwardedForHeaderFlagInvalidValueTest(){
        addValueToConfig(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS, INVALID_NUMBER_VALUE);
        assertDefaultValue(ConfigurationParameter.ADD_FORWARDED_FOR_HEADERS);
    }

    @Test
    public void getAddForwardedByHeaderFlagTest(){
        addValueToConfig(ConfigurationParameter.ADD_FORWARDED_BY_HEADERS, true);
        assertConfigValue(ConfigurationParameter.ADD_FORWARDED_BY_HEADERS, true);
    }

    @Test
    public void getAddForwardedByHeaderFlagDefaultValueTest(){
        assertDefaultValue(ConfigurationParameter.ADD_FORWARDED_BY_HEADERS);
    }

    @Test
    public void getAddForwardedByHeaderFlagInvalidValueTest(){
        addValueToConfig(ConfigurationParameter.ADD_FORWARDED_BY_HEADERS, INVALID_NUMBER_VALUE);
        assertDefaultValue(ConfigurationParameter.ADD_FORWARDED_BY_HEADERS);
    }
}
