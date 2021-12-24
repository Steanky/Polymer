package io.github.steanky.polymer.config.codec;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.github.steanky.polymer.config.ConfigParseException;
import io.github.steanky.polymer.config.TreeBuilder;
import io.github.steanky.polymer.config.TreeBuilders;
import io.github.steanky.polymer.config.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public class TomlCodec implements ConfigCodec {
    public static final TomlCodec INSTANCE = new TomlCodec();

    private static final Set<String> NAMES = Set.of("toml");
    private static final TomlWriter WRITER = new TomlWriter();

    private TomlCodec() {
        CodecRegistry.INSTANCE.registerCodec(this);
    }

    @Override
    public @NotNull Map<String, Object> decode(@NotNull InputStream stream, boolean close) throws IOException {
        try {
            return new Toml().read(stream).toMap();
        }
        catch (IllegalStateException illegalStateException) {
            throw new ConfigParseException(illegalStateException);
        }
        finally {
            if(close) {
                stream.close();
            }
        }
    }

    @Override
    public void encode(@NotNull OutputStream outputStream, @NotNull ConfigNode node, boolean close) throws IOException {
        try {
            //this works because ConfigNode implements Map, so we can just write it directly
            WRITER.write(node, outputStream);
        }
        finally {
            if(close) {
                outputStream.close();
            }
        }
    }

    @Override
    public @NotNull TreeBuilder getBuilder() {
        return TreeBuilders.SIMPLE;
    }

    @Override
    public @NotNull Set<String> getNames() {
        return NAMES;
    }
}
