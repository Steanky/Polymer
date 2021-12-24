package io.github.steanky.polymer.config.collection;

import io.github.steanky.polymer.config.ConfigElement;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An implementation of {@link ConfigList} based off of {@link ArrayList}, with similar performance and other
 * characteristics.
 */
public class ArrayConfigList extends AbstractConfigList implements RandomAccess {
    /**
     * Constructs a new ArrayConfigList backed by an empty {@link ArrayList}.
     */
    public ArrayConfigList() {
        super(new ArrayList<>());
    }

    /**
     * Constructs a new ArrayConfigList backed an {@link ArrayList} containing the same elements as the provided
     * {@link Collection}.
     * @param collection The collection to copy elements from
     */
    public ArrayConfigList(@NotNull Collection<ConfigElement> collection) {
        super(new ArrayList<>(Validate.notNull(collection)));
    }
}