package net.reyemxela.warpsigns;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class WarpSigns implements ModInitializer {
    public static final String MOD_ID = "net.reyemxela.warpsigns";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer serverInstance;

    public static HashMap<String, PairingInfo> warpSignData;

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register(Handlers::serverLoadHandler);

        UseBlockCallback.EVENT.register(Handlers::clickHandler);

        PlayerBlockBreakEvents.BEFORE.register(Handlers::blockBreakHandler);
    }
}
