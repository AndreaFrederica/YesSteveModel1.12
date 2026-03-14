package com.elfmcys.yesstevemodel.model.format.access;

import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FolderModelAccess implements IModelAccess {
    private final Path rootPath;

    public FolderModelAccess(Path rootPath) {
        this.rootPath = rootPath;
    }

    @Nullable
    @Override
    public byte[] readFile(String path) throws IOException {
        File file = this.rootPath.resolve(path).toFile();
        if (!file.isFile()) return null;
        return FileUtils.readFileToByteArray(file);
    }

    @Override
    public boolean exists(String path) {
        return this.rootPath.resolve(path).toFile().isFile();
    }

    @Nonnull
    @Override
    public List<String> listFiles(String suffix) {
        List<String> result = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(this.rootPath.toFile(), null, true);
        for (File file : files) {
            if (file.getName().endsWith(suffix)) {
                String relative = this.rootPath.toFile().toURI().relativize(file.toURI()).getPath();
                result.add(relative);
            }
        }
        return result;
    }

    @Override
    public void close() {
        // no-op for folder access
    }
}
