package ch.frattino.spotifirepoc;

import org.slf4j.Logger;

public final class Helpers {

    public static String readEnvOrWarn(String key, Logger logger) {
        String value = System.getenv(key);
        if (value == null) {
            logger.error("You forgot to set the " + key + " environment variable!");
        }
        return value;
    }

}
