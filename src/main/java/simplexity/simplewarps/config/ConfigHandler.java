package simplexity.simplewarps.config;


import org.bukkit.configuration.file.FileConfiguration;
import simplexity.simplewarps.SimpleWarps;

public class ConfigHandler {
    private static ConfigHandler instance;

    private ConfigHandler() {
    }

    public static ConfigHandler getInstance() {
        if (instance == null) instance = new ConfigHandler();
        return instance;
    }

    private boolean mySqlEnabled;
    private String mySqlIp, mySqlName, mySqlUsername, mySqlPassword;

    public void reloadConfigValues(){
        SimpleWarps.getInstance().reloadConfig();
        FileConfiguration config = SimpleWarps.getInstance().getConfig();
        mySqlEnabled = config.getBoolean("mysql.enabled", false);
        mySqlIp = config.getString("mysql.ip", "localhost:3306");
        mySqlName = config.getString("mysql.name", "simplewarps");
        mySqlUsername = config.getString("mysql.username", "username1");
        mySqlPassword = config.getString("mysql.password", "badpassword!");
    }


    public boolean isMySqlEnabled() {
        return mySqlEnabled;
    }

    public String getMySqlIp() {
        return mySqlIp;
    }

    public String getMySqlName() {
        return mySqlName;
    }

    public String getMySqlUsername() {
        return mySqlUsername;
    }

    public String getMySqlPassword() {
        return mySqlPassword;
    }
}
