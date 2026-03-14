package com.elfmcys.yesstevemodel.command.sub;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.model.format.Type;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.RequestServerModelInfo;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ManageCommand extends CommandBase {
    private static final String MANAGE_NAME = "manage";

    @Nonnull
    @Override
    public String getName() {
        return MANAGE_NAME;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.yes_steve_model.manage.usage";
    }

    // 4 级命令，省去重写

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length != 0) throw new WrongUsageException(this.getUsage(sender));
        this.exportModel(sender);
    }

    private void exportModel(ICommandSender sender) throws CommandException {
        if (sender.canUseCommand(4, this.getName())) {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            List<RequestServerModelInfo.Info> builtinInfo = getFilesInfo(ServerModelManager.BUILTIN);
            List<RequestServerModelInfo.Info> customInfo = getFilesInfo(ServerModelManager.CUSTOM);
            List<RequestServerModelInfo.Info> authInfo = getFilesInfo(ServerModelManager.AUTH);
            NetworkHandler.sendToClientPlayer(new RequestServerModelInfo(builtinInfo, customInfo, authInfo), player);
        }
    }

    public static List<RequestServerModelInfo.Info> getFilesInfo(Path rootPath) {
        List<RequestServerModelInfo.Info> out = Lists.newArrayList();
        File folder = rootPath.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return out;
        }
        File[] files = rootPath.toFile().listFiles();
        if (files == null) return out;
        for (File file : files) {
            Type type = Type.getType(file);
            if (type == Type.UNKNOWN) continue;
            RequestServerModelInfo.Info info = new RequestServerModelInfo.Info(type.getFileName(file), type, FileUtils.sizeOf(file));
            out.add(info);
        }
        return out;
    }
}
