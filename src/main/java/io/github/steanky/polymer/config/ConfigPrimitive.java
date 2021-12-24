package io.github.steanky.polymer.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a "primitive" type. A type is considered "primitive" if and only if it subclasses
 * {@link String}, {@link Number}, {@link Boolean}, or is a null value. Therefore, all Java primitives except for
 * char (and with the addition of String) are compatible. The exclusion of char is due to the inconsistent support for
 * primitive characters in some config formats; for example, many will use single-character strings rather than proper
 * chars.
 */
public class ConfigPrimitive implements ConfigElement {
    private final Object object;
    private final ElementType type;

    /**
     * Creates a new ConfigPrimitive instance wrapping the provided {@link Object}. The object may only subclass one of
     * a number of restricted types; otherwise, an {@link IllegalArgumentException} will be thrown.
     * @param object the object to wrap
     * @throws IllegalArgumentException if the provided object is a type other than a String, Number, or boolean, and is
     * not null
     */
    public ConfigPrimitive(@Nullable Object object) {
        if (object instanceof String) {
            this.type = ElementType.STRING;
        }
        else if (object instanceof Number) {
            this.type = ElementType.NUMBER;
        }
        else if (object instanceof Boolean) {
            this.type = ElementType.BOOLEAN;
        }
        else if (object == null) {
            this.type = ElementType.NULL;
        }
        else {
            throw new IllegalArgumentException("objects of type " + object.getClass().getName() +
                    "cannot be converted into a ConfigPrimitive");
        }

        this.object = object;
    }

    private <T> T convert(ElementType elementType, Class<T> classType) {
        if(type == elementType) {
            return classType.cast(object);
        }

        throw new IllegalStateException("element may not be converted to a " + classType.getSimpleName());
    }

    @Override
    public @NotNull String asString() {
        return convert(ElementType.STRING, String.class);
    }

    @Override
    public @NotNull Number asNumber() {
        return convert(ElementType.NUMBER, Number.class);
    }

    @Override
    public boolean asBoolean() {
        return convert(ElementType.BOOLEAN, Boolean.class);
    }

    @Override
    public @NotNull ElementType getType() {
        return type;
    }

    /**
     * Returns the object wrapped by this ConfigPrimitive.
     * @return The object wrapped by this ConfigPrimitive, or null
     */
    public @Nullable Object getObject() {
        return object;
    }
}