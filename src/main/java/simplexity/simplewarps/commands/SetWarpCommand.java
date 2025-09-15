package simplexity.simplewarps.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import simplexity.simplewarps.saving.Cache;
import simplexity.simplewarps.saving.SqlHandler;
import simplexity.simplewarps.warp.Warp;

public class SetWarpCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("setwarp")
                .requires(SetWarpCommand::canExecute)
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(SetWarpCommand::setWarp));

    }

    private static boolean canExecute(CommandSourceStack css) {
        CommandSender sender = css.getSender();
        if (!(sender instanceof Player player)) return false;
        //todo permission check
        return true;
    }

    private static int setWarp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = (Player) ctx.getSource().getSender();
        String name = StringArgumentType.getString(ctx, "name");
        //todo validate name/check for allowed characters
        //todo check permissions on if there's too many warps by this person already
        if (Cache.getWarpByName(name) != null) throw Exceptions.NAME_TAKEN.create(name);
        Location loc = player.getLocation();
        Warp warp = new Warp(
                name, loc, player.getUniqueId()
        );

        SqlHandler.getInstance().saveWarp(warp);
        Cache.addWarp(warp);
        player.sendMessage("Warp " + name + " set!");
        return 1;
    }

}
