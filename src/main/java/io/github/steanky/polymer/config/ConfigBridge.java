package io.github.steanky.polymer.config;

import io.github.steanky.polymer.config.collection.ConfigNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Reads and writes ConfigNode objects to and from an implementation-defined source. This source may be the network,
 * file IO, memory, a database, etc. Synchronicity is also implementation-specific: {@link Future} is used to provide
 * a common interface between synchronous and non-synchronous usage.
 */
public interface ConfigBridge {
    /**
     * Loads a {@link ConfigNode} object from this loader's source. Asynchronous implementations may choose to load
     * ConfigNode objects on another thread; in which case this method should return immediately.
     * @return A {@link Future} object which will contain a ConfigNode object when it has finished loading, and can be
     * used to query or await the completion of the read task
     * @throws IOException if an IO error occurs
     */
    @NotNull Future<ConfigNode> read() throws IOException;

    /**
     * Writes a {@link ConfigNode} object to this loader's source. This operation may occur asynchronously in some
     * implementations, in which case this method should return immediately. Write operations may not be supported on
     * all implementations.
     * @param node The node to write to the source
     * @return A {@link Future} object, which may be used to query or await the completion of the write task
     * @throws IOException if an IO error occurs
     * @throws IllegalStateException if the bridge does not support writing at this current time
     */
    @NotNull Future<Void> write(@NotNull ConfigNode node) throws IOException;

    /**
     * Used to query if this ConfigBridge supports writes as well as reads.
     * @return true if this object is read-only (does not support writing); false otherwise
     */
    boolean readOnly();
}
