package io.github.steanky.polymer.config.codec;

import io.github.steanky.polymer.config.TreeBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public interface ConfigCodec {
    @NotNull Map<String, Object> decode(@NotNull InputStream input, boolean close) throws IOException;

    void encode(@NotNull Map<String, Object> input, @NotNull OutputStream output, boolean close) throws IOException;

    @NotNull TreeBuilder getBuilder();

    @NotNull Set<String> getNames();
}
