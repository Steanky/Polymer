package io.github.steanky.polymer.config;

/**
 * Provides default implementations of {@link TreeBuilder}.
 */
public final class TreeBuilders {
    /**
     * The simplest {@link TreeBuilder}. Uses a mapper that returns the same object it is provided with no changes made.
     */
    public static final TreeBuilder SIMPLE = new BasicTreeBuilder();

}
