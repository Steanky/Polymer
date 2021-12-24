package io.github.steanky.polymer.config;

import io.github.steanky.polymer.config.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * TreeBuilder implementations are used to convert a "raw" deserialized {@link Map} to an equivalent {@link ConfigNode}
 * object, and vice versa. Some TreeBuilders may need to be specific to a configuration-format or deserializer.
 */
public interface TreeBuilder {
    @NotNull ConfigNode makeNode(@NotNull Map<String, Object> raw);

    @NotNull Map<String, Object> makeMap(@NotNull ConfigNode node);
}
