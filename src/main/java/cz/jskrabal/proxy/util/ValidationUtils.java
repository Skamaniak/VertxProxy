package cz.jskrabal.proxy.util;


import cz.jskrabal.proxy.config.pojo.NetworkSettings;

/**
 * Created by janskrabal on 04/06/16.
 */
public class ValidationUtils {

    private static final int PORT_MIN = 0;
    private static final int PORT_MAX = 65535;

    public static boolean validatePort(Object port) {
        if(port instanceof Integer) {
            int val = (int) port;
            return validatePort(val);
        }
        return false;
    }

    public static boolean validatePort(int port) {
        return port >= PORT_MIN && port <= PORT_MAX;
    }

    public static boolean validateOptionalNetworkSettings(Object value) {
        if(value == null) {
            return true;
        }

        if(value instanceof NetworkSettings) {
            NetworkSettings settings = (NetworkSettings) value;
            return validatePort(settings.getPort()) && settings.getHost() != null;
        }
        return false;
    }
}
