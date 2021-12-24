package io.github.steanky.polymer.config.codec;

import io.github.steanky.polymer.config.ConfigParseException;
import io.github.steanky.polymer.config.ConfigPrimitive;
import io.github.steanky.polymer.config.TreeBuilder;
import io.github.steanky.polymer.config.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public interface ConfigCodec {
    @NotNull Map<String, Object> decode(@NotNull InputStream stream, boolean close) throws IOException;

    void encode(@NotNull OutputStream outputStream, @NotNull ConfigNode node, boolean close) throws IOException;

    @NotNull TreeBuilder getBuilder();

    @NotNull Set<String> getNames();
}
