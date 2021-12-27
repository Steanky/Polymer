package io.github.steanky.polymer.config.codec;

import io.github.steanky.polymer.config.BasicTreeBuilder;
import io.github.steanky.polymer.config.TreeBuilder;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

abstract class AbstractConfigCodec implements ConfigCodec {
    private final Set<String> names;

    AbstractConfigCodec(@NotNull Set<String> names) {
        this.names = Validate.noNullElements(names);
    }

    @Override
    public final @NotNull Map<String, Object> decode(@NotNull InputStream input, boolean close) throws IOException {
        try {
            return read(input);
        }
        finally {
            if(close) {
                input.close();
            }
        }
    }

    @Override
    public final void encode(@NotNull Map<String, Object> input, @NotNull OutputStream output, boolean close)
            throws IOException {
        try {
            write(input, output);
        }
        finally {
            if(close) {
                output.close();
            }
        }
    }

    @Override
    public @NotNull TreeBuilder getBuilder() {
        return BasicTreeBuilder.INSTANCE;
    }

    @Override
    public @NotNull Set<String> getNames() {
        return names;
    }

    protected abstract @NotNull Map<String, Object> read(@NotNull InputStream input) throws IOException;

    protected abstract void write(@NotNull Map<String, Object> input, OutputStream output) throws IOException;
}