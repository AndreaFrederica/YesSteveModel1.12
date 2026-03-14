package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.model.format.access.FolderModelAccess;
import com.elfmcys.yesstevemodel.model.format.access.IModelAccess;
import com.elfmcys.yesstevemodel.model.format.access.MapModelAccess;
import com.elfmcys.yesstevemodel.model.format.access.ZipModelAccess;
import com.elfmcys.yesstevemodel.util.YesModelUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipFile;

/**
 * 文件形式
 */
public enum Type {
    FOLDER("folder") {
        @Override
        public boolean match(File file) {
            return file.isDirectory();
        }

        @Override
        public String addExtension(String fileName) {
            return fileName;
        }

        @Override
        public String getFileName(String fileName) {
            return fileName;
        }

        @Nonnull
        @Override
        public IModelAccess createAccess(File file) {
            return new FolderModelAccess(file.toPath());
        }
    },
    ZIP("zip") {
        @Nonnull
        @Override
        public IModelAccess createAccess(File file) throws IOException {
            return new ZipModelAccess(new ZipFile(file));
        }
    },
    SEVEN_Z("7z") {
        @Nullable
        @Override
        public IModelAccess createAccess(File file) {
            return null;
        }
    },
    //    SEVEN_Z("7z") {
//        @Nullable
//        @Override
//        public IModelAccess createAccess(File file) throws IOException {
//            Map<String, byte[]> data = Maps.newHashMap();
//            try (SevenZFile sevenZFile = new SevenZFile(file)) {
//                SevenZArchiveEntry entry;
//                while ((entry = sevenZFile.getNextEntry()) != null) {
//                    if (entry.isDirectory()) continue;
//                    int size = (int) entry.getSize();
//                    byte[] content = new byte[size];
//                    int bytesRead = 0;
//                    while (bytesRead < size) {
//                        int result = sevenZFile.read(content, bytesRead, size - bytesRead);
//                        if (result == -1) break;
//                        bytesRead += result;
//                    }
//                    data.put(entry.getName(), content);
//                }
//            }
//            if (data.isEmpty()) return null;
//            return new MapModelAccess(data);
//        }
//    },
    YSM("ysm") {
        @Nullable
        @Override
        public IModelAccess createAccess(File file) throws IOException {
            Map<String, byte[]> data = YesModelUtils.input(file);
            if (data.isEmpty()) return null;
            return new MapModelAccess(data);
        }
    },
    UNKNOWN("unknown") {
        @Override
        public boolean match(File file) {
            return false;
        }

        @Override
        public String addExtension(String fileName) {
            return "";
        }

        @Override
        public String getFileName(String fileName) {
            return "";
        }

        @Nullable
        @Override
        public IModelAccess createAccess(File file) {
            return null;
        }
    };

    private final String name;

    Type(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String addExtension(String fileName) {
        return fileName + "." + this.getName();
    }

    public boolean match(File file) {
        return FilenameUtils.isExtension(file.getName(), this.name);
    }

    /**
     * @return 去除拓展名
     */
    public String getFileName(File file) {
        return this.getFileName(file.getName());
    }

    /**
     * @return 去除拓展名
     */
    public String getFileName(String fileName) {
        return FilenameUtils.removeExtension(fileName);
    }

    /**
     * @return null 则表示无法加载
     */
    @Nullable
    public abstract IModelAccess createAccess(File file) throws IOException;

    @Nonnull
    public static Type getType(File file) {
        for (Type type : Type.values()) {
            if (type.match(file)) return type;
        }
        return UNKNOWN;
    }
}
