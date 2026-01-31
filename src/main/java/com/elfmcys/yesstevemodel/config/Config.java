package com.elfmcys.yesstevemodel.config;

import com.elfmcys.yesstevemodel.config.util.ConfigBuilder;
import com.elfmcys.yesstevemodel.config.util.IFormatter;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Config {
    private static Configuration config;

    public static void init(File configFile) {
        if (config != null) throw new IllegalStateException("Init have been performed!");
        config = new Configuration(configFile);
        readFromFile();
    }

    public static void readFromProp() {
        build(ConfigBuilder.startReadingFromProp(config));
    }

    public static void readFromFile() {
        build(ConfigBuilder.startReadingFromFile(config));
    }

    public static void save() {
        build(ConfigBuilder.startSaving(config));
    }

    private static void build(@Nonnull ConfigBuilder builder) {
        builder.setLangKeyPrefix("config.yes_steve_model");
        builder.setLangKeyFormatter(IFormatter.CAMEL_TO_SNAKE);
        GeneralConfig.build(builder);
        ExtraPlayerScreenConfig.build(builder);
        builder.finishBuilding();
    }

    public static Configuration getConfig() {
        return config;
    }

    @Nonnull
    public static List<IConfigElement> getRootConfigElements() {
        return config.getCategoryNames().stream()
                .map(config::getCategory)
                .map(ConfigElement::new)
                .collect(Collectors.toList());
    }
}
