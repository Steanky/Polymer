package io.github.steanky.polymer.config.codec;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.github.steanky.polymer.config.ConfigParseException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

class TomlCodec extends AbstractConfigCodec {
    private final TomlWriter writer;

    TomlCodec() {
        super(Set.of("toml"));
        writer = new TomlWriter();
    }

    @Override
    protected @NotNull Map<String, Object> read(@NotNull InputStream input) throws IOException {
        try {
            return new Toml().read(input).toMap();
        }
        catch (IllegalStateException exception) {
            throw new ConfigParseException(exception);
        }
    }

    @Override
    protected void write(@NotNull Map<String, Object> input, OutputStream output) throws IOException {
        writer.write(input, output);
    }
}