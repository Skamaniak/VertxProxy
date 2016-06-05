package cz.jskrabal.proxy.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import cz.jskrabal.proxy.config.pojo.NetworkSettings;
import cz.jskrabal.proxy.util.ValidationUtils;

/**
 * Created by janskrabal on 04/06/16.
 */
public enum ConfigurationParameter implements Validable<Object> {
	NETWORK_PORT(Arrays.asList("network", "port"), Integer.class, 8080) {
		@Override
		public boolean validate(Object value) {
			return getType().isInstance(value) && ValidationUtils.validatePort(value);
		}
	},
	NETWORK_HOST(Arrays.asList("network", "host"), String.class, "0.0.0.0"), NEXT_HTTP_PROXY("nextHttpProxy",
			NetworkSettings.class, null) {
		@Override
		public boolean validate(Object value) {
			return ValidationUtils.validateOptionalNetworkSettings(value);
		}
	},
	NEXT_TUNNEL_PROXY("nextTunnelProxy", NetworkSettings.class, null) { // TODO implement logic
		@Override
		public boolean validate(Object value) {
			return ValidationUtils.validateOptionalNetworkSettings(value);
		}
	};
	// TODO add configurable headers that should be added to the downstream
	// request
	// TODO add configurable headers that should be added to the upstream
	// response
	private List<String> compositionKey;
	private Object defaultValue;
	private Class type;

	ConfigurationParameter(List<String> compositionKey, Class type, Object defaultValue) {
		this.defaultValue = defaultValue;
		this.type = type;
		this.compositionKey = compositionKey;
	}

	ConfigurationParameter(String key, Class type, Object defaultValue) {
		this(Arrays.asList(key), type, defaultValue);
	}

	@Override
	public Class getType() {
		return type;
	}

	public String getFullJsonKey() {
		StringJoiner keyComposer = new StringJoiner(".");
		compositionKey.forEach(keyComposer::add);
		return keyComposer.toString();
	}

	public List<String> getJsonKeyParts() {
		return Collections.unmodifiableList(compositionKey);
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public static Optional<ConfigurationParameter> fromJsonKey(String jsonKey) {
		for (ConfigurationParameter param : ConfigurationParameter.values()) {
			if (param.getFullJsonKey().equals(jsonKey)) {
				return Optional.of(param);
			}
		}
		return Optional.empty();
	}
}
