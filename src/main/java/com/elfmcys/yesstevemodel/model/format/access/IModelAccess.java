package com.elfmcys.yesstevemodel.model.format.access;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * 统一的模型包文件访问接口。
 */
public interface IModelAccess extends Closeable {
    /**
     * 读取指定路径的文件内容。
     *
     * @param path 相对于模型包根目录的路径，如 "models/main.json"
     * @return 文件字节内容，不存在则返回 null
     */
    @Nullable
    byte[] readFile(String path) throws IOException;

    /**
     * 检查指定路径的文件是否存在。
     */
    boolean exists(String path);

    /**
     * 列出所有匹配指定后缀的文件路径。
     *
     * @param suffix 文件后缀，如 ".png"
     * @return 相对路径列表
     */
    @Nonnull
    List<String> listFiles(String suffix) throws IOException;
}
