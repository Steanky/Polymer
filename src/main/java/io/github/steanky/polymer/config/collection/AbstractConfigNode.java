package io.github.steanky.polymer.config.collection;

import io.github.steanky.polymer.config.ConfigElement;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * <p>Contains functionality and methods common to many {@link ConfigNode} implementations. This abstract class does not
 * define any abstract methods. Its main use is to enable concrete implementations to specify what kind of map they'd
 * like to use.</p>
 *
 * <p>See {@link AbstractConfigList} for an explanation of the reasoning behind why this class was made
 * package-private.</p>
 */
abstract class AbstractConfigNode extends AbstractMap<String, ConfigElement> implements ConfigNode {
    final Map<String, ConfigElement> mappings;

    AbstractConfigNode(@NotNull Map<String, ConfigElement> mappings) {
        Validate.notNull(mappings);

        Validate.noNullElements(mappings.keySet());
        Validate.noNullElements(mappings.values());

        this.mappings = mappings;
    }

    protected void validateKeyValuePair(@NotNull String key, @NotNull ConfigElement value) {
        Validate.notNull(key);
        Validate.notNull(value);
    }

    @Override
    public ConfigElement get(Object key) {
        Validate.notNull(key);
        return mappings.get(key);
    }

    @Override
    public ConfigElement put(@NotNull String key, @NotNull ConfigElement value) {
        validateKeyValuePair(key, value);
        return mappings.put(key, value);
    }

    @Override
    public boolean containsKey(Object key) {
        Validate.notNull(key);
        return mappings.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        Validate.notNull(value);
        return mappings.containsValue(value);
    }

    @Override
    public ConfigElement remove(Object key) {
        Validate.notNull(key);
        return mappings.remove(key);
    }

    @Override
    public void clear() {
        mappings.clear();
    }

    @NotNull
    @Override
    public Set<Entry<String, ConfigElement>> entrySet() {
        return mappings.entrySet();
    }

    @Override
    public @NotNull Optional<ConfigElement> getElement(@NotNull String... keys) {
        Validate.noNullElements(keys);
        Validate.inclusiveBetween(1, Integer.MAX_VALUE, keys.length);

        if(keys.length == 1) { //simplest case, just return directly from our map
            return Optional.ofNullable(mappings.get(keys[0]));
        }
        else { //iterate through the provided keys
            ConfigNode current = this;
            int lastIndex = keys.length - 1;
            for(int i = 0; i < keys.length; i++) {
                Optional<ConfigElement> childOptional = current.getElement(keys[i]);

                if(i == lastIndex) { //we got to the last key, so return whatever we find
                    return childOptional;
                }
                else if(childOptional.isPresent()) {
                    ConfigElement childElement = childOptional.get();
                    if(childElement.getType().isNode()) { //continue traversing nodes...
                        current = childElement.asConfigNode();
                    }
                    else { //if we still have nodes to traverse, but ran into something that's NOT a node, return
                        return Optional.empty();
                    }
                }
                else { //there is no element here, return
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }
}
