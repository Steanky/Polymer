package io.github.steanky.polymer.config;

import io.github.steanky.polymer.config.collection.ArrayConfigList;
import io.github.steanky.polymer.config.collection.ConfigNode;
import io.github.steanky.polymer.config.collection.LinkedConfigNode;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>This is the simplest implementation of {@link TreeBuilder}, which is capable of handling most if not all
 * deserializers.</p>
 *
 * <p>When converting an input map to a node using {@link BasicTreeBuilder#makeNode(Map)}, all subclasses of
 * {@link Collection} and {@link Map}, when encountered, will be recursively iterated and converted into
 * {@link io.github.steanky.polymer.config.collection.ConfigList} and {@link ConfigNode} objects, respectively.</p>
 *
 * <p>When converting an input node to a map, all ConfigList and ConfigNode objects will be transformed back into
 * equivalent Java types in the returned map.</p>
 *
 * <p>This implementation uses insertion order maps.</p>
 */
public class BasicTreeBuilder implements TreeBuilder {
    private final BiFunction<Object, Boolean, Object> mapper;

    /**
     * Construct a new BasicTreeBuilder using the default mapper, which does nothing. Users should consider using
     * {@link TreeBuilders#SIMPLE} instead of creating a new instance using this constructor.
     */
    public BasicTreeBuilder() {
        this.mapper = (object, serialize) -> object;
    }

    /**
     * Construct a new BasicTreeBuilder using the provided mapper, which will be applied to every input value in the
     * configuration hierarchy. This can be used to convert the types outputted by a specific deserializer to something
     * expected by this builder.
     * @param mapper The mapper to use, which cannot be null. The first parameter is the object to convert; the second
     *               parameter will be true if the object is being deserialized (loaded) and false if it's being
     *               serialized (saved)
     * @throws NullPointerException if the mapper is null
     */
    public BasicTreeBuilder(@NotNull BiFunction<Object, Boolean, Object> mapper) {
        this.mapper = Validate.notNull(mapper);
    }

    /**
     * Used to keep track of various objects while traversing an input map.
     */
    protected record Node<TInput, TOutput>(Map<String, TInput> inputMap,
                                         Collection<TInput> inputCollection,
                                         BiConsumer<String, TOutput> output) { }

    @SuppressWarnings("unchecked")
    protected <TInput, TOutput> void processValue(Object value, Deque<Node<TInput, TOutput>> stack, Set<Object> visited,
                                                Node<TInput, TOutput> currentNode, String keyString,
                                                Supplier<Map<String, TOutput>> mapSupplier,
                                                Supplier<Collection<TOutput>> collectionSupplier,
                                                Function<TInput, TOutput> objectCreator, boolean deserialize) {
        //used to convert objects to a type we can handle. the output of some deserializers may differ: for example, one
        //may produce arrays, which must be (deeply!) converted to a Collection using a custom mapper. note that the
        //default mapper does not try to convert anything; this is suitable for most well-behaved deserializers
        value = this.mapper.apply(value, deserialize);

        //deeply traverse maps and collections. self-referential data is handled by only processing each collection once
        if(value instanceof Map<?, ?> map) {
            if(visited.add(map)) {
                Map<String, TOutput> newMap = mapSupplier.get();

                //this cast is safe-each key and value of 'map' will be iterated and effectively type-checked
                stack.push(new Node<>((Map<String, TInput>)map, null, newMap::put));

                //this cast is also safe-TOutput is always either ConfigElement or Map<String, Object>
                currentNode.output.accept(keyString, (TOutput)newMap);
            }
        }
        else if(value instanceof Collection<?> collection) {
            if(visited.add(collection)) {
                Collection<TOutput> newCollection = collectionSupplier.get();
                stack.push(new Node<>(null, (Collection<TInput>)collection,
                        (string, element) -> newCollection.add(element)));
                currentNode.output.accept(keyString, (TOutput)newCollection);
            }
        }
        else {
            //try to convert the object. it will not be added to the visited set
            currentNode.output.accept(keyString, objectCreator.apply((TInput)value));
        }
    }

    protected <TInput, TOutput> Map<String, TOutput> processMapping(Map<String, TInput> input,
                                                                  Supplier<Map<String, TOutput>> mapSupplier,
                                                                  Supplier<Collection<TOutput>> collectionSupplier,
                                                                  Function<TInput, TOutput> objectSupplier,
                                                                  boolean deserialize) {
        Map<String, TOutput> topLevel = mapSupplier.get();

        //used to deal with circular references
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        visited.add(topLevel);

        //keep track of nodes we haven't explored
        Deque<Node<TInput, TOutput>> stack = new ArrayDeque<>();

        //push first node (which is the top level of the tree)
        stack.push(new Node<>(input, null, topLevel::put));

        while(!stack.isEmpty()) {
            Node<TInput, TOutput> currentNode = stack.pop();

            if(currentNode.inputMap != null) {
                for(Map.Entry<?, ?> entry : currentNode.inputMap.entrySet()) {
                    if(entry.getKey() instanceof String keyString) {
                        //key type good, process the value now
                        processValue(entry.getValue(), stack, visited, currentNode, keyString, mapSupplier,
                                collectionSupplier, objectSupplier, deserialize);
                    }
                    else {
                        //no reasonable way to handle this case? toString() is an option, but probably better to
                        //fail-fast instead of attempting to make sense of likely broken input
                        throw new IllegalStateException("map key must be a string");
                    }
                }
            }
            else { //we must be dealing with a list
                for(Object listElement : currentNode.inputCollection) {
                    processValue(listElement, stack, visited, currentNode, null, mapSupplier,
                            collectionSupplier, objectSupplier, deserialize);
                }
            }
        }

        return topLevel;
    }

    @Override
    public @NotNull ConfigNode makeNode(@NotNull Map<String, Object> raw) {
        Validate.notNull(raw);

        //use LinkedConfigNode to preserve insertion order
        return (ConfigNode) processMapping(raw, LinkedConfigNode::new, ArrayConfigList::new, ConfigPrimitive::new,
                true);
    }

    @Override
    public @NotNull Map<String, Object> makeMap(@NotNull ConfigNode node) {
        Validate.notNull(node);
        return processMapping(node, LinkedHashMap::new, ArrayList::new, (obj) -> ((ConfigPrimitive)obj).getObject(),
                false);
    }
}