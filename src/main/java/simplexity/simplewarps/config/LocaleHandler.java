package simplexity.simplewarps.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import simplexity.simplewarps.SimpleWarps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocaleHandler {
    private static LocaleHandler instance;
    private final String fileName = "locale.yml";
    private final File dataFile = new File(SimpleWarps.getInstance().getDataFolder(), fileName);
    private FileConfiguration locale = new YamlConfiguration();
    private final Logger logger = SimpleWarps.getInstance().getSLF4JLogger();

    private LocaleHandler() {
        try {
            Path dataFilePath = dataFile.toPath();
            Path parentDirectory = dataFilePath.getParent();

            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }
            if (Files.notExists(dataFilePath)) {
                Files.createFile(dataFilePath);
            }
        } catch (IOException e) {
            logger.warn("Unable to create locale.yml. Error: {}", e.getMessage(), e);
        }
        reloadLocale();
    }

    public static LocaleHandler getInstance() {
        if (instance == null) {
            instance = new LocaleHandler();
        }
        return instance;
    }

    public void reloadLocale() {
        try {
            locale.load(dataFile);
            populateLocale();
            sortLocale();
            saveLocale();
        } catch (IOException | InvalidConfigurationException e) {
            logger.warn("Unable to reload locale.yml - Error: {}", e.getMessage(), e);
        }
    }


    private void populateLocale() {
        Set<LocaleMessage> missing = new HashSet<>(Arrays.asList(LocaleMessage.values()));
        for (LocaleMessage localeMessage : LocaleMessage.values()) {
            if (locale.contains(localeMessage.getPath())) {
                localeMessage.setMessage(locale.getString(localeMessage.getPath()));
                missing.remove(localeMessage);
            }
        }
        for (LocaleMessage localeMessage : missing) {
            locale.set(localeMessage.getPath(), localeMessage.getMessage());
        }
    }

    private void sortLocale() {
        FileConfiguration newLocale = new YamlConfiguration();
        List<String> keys = new ArrayList<>(locale.getKeys(true));
        Collections.sort(keys);
        for (String key : keys) {
            newLocale.set(key, locale.getString(key));
        }
        locale = newLocale;
    }

    private void saveLocale() {
        try {
            locale.save(dataFile);
        } catch (IOException e) {
            logger.warn("Unable to save locale.yml - Error: {}", e.getMessage(), e);
        }
    }

}
