package io.github.steanky.polymer.config;

import io.github.steanky.polymer.config.collection.*;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicTreeBuilder implements TreeBuilder {
    public static final BasicTreeBuilder INSTANCE = new BasicTreeBuilder();

    private BasicTreeBuilder() {}

    protected record Node<TOut>(Object inputContainer, BiConsumer<String, TOut> output) {}

    //returns true if object subclasses Map, Collection, or is an array; otherwise returns false
    private boolean isContainer(Object object) {
        return object != null && (object instanceof Map<?, ?> || object instanceof Collection<?> ||
                object.getClass().isArray());
    }

    protected <TMap extends Map<String, TOut>,
            TCollection extends Collection<TOut>,
            TOut> void processValue(@Nullable Object value,
                                    @NotNull Deque<Node<TOut>> stack,
                                    @NotNull Set<Object> visited,
                                    @NotNull Node<TOut> currentNode,
                                    @Nullable String keyString,
                                    @NotNull Supplier<TMap> mapSupplier,
                                    @NotNull Supplier<TCollection> collectionSupplier,
                                    @NotNull Function<Object, TOut> converter) {
        if(isContainer(value)) {
            if(visited.add(value)) {
                BiConsumer<String, TOut> consumer;
                Object output;

                if(value instanceof Map<?, ?>) {
                    TMap newMap = mapSupplier.get();
                    output = newMap;
                    consumer = newMap::put;
                }
                else  {
                    TCollection newCollection = collectionSupplier.get();
                    output = newCollection;
                    consumer = (k, v) -> newCollection.add(v);
                }

                stack.push(new Node<>(value, consumer));
                currentNode.output.accept(keyString, converter.apply(output));
            }
        }
        else {
            currentNode.output.accept(keyString, converter.apply(value));
        }
    }

    protected <TRootMap extends Map<String, TOut>,
            TSubMap extends Map<String, TOut>,
            TCollection extends Collection<TOut>,
            TOut> @NotNull TRootMap processMap(@NotNull Map<String, ?> input,
                                      @NotNull Supplier<TRootMap> rootMapSupplier,
                                      @NotNull Supplier<TSubMap> subMapSupplier,
                                      @NotNull Supplier<TCollection> collection,
                                      @NotNull Function<Object, TOut> converter) {
        TRootMap topLevel = rootMapSupplier.get();

        Deque<Node<TOut>> stack = new ArrayDeque<>();
        stack.push(new Node<>(input, topLevel::put));

        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        visited.add(topLevel);

        while(!stack.isEmpty()) {
            Node<TOut> node = stack.pop();

            if(node.inputContainer instanceof Map<?, ?> inputMap) {
                for(Map.Entry<?, ?> entry : inputMap.entrySet()) {
                    if(entry.getKey() instanceof String key) {
                        processValue(entry.getValue(), stack, visited, node, key, subMapSupplier, collection, converter);
                    }
                    else {
                        throw new IllegalArgumentException("key is not a string");
                    }
                }
            }
            else if(node.inputContainer instanceof Collection<?> inputCollection) {
                for(Object value : inputCollection) {
                    processValue(value, stack, visited, node, null, subMapSupplier, collection, converter);
                }
            }
            else {
                int length = Array.getLength(node.inputContainer);

                for(int i = 0; i < length; i++) {
                    Object value = Array.get(node.inputContainer, i);
                    processValue(value, stack, visited, node, null, subMapSupplier, collection, converter);
                }
            }
        }

        return topLevel;
    }

    @Override
    public <TNode extends ConfigNode> @NotNull TNode makeNode(@NotNull Map<String, Object> raw,
                                                              @NotNull Supplier<TNode> nodeSupplier) {
        Validate.notNull(raw);
        Validate.notNull(nodeSupplier);

        return processMap(raw, nodeSupplier, LinkedConfigNode::new, ArrayConfigList::new, value -> {
            if(value instanceof ConfigElement element) {
                return element;
            }
            else {
                //if value is of a type unrecognized by ConfigPrimitive, an exception will be thrown
                return new ConfigPrimitive(value);
            }
        });
    }

    @Override
    public <TMap extends Map<String, Object>> @NotNull TMap makeMap(@NotNull ConfigNode node,
                                                                    @NotNull Supplier<TMap> mapSupplier) {
        Validate.notNull(node);
        Validate.notNull(mapSupplier);

        return processMap(node, mapSupplier, LinkedHashMap::new, ArrayList::new, value -> {
            if(value instanceof ConfigPrimitive primitive) {
                return primitive.getObject();
            }
            else {
                return value;
            }
        });
    }
}