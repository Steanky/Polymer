package io.github.steanky.polymer.config;

import io.github.steanky.polymer.config.codec.CodecRegistry;
import io.github.steanky.polymer.config.codec.ConfigCodec;
import io.github.steanky.polymer.config.collection.ConfigNode;
import io.github.steanky.polymer.config.collection.LinkedConfigNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>A basic {@link ConfigBridge} implementation that uses a provided {@link ExecutorService} to execute read and write
 * operations asynchronously. Users should take care to only use this implementation in cases where a large amount of
 * data must be read at a time, otherwise, the benefit of using multithreading is negated by overhead.</p>
 */
public class AsyncFilesystemConfigBridge implements ConfigBridge {
    public static final Supplier<ConfigNode> DEFAULT_NODE_SUPPLIER = LinkedConfigNode::new;

    public static final FileFilter DEFAULT_FILE_FILTER = pathname -> {
        if(pathname.isDirectory()) {
            return true;
        }
        else {
            return CodecRegistry.INSTANCE.hasCodec(FilenameUtils.getExtension(pathname.getName()));
        }
    };

    public static final Function<File, String> DEFAULT_KEY_FUNCTION = file -> {
        if(file.isDirectory()) {
            return file.getName();
        }
        else {
            return FilenameUtils.getBaseName(file.getName());
        }
    };

    private final Path root;
    private final ExecutorService executorService;
    private final Supplier<ConfigNode> nodeSupplier;
    private final FileFilter fileFilter;
    private final Function<File, String> fileNameFunction;

    private record Node(File file, ConfigNode children) {}

    public AsyncFilesystemConfigBridge(@NotNull Path root, @NotNull ExecutorService executorService,
                                @NotNull Supplier<ConfigNode> nodeSupplier, @NotNull FileFilter fileFilter,
                                @NotNull Function<File, String> fileKeyFunction) {
        this.root = Validate.notNull(root);
        this.executorService = Validate.notNull(executorService);
        this.nodeSupplier = Validate.notNull(nodeSupplier);
        this.fileFilter = Validate.notNull(fileFilter);
        this.fileNameFunction = Validate.notNull(fileKeyFunction);
    }

    public AsyncFilesystemConfigBridge(@NotNull Path root, @NotNull ExecutorService executorService) {
        this(root, executorService, DEFAULT_NODE_SUPPLIER, DEFAULT_FILE_FILTER, DEFAULT_KEY_FUNCTION);
    }

    private ConfigNode readFile(File file) throws IOException {
        String extension = FilenameUtils.getExtension(file.getName());
        ConfigCodec codec = CodecRegistry.INSTANCE.getCodec(extension);
        if(codec != null) {
            return codec.getBuilder().makeNode(codec.decode(new FileInputStream(file), true));
        }

        //we can't find a codec to decode this file type, so just return the default node
        return nodeSupplier.get();
    }

    @Override
    public @NotNull Future<ConfigNode> read() {
        return executorService.submit(() -> {
            File rootFile = root.toFile();
            if(!fileFilter.accept(rootFile)) {
                //if the file doesn't match our filter, just return a new empty node
                return nodeSupplier.get();
            }

            if(!rootFile.isDirectory()) { //root isn't a directory, so try to parse this file
                return readFile(rootFile);
            }
            else { //root is a directory, so we attempt to recursively parse its files
                ConfigNode rootConfigNode = nodeSupplier.get();
                Deque<Node> stack = new ArrayDeque<>();
                stack.push(new Node(rootFile, rootConfigNode));

                Set<File> visited = Collections.newSetFromMap(new IdentityHashMap<>());
                visited.add(rootFile);

                while(!stack.isEmpty()) {
                    Node currentNode = stack.pop();

                    File[] subFiles = currentNode.file.listFiles(fileFilter);
                    if(subFiles != null) { //this should never be null
                        for(File subFile : subFiles) {
                            ConfigElement subElement;
                            if(subFile.isDirectory() && visited.add(subFile)) {
                                ConfigNode childNode = nodeSupplier.get();
                                subElement = childNode;
                                stack.push(new Node(subFile, childNode));
                            }
                            else {
                                subElement = readFile(subFile);
                            }

                            currentNode.children.put(fileNameFunction.apply(subFile), subElement);
                        }
                    }
                }

                return rootConfigNode;
            }
        });
    }

    @Override
    public @NotNull Future<Void> write(@NotNull ConfigNode node) throws IOException {
        return null;
    }

    @Override
    public boolean readOnly() {
        return false;
    }
}
