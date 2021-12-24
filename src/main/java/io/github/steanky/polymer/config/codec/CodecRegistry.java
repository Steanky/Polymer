package io.github.steanky.polymer.config.codec;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CodecRegistry {
    public static final CodecRegistry INSTANCE = new CodecRegistry();

    private final Map<String, ConfigCodec> codecMap = new HashMap<>();

    //enforce singleton
    private CodecRegistry() {}

    public void registerCodec(@NotNull ConfigCodec codec) {
        Validate.notNull(codec);

        Set<String> codecNames;
        Validate.isTrue(!(codecNames = codec.getNames()).isEmpty(), "codec did not supply any names");

        for(String name : codecNames) {
            Validate.notNull(name, "codec name cannot be null");

            if(codecMap.containsKey(name)) {
                throw new IllegalArgumentException("attempt made to register codec with name that already exists: " +
                        name);
            }
            else {
                codecMap.put(name, codec);
            }
        }
    }

    public ConfigCodec getCodec(@NotNull String name) {
        Validate.notNull(name);
        return codecMap.get(name);
    }

    public boolean hasCodec(@NotNull String name) {
        Validate.notNull(name);
        return codecMap.containsKey(name);
    }
}
