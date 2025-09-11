package simplexity.simplewarps;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleWarps extends JavaPlugin {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static SimpleWarps instance;


    @Override
    public void onEnable() {
        instance = this;
        handleConfig();
        registerCommands();
        registerListeners();
    }

    public static SimpleWarps getInstance() {
        return instance;
    }

    public static MiniMessage getMiniMessage() {
        return miniMessage;
    }

    private void registerCommands() {
    }

    private void registerListeners() {
    }

    private void handleConfig() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}
