package simplexity.simplewarps.commands;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import simplexity.simplewarps.SimpleWarps;

public class Exceptions {

    private static final MiniMessage miniMessage = SimpleWarps.getMiniMessage();

    public static final DynamicCommandExceptionType NAME_TAKEN = new DynamicCommandExceptionType(
            warpName -> MessageComponentSerializer.message().serialize(
                    miniMessage.deserialize(
                            "<name> IS ALREADY TAKEN",
                            Placeholder.unparsed("name", warpName.toString())
                    )
            )
    );
}
