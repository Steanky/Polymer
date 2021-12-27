package io.github.steanky.polymer.config.bridge;

import io.github.steanky.polymer.config.codec.ConfigCodec;
import io.github.steanky.polymer.config.collection.FileConfigNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public class SynchronousFilesystemBridge extends FilesystemBridgeAbstract {
    public SynchronousFilesystemBridge(@NotNull Path root,
                                 @Nullable FileFilter readFilter,
                                 @NotNull Function<File, ConfigCodec> codecFunction,
                                 @NotNull Function<File, String> fileKeyFunction) {
        super(root, readFilter, codecFunction, fileKeyFunction);
    }

    public SynchronousFilesystemBridge(@NotNull Path root) {
        this(root, DEFAULT_READ_FILTER, DEFAULT_CODEC_FUNCTION, DEFAULT_KEY_FUNCTION);
    }

    @Override
    protected Future<FileConfigNode> doReadOperation(@NotNull Callable<FileConfigNode> callable) throws Exception {
        return CompletableFuture.completedFuture(callable.call());
    }

    @Override
    protected Future<Void> doWriteOperation(@NotNull Callable<Void> callable) throws Exception {
        return CompletableFuture.completedFuture(callable.call());
    }
}
