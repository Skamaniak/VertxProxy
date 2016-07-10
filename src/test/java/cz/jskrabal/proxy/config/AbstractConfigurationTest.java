package cz.jskrabal.proxy.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.Before;

import cz.jskrabal.proxy.config.enums.ConfigurationParameter;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by janskrabal on 11/06/16.
 */
/*package*/ abstract class AbstractConfigurationTest {
    protected static final String INVALID_STRING_VALUE = "invalidValue";
    protected static final int INVALID_NUMBER_VALUE = Integer.MAX_VALUE;
    protected static final String LOCALHOST_IP = "127.0.0.1";
    protected static final boolean DEBUG_LOGGING = true;
    protected static final int HTTP_RESERVED_PORT = 80;

    protected JsonObject config;
    protected ProxyConfiguration proxyConfig;

    @Before
    public void setup() {
        config = new JsonObject();
        proxyConfig = new ProxyConfiguration(config);
    }

    protected void assertDefaultValue(ConfigurationParameter parameter) {
        assertConfigValue(parameter, parameter.getDefaultValue());
    }

    protected void assertConfigValue(ConfigurationParameter parameter, Object expected) {
        assertThat(proxyConfig.getValue(parameter, parameter.getType()), is(expected));
    }

    protected void addValueToConfig(ConfigurationParameter parameter, Object value) {
        addToConfig(config, parameter.getJsonKeyParts(), value);
    }

    protected void addPojoToConfig(ConfigurationParameter parameter, Object value) {
        String jsonString = Json.encode(value);
        Object json;
        if(value instanceof Collection) {
            json = new JsonArray(jsonString);
        } else {
            json = new JsonObject(jsonString);
        }
        addToConfig(config, parameter.getJsonKeyParts(), json);
    }

    private void addToConfig(JsonObject config, List<String> key, Object value) {
        JsonObject relativeJsonObject = config;
        String keyPart;

        for (int i = 0; i < key.size(); i++) {
            keyPart = key.get(i);
            if (i == key.size() - 1) {
                relativeJsonObject.put(keyPart, value);
            } else {
                if (!relativeJsonObject.containsKey(keyPart)) {
                    JsonObject subJson = new JsonObject();
                    relativeJsonObject.put(keyPart, subJson);
                    relativeJsonObject = subJson;
                }
            }
        }
    }
}
