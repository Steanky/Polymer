package io.github.steanky.polymer.config.collection;

import io.github.steanky.polymer.config.ConfigElement;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * <p>Contains functionality and methods common to many {@link ConfigList} implementations. This abstract class does not
 * define any abstract methods. Its main use is to enable concrete implementations to specify what kind of list they'd
 * like to use.</p>
 *
 * <p>This class, much like {@link AbstractConfigNode}, is package-private because it would be trivial to violate
 * ConfigList's non-nullability specification otherwise. For example, a third-party subclass could unknowingly construct
 * an implementation of this class that passes a list to its superclass which is also accessible elsewhere, and could
 * potentially have null values added to it after construction.</p>
 */
abstract class AbstractConfigList extends AbstractList<ConfigElement> implements ConfigList {
    private final List<ConfigElement> list;

    AbstractConfigList(@NotNull List<ConfigElement> list) {
        this.list = Validate.noNullElements(list);
    }

    @Override
    public boolean add(@NotNull ConfigElement element) {
        Validate.notNull(element);
        return list.add(element);
    }

    @Override
    public void add(int index, @NotNull ConfigElement element) {
        Validate.notNull(element);
        list.add(index, element);
    }

    @Override
    public @NotNull ConfigElement set(int index, @NotNull ConfigElement element) {
        Validate.notNull(element);
        return list.set(index, element);
    }

    @Override
    public @NotNull ConfigElement remove(int index) {
        return list.remove(index);
    }

    @Override
    public @NotNull ConfigElement get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }
}