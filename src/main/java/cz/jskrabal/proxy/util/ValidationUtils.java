package cz.jskrabal.proxy.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.jskrabal.proxy.dto.NetworkSettings;
import io.netty.util.internal.StringUtil;

/**
 * Created by janskrabal on 04/06/16.
 */
//TODO Use some validation framework or something... This is getting out of hand
public class ValidationUtils {

	private static final int PORT_MIN = 0;
	private static final int PORT_MAX = 65535;

	public static boolean validatePort(Object port) {
		if (port instanceof Integer) {
			int val = (int) port;
			return validatePort(val);
		}
		return false;
	}

	public static boolean validatePort(int port) {
		return port >= PORT_MIN && port <= PORT_MAX;
	}

	public static boolean validateOptionalNetworkSettings(Object networkSettings) {
		if (networkSettings == null) {
			return true;
		}

		if (networkSettings instanceof NetworkSettings) {
			NetworkSettings settings = (NetworkSettings) networkSettings;
			return validatePort(settings.getPort()) && settings.getHost() != null;
		}
		return false;
	}

	public static boolean validateOptionalSetOfTypes(Object object, Class<?> type){
		return object == null || validateSetOfTypes(object, type);
	}

	private static boolean validateSetOfTypes(Object object, Class<?> type) {
		if(object instanceof Set) {
			Set<?> list = (Set<?>) object;
			return list.stream().allMatch(type::isInstance);
		}
		return false;
	}

	public static boolean validateCustomHeaders(Object customHeaders) {
		return customHeaders == null ||
				customHeaders instanceof Map && validateHeaders((Map<?, ?>) customHeaders);
	}

	private static boolean validateHeaders(Map<?, ?> headers) {
		for (Map.Entry<?, ?> entry : headers.entrySet()) {
			if (!isHeaderValid(entry.getKey(), entry.getValue())) {
				return false;
			}
		}
		return true;
	}

	private static boolean isHeaderValid(Object name, Object value) {
		if (name instanceof String && value instanceof String) {
			String valueStr = (String) value;
			String nameStr = (String) name;

			return !StringUtil.isNullOrEmpty(valueStr) && !StringUtil.isNullOrEmpty(nameStr);
		}
		return false;
	}

	public static boolean validateOptionalEnumOption(Object value, Class<?> enumClass) {
		if(value == null) {
			return true;
		}

		if(enumClass.isEnum() && value instanceof String) {
			Object[] constants = enumClass.getEnumConstants();
			return Arrays.stream(constants)
					.map(String::valueOf)
					.anyMatch(value::equals);
		}

		return false;
	}
}
