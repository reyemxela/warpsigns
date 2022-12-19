package net.reyemxela.warpsigns;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.MinecraftServer;
import net.reyemxela.warpsigns.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class WarpSigns implements ModInitializer {
    public static final String MOD_ID = "net.reyemxela.warpsigns";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer serverInstance;

    public static HashMap<String, PairingInfo> warpSignData;
    public static Config config;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(Handlers::serverLoadHandler);

        UseBlockCallback.EVENT.register(Handlers::clickHandler);

        PlayerBlockBreakEvents.BEFORE.register(Handlers::blockBreakHandler);
    }
}
