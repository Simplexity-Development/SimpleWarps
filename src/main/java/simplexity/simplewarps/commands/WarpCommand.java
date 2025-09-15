package simplexity.simplewarps.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import simplexity.simplewarps.saving.Cache;
import simplexity.simplewarps.saving.SqlHandler;
import simplexity.simplewarps.warp.Warp;

public class WarpCommand {


    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("warp")
                .requires(src -> src.getSender() instanceof Player)
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(WarpCommand::teleportWarp)
                )
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(WarpCommand::deleteWarp)
                        )
                )
                .then(Commands.literal("modify")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(WarpCommand::modifyWarp)
                        )
                );
    }

    private static int teleportWarp(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        String name = StringArgumentType.getString(ctx, "name");
        Warp warp = Cache.getWarpByName(name);

        if (warp == null) {
            player.sendMessage("Warp not found!");
            return 0;
        }

        Location loc = warp.getLocation();
        player.teleportAsync(loc);
        player.sendMessage("Teleported to warp " + warp.getName() + "!");
        return 1;
    }


    private static int deleteWarp(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        String name = StringArgumentType.getString(ctx, "name");
        Warp warp = Cache.getWarpByName(name);
        if (warp == null) {
            player.sendMessage("Warp not found!");
            return 0;
        }

        SqlHandler.getInstance().deleteWarp(warp.getWarpId());
        Cache.removeWarp(warp.getWarpId());
        player.sendMessage("Warp " + name + " deleted!");
        return 1;
    }

    private static int modifyWarp(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        String name = StringArgumentType.getString(ctx, "name");

        Warp warp = Cache.getWarpByName(name);
        if (warp == null) {
            player.sendMessage("Warp not found!");
            return 0;
        }

        Location loc = player.getLocation();
        warp.setLocation(loc);
        SqlHandler.getInstance().saveWarp(warp);
        Cache.addWarp(warp);

        player.sendMessage("Warp " + name + " updated!");
        return 1;
    }
}
