package com.elfmcys.yesstevemodel.command;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.IClientCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClientCacheCommand extends CommandBase implements IClientCommand {
    @Nonnull
    @Override
    public String getName() {
        return "ysmclient";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.yes_steve_model.client.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length != 2 || !"cache".equalsIgnoreCase(args[0]) || !"dump".equalsIgnoreCase(args[1])) {
            throw new WrongUsageException(this.getUsage(sender));
        }

        ClientModelManager.CacheExportResult result = ClientModelManager.exportAllCachedModels();
        if (result.isSuccess()) {
            sender.sendMessage(new TextComponentTranslation(
                    "commands.yes_steve_model.client.cache.dump.success",
                    result.getExportedCount(),
                    result.getFilePath()
            ));
            return;
        }

        sender.sendMessage(new TextComponentTranslation(result.getMessageKey()));
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Collections.singletonList("cache"));
        }
        if (args.length == 2 && "cache".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, Collections.singletonList("dump"));
        }
        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Arrays.asList("ysmc");
    }
}