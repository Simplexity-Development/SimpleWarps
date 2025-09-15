package simplexity.simplewarps;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import simplexity.simplewarps.commands.WarpCommand;
import simplexity.simplewarps.config.ConfigHandler;
import simplexity.simplewarps.saving.Cache;
import simplexity.simplewarps.saving.SqlHandler;

import java.sql.SQLException;

public final class SimpleWarps extends JavaPlugin {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static SimpleWarps instance;


    @Override
    public void onEnable() {
        instance = this;
        handleConfig();
        try {
            SqlHandler.getInstance().initialize();
        } catch (SQLException e) {
            getLogger().info("Exception: " + e.getMessage());
        }
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(WarpCommand.createCommand().build()));
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
        ConfigHandler.getInstance().reloadConfigValues();
    }

    @Override
    public void onDisable(){
        Cache.clear();
        SqlHandler.getInstance().closeDatabase();
    }
}
