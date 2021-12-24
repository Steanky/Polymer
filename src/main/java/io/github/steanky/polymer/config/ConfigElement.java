package io.github.steanky.polymer.config;

import io.github.steanky.polymer.config.collection.ConfigList;
import io.github.steanky.polymer.config.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a particular value from a configuration file. More specific sub-interfaces are {@link ConfigNode} and
 * {@link ConfigList}. A direct implementation is {@link ConfigPrimitive}.
 */
public interface ConfigElement {
    /**
     * The null element. This object is singleton; however, code that wishes to check for null elements should check
     * the ElementType rather than performing a reference comparison against this field.
     */
    ConfigElement NULL = new ConfigPrimitive(null);

    /**
     * Converts this ConfigElement into a {@link ConfigNode}.
     * @return This element as a ConfigNode object
     * @throws IllegalStateException if this element is not a ConfigNode
     */
    default @NotNull ConfigNode asConfigNode() {
        throw new IllegalStateException("element may not be converted to ConfigNode");
    }

    /**
     * Converts this ConfigElement into a {@link ConfigList}.
     * @return This element as a ConfigList object
     * @throws IllegalStateException if this element is not a ConfigList
     */
    default @NotNull ConfigList asConfigList() {
        throw new IllegalStateException("element may not be converted to ConfigArray");
    }

    /**
     * Converts this ConfigElement into a string.
     * @return This element as a string
     * @throws IllegalStateException if this element is not a {@link ConfigPrimitive} containing a string
     */
    default @NotNull String asString() {
        throw new IllegalStateException("element may not be converted to String");
    }

    /**
     * Converts this ConfigElement into a Number.
     * @return This element as a Number
     * @throws IllegalStateException if this element cannot be converted into a Number
     */
    default @NotNull Number asNumber() {
        throw new IllegalStateException("element may not be converted to Number");
    }

    /**
     * Converts this ConfigElement into a boolean.
     * @return This element as a boolean
     * @throws IllegalStateException if this element cannot be converted into a boolean
     */
    default boolean asBoolean() {
        throw new IllegalStateException("element may not be converted to boolean");
    }

    /**
     * Obtains the {@link ElementType} of this ConfigElement. This can be used in conjunction with
     * {@link ConfigElement#asConfigNode()}, {@link ConfigElement#asConfigList()}, {@link ConfigElement#asString()},
     * {@link ConfigElement#asNumber()}, or {@link ConfigElement#asBoolean()} in order to safely convert ConfigElement
     * instances into more specific implementations or types.
     * @return the ElementType representing the type of this ConfigElement
     */
    @NotNull ElementType getType();
}
