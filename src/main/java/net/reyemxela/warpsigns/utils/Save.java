package net.reyemxela.warpsigns.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.WorldSavePath;
import net.reyemxela.warpsigns.Coords;
import net.reyemxela.warpsigns.PairingInfo;
import net.reyemxela.warpsigns.WarpSigns;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class Save {
    private static final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    private static Gson gson;
    public static File dataFile = null;

    private static final JsonSerializer<PairingInfo> serializer = (src, typeOfSrc, context) -> {
        JsonObject jsonPairingInfo = new JsonObject();
        jsonPairingInfo.addProperty("pairedSign", src.pairedSign.getKey());
        jsonPairingInfo.addProperty("pairedSignDest", src.pairedSignDest.getKey());
        jsonPairingInfo.addProperty("facing", src.facing);
        return jsonPairingInfo;
    };

    private static final JsonDeserializer<PairingInfo> deserializer = (json, typeOfT, context) -> {
        JsonObject jsonObject = json.getAsJsonObject();

        Coords pairedSign = new Coords(jsonObject.get("pairedSign").getAsString());
        Coords pairedSignDest = new Coords(jsonObject.get("pairedSignDest").getAsString());
        int facing = jsonObject.get("facing").getAsInt();

        return new PairingInfo(pairedSign, pairedSignDest, facing);
    };

    public static void initialize() {
        gsonBuilder.registerTypeAdapter(PairingInfo.class, serializer);
        gsonBuilder.registerTypeAdapter(PairingInfo.class, deserializer);
        gson = gsonBuilder.create();

        dataFile = new File(WarpSigns.serverInstance.getSavePath(WorldSavePath.ROOT).toString(), "warpSignData.json");
        loadData();
    }

    private static void loadData() {
        try {
            var reader = new FileReader(dataFile);
            Type mapType = new TypeToken<HashMap<String, PairingInfo>>(){}.getType();
            WarpSigns.warpSignData = gson.fromJson(reader, mapType);
            reader.close();
            WarpSigns.LOGGER.info("Loaded warpSign save from file");
        } catch (IOException | JsonSyntaxException | JsonIOException err) {
            WarpSigns.LOGGER.info("Creating new save file");
            WarpSigns.warpSignData = new HashMap<>();
            saveData();
        }
    }

    public static void saveData() {
        try {
            var writer = new FileWriter(dataFile);
            gson.toJson(WarpSigns.warpSignData, writer);
            writer.close();
            WarpSigns.LOGGER.info("Saved warpSign save file");
        } catch (IOException | JsonIOException err) {
            WarpSigns.LOGGER.error("Unable to create warpSign save file!");
        }
    }
}
