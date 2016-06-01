package cz.jskrabal.proxy.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by janskrabal on 01/06/16.
 */
public class IdUtils {
    public static String generateId(){
        return Long.toHexString(ThreadLocalRandom.current().nextLong());
    }
}
