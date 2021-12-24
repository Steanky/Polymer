package io.github.steanky.polymer.config.collection;

import io.github.steanky.polymer.config.ConfigElement;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link ConfigNode} based off of {@link HashMap}, with similar performance and other
 * characteristics. Notably, it has no guaranteed order for values, keys, or entries.
 */
public class HashConfigNode extends AbstractConfigNode {
    public HashConfigNode() {
        super(new HashMap<>());
    }

    public HashConfigNode(@NotNull Map<String, ConfigElement> mappings) {
        super(new HashMap<>(mappings));
    }
}
