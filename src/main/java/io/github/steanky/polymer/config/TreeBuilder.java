package io.github.steanky.polymer.config;

import io.github.steanky.polymer.config.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

/**
 * TreeBuilder implementations are used to convert a "raw" deserialized {@link Map} to an equivalent {@link ConfigNode}
 * object, and vice versa. Some TreeBuilders may need to be specific to a configuration-format or deserializer.
 */
public interface TreeBuilder {
    <T extends ConfigNode> @NotNull T makeNode(@NotNull Map<String, Object> raw, @NotNull Supplier<T> nodeSupplier);

    <T extends Map<String, Object>> @NotNull T makeMap(@NotNull ConfigNode node, @NotNull Supplier<T> mapSupplier);
}
