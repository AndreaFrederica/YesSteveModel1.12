package com.elfmcys.yesstevemodel.model.format.access;

import com.elfmcys.yesstevemodel.util.InputStreamUtils;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipModelAccess implements IModelAccess {
    private final ZipFile zipFile;

    public ZipModelAccess(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    @Nullable
    @Override
    public byte[] readFile(String path) throws IOException {
        ZipEntry entry = this.zipFile.getEntry(path);
        if (entry == null) return null;
        try (InputStream stream = this.zipFile.getInputStream(entry)) {
            return InputStreamUtils.toBytes(stream);
        }
    }

    @Override
    public boolean exists(String path) {
        return this.zipFile.getEntry(path) != null;
    }

    @Nonnull
    @Override
    public List<String> listFiles(String suffix) {
        List<String> result = Lists.newArrayList();
        this.zipFile.stream().forEach(entry -> {
            if (!entry.isDirectory() && entry.getName().endsWith(suffix)) {
                result.add(entry.getName());
            }
        });
        return result;
    }

    @Override
    public void close() throws IOException {
        this.zipFile.close();
    }
}
