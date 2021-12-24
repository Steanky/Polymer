package io.github.steanky.polymer.config.collection;

import io.github.steanky.polymer.config.ConfigElement;
import io.github.steanky.polymer.config.ElementType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an ordered collection of {@link ConfigElement} objects. ConfigList implementations can be treated as
 * mutable {@link List} objects that do not support the addition of null elements.
 */
public interface ConfigList extends ConfigElement, List<ConfigElement> {
    @Override
    default @NotNull ConfigList asConfigList() {
        return this;
    }

    default @NotNull ElementType getType() {
        return ElementType.ARRAY;
    }
}
