package io.github.steanky.polymer.config.bridge;

import io.github.steanky.polymer.config.ConfigElement;
import io.github.steanky.polymer.config.codec.CodecRegistry;
import io.github.steanky.polymer.config.codec.ConfigCodec;
import io.github.steanky.polymer.config.collection.ConfigNode;
import io.github.steanky.polymer.config.collection.FileConfigNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;

public abstract class FilesystemBridgeAbstract implements ConfigBridge<FileConfigNode> {
    /**
     * The default FileFilter, which only accepts directories, as well as files whose extension has a registered codec.
     * This filter is applied during reads from the ConfigBridge.
     */
    public static final FileFilter DEFAULT_READ_FILTER = pathname -> {
        if(pathname.isDirectory()) {
            return true;
        }
        else {
            return CodecRegistry.INSTANCE.hasCodec(FilenameUtils.getExtension(pathname.getName()));
        }
    };

    /**
     * The default key function, whose returned keys are just the file's name (without any extension).
     */
    public static final Function<File, String> DEFAULT_KEY_FUNCTION = file -> FilenameUtils.getBaseName(file.getName());

    /**
     * The default codec function, which searches the codec registry using the file's extension, and returns null for
     * directories (as they can have no actual codec).
     */
    public static final Function<File, ConfigCodec> DEFAULT_CODEC_FUNCTION = file -> {
        if(!file.isDirectory()) {
            //use the file extension to determine what codec to use
            return CodecRegistry.INSTANCE.getCodec(FilenameUtils.getExtension(file.getName()));
        }

        //no codec for directories!
        return null;
    };

    private final Path root;
    private final FileFilter readFilter;
    private final Function<File, ConfigCodec> codecFunction;
    private final Function<File, String> fileKeyFunction;

    private record InputNode(File file, ConfigNode children) {}

    private record OutputNode(FileConfigNode node, Path path) {}

    public FilesystemBridgeAbstract(@NotNull Path root,
                                    @Nullable FileFilter readFilter,
                                    @NotNull Function<File, ConfigCodec> codecFunction,
                                    @NotNull Function<File, String> fileKeyFunction) {
        this.root = Validate.notNull(root);
        this.readFilter = readFilter;
        this.codecFunction = Validate.notNull(codecFunction);
        this.fileKeyFunction = Validate.notNull(fileKeyFunction);
    }

    private @NotNull FileConfigNode readFile(@NotNull File file) throws IOException {
        //directories are handled specially, not by this function
        Validate.isTrue(!file.isDirectory(), "cannot read from a directory");

        ConfigCodec codec = codecFunction.apply(file);
        if(codec != null) {
            return codec.getBuilder().makeNode(codec.decode(new FileInputStream(file), true),
                    () -> new FileConfigNode(codec));
        }

        //couldn't find a codec to decode this file, this is an error (users should filter out files they can't read)
        throw new IllegalArgumentException("unable to find a codec for file " + file);
    }

    private void writeFile(@NotNull File file, @NotNull FileConfigNode node) throws IOException {
        if(!node.isDirectory()) {
            ConfigCodec codec = node.getCodec();
            codec.encode(codec.getBuilder().makeMap(node, LinkedHashMap::new), new FileOutputStream(file), true);
        }
        else {
            throw new IllegalArgumentException("cannot write a directory node");
        }
    }

    @Override
    public @NotNull Future<FileConfigNode> read() throws IOException {
        try {
            return doReadOperation(() -> {
                File rootFile = root.toFile();

                if(readFilter != null && !readFilter.accept(rootFile)) {
                    //return null if the filter exists and doesn't accept our root
                    return null;
                }
                else if(!rootFile.isDirectory()) {
                    //root isn't a directory, so read only the root and nothing else
                    return readFile(rootFile);
                }
                else {
                    //root is a directory, so we need to iterate the directory tree
                    FileConfigNode rootConfigNode = new FileConfigNode();

                    Deque<InputNode> stack = new ArrayDeque<>();
                    stack.push(new InputNode(rootFile, rootConfigNode));

                    //handles recursive file structures by only processing each directory once
                    Set<File> visited = new HashSet<>();
                    visited.add(rootFile);

                    while(!stack.isEmpty()) {
                        InputNode currentNode = stack.pop();

                        File[] subFiles = currentNode.file.listFiles(readFilter);
                        if(subFiles != null) { //this should never be null, currentNode.file must be a directory
                            for(File subFile : subFiles) {
                                if(subFile.isDirectory()) {
                                    if(visited.add(subFile)) {
                                        //use directory node here as well
                                        ConfigNode childNode = new FileConfigNode();
                                        stack.push(new InputNode(subFile, childNode));
                                        currentNode.children.put(fileKeyFunction.apply(subFile), childNode);
                                    }
                                }
                                else {
                                    currentNode.children.put(fileKeyFunction.apply(subFile), readFile(subFile));
                                }
                            }
                        }
                    }

                    return rootConfigNode;
                }
            });
        }
        catch (Exception exception) {
            //rethrow any exceptions as IOExceptions
            throw new IOException(exception);
        }
    }

    @Override
    public @NotNull Future<Void> write(@NotNull FileConfigNode node) throws IOException {
        Validate.notNull(node);

        try {
            return doWriteOperation(() -> {
                File rootFile = root.toFile();

                if(!node.isDirectory()) {
                    //assume rootFile is a non-directory since node is not a directory either
                    //an exception will be thrown here if this is not the case, indicating user error
                    writeFile(rootFile, node);
                }
                else {
                    Deque<OutputNode> stack = new ArrayDeque<>();
                    stack.push(new OutputNode(node, root));

                    Set<Object> visited = new HashSet<>();
                    visited.add(node);

                    while(!stack.isEmpty()) {
                        OutputNode currentNode = stack.pop();

                        for(Map.Entry<String, ConfigElement> childEntry : currentNode.node.entrySet()) {
                            //cast should always succeed: we only push FileConfigNode instances that are DIRECTORIES onto
                            //the stack, and directories are guaranteed to only contain other FileConfigNode instances as
                            //per the additional restrictions placed on put() for that class
                            FileConfigNode childNode = (FileConfigNode)childEntry.getValue().asConfigNode();

                            if(childNode.isDirectory() && visited.add(childNode)) {
                                //node is a directory we haven't visited yet
                                stack.push(new OutputNode(childNode, root.resolve(childEntry.getKey())));
                            }
                            else {
                                //not a directory, so write to the filesystem
                                writeFile(currentNode.path.toFile(), currentNode.node);
                            }
                        }
                    }
                }

                return null;
            });
        }
        catch (Exception exception) {
            throw new IOException(exception);
        }
    }

    @Override
    public boolean readOnly() {
        return false;
    }

    protected abstract Future<FileConfigNode> doReadOperation(@NotNull Callable<FileConfigNode> callable) throws Exception;

    protected abstract Future<Void> doWriteOperation(@NotNull Callable<Void> callable) throws Exception;
}