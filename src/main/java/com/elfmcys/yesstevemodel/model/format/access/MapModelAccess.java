package com.elfmcys.yesstevemodel.model.format.access;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * 基于 Map 的模型包访问实现。
 */
public class MapModelAccess implements IModelAccess {
    private final Map<String, byte[]> data;

    public MapModelAccess(Map<String, byte[]> data) {
        this.data = data;
    }

    @Nullable
    @Override
    public byte[] readFile(String path) {
        return this.data.get(path);
    }

    @Override
    public boolean exists(String path) {
        return this.data.containsKey(path);
    }

    @Nonnull
    @Override
    public List<String> listFiles(String suffix) {
        List<String> result = Lists.newArrayList();
        for (String key : this.data.keySet()) {
            if (key.endsWith(suffix)) {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public void close() {
        // no-op
    }
}
