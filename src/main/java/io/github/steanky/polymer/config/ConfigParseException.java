package io.github.steanky.polymer.config;

import java.io.IOException;

/**
 * Used to "unify" all config file parsing exceptions, so that users do not have to deal with any format-specific ones.
 * This class extends IOException and can thus be handled alongside file IO.
 */
public class ConfigParseException extends IOException {
    /**
     * Creates a new ConfigParseException.
     * @param throwable The {@link Throwable} that caused this exception, which may be an exception unique to some
     *                  configuration format
     */
    public ConfigParseException(Throwable throwable) {
        super(throwable);
    }
}
