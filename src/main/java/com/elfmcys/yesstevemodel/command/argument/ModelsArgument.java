package com.elfmcys.yesstevemodel.command.argument;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class ModelsArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Collections.singleton("default");

    private ModelsArgument() {
    }

    public static ModelsArgument ids() {
        return new ModelsArgument();
    }

    public static String getModel(CommandContext<CommandSource> context, String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    @Keep
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    @Keep
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> source, SuggestionsBuilder builder) {
        if (source.getSource() instanceof ISuggestionProvider) {
            if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                return ISuggestionProvider.suggest(ServerModelManager.CACHE_NAME_INFO.keySet(), builder);
            } else {
                return ISuggestionProvider.suggest(ClientModelManager.MODELS.keySet().stream().map(ResourceLocation::getPath), builder);
            }
        } else {
            return Suggestions.empty();
        }
    }

    @Override
    @Keep
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
