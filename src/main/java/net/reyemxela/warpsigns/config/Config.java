package net.reyemxela.warpsigns.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.reyemxela.warpsigns.WarpSigns;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile = null;

    public String pairingItem = "minecraft:diamond";
    public Boolean adminOnly = false;

    public static void initialize() {
        configFile = new File(FabricLoader.getInstance().getConfigDir().toString(), "warpSigns.json");
        loadData();
    }

    private static void loadData() {
        try {
            var reader = new FileReader(configFile);
            WarpSigns.config = gson.fromJson(reader, Config.class);
            reader.close();
            WarpSigns.LOGGER.info("Loaded warpSign config from file");
        } catch (IOException | JsonSyntaxException | JsonIOException err) {
            WarpSigns.LOGGER.info("Creating new config file");
            WarpSigns.config = new Config();
            saveData();
        }
    }

    private static void saveData() {
        try {
            var writer = new FileWriter(configFile);
            gson.toJson(WarpSigns.config, writer);
            writer.close();
            WarpSigns.LOGGER.info("Saved warpSign config file");
        } catch (IOException | JsonIOException err) {
            WarpSigns.LOGGER.info("Unable to create warpSign config file!");
        }
    }
}
