package io.github.steanky.polymer.config.codec;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class CodecRegistry {
    public static final CodecRegistry INSTANCE = new CodecRegistry();

    private final Map<String, ConfigCodec> codecMap = new HashMap<>();

    //enforce singleton
    private CodecRegistry() {
        //register built-in codecs
        List<ConfigCodec> defaultCodecs = List.of(new TomlCodec());

        for(ConfigCodec codec : defaultCodecs) {
            registerCodecInternal(codec);
        }
    }

    private void registerCodecInternal(ConfigCodec codec) {
        for(String name : codec.getNames()) {
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

    public void registerCodec(@NotNull ConfigCodec codec) {
        Validate.notNull(codec);
        Validate.isTrue(!(codec.getNames()).isEmpty(), "codec must supply at least one name");

        registerCodecInternal(codec);
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
