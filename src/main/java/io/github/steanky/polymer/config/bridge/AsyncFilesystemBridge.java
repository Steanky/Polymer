package io.github.steanky.polymer.config.bridge;

import io.github.steanky.polymer.config.codec.ConfigCodec;
import io.github.steanky.polymer.config.collection.FileConfigNode;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

public class AsyncFilesystemBridge extends FilesystemBridgeAbstract {
    private final ExecutorService executorService;

    public AsyncFilesystemBridge(@NotNull Path root,
                                 @Nullable FileFilter readFilter,
                                 @NotNull Function<File, ConfigCodec> codecFunction,
                                 @NotNull Function<File, String> fileKeyFunction,
                                 @NotNull ExecutorService executorService) {
        super(root, readFilter, codecFunction, fileKeyFunction);
        this.executorService = Validate.notNull(executorService);
    }

    public AsyncFilesystemBridge(@NotNull Path root, @NotNull ExecutorService executorService) {
        this(root, DEFAULT_READ_FILTER, DEFAULT_CODEC_FUNCTION, DEFAULT_KEY_FUNCTION, executorService);
    }

    @Override
    protected Future<FileConfigNode> doReadOperation(@NotNull Callable<FileConfigNode> callable) {
        return executorService.submit(callable);
    }

    @Override
    protected Future<Void> doWriteOperation(@NotNull Callable<Void> callable) {
        return executorService.submit(callable);
    }
}
