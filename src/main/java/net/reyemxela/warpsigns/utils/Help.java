package net.reyemxela.warpsigns.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.reyemxela.warpsigns.config.Config;

public class Help {
    public static void showHelp(ServerPlayerEntity player) {
        player.sendMessage(Text.of("------ WarpSigns usage: ------"));
        player.sendMessage(Text.of(String.format("Pairing item: %s", Config.pairingItem)));
        player.sendMessage(Text.of("- Right-click with item to pair signs"));
        player.sendMessage(Text.of("- Sneak-right-click for global pairing (another player can finish pairing)"));
        player.sendMessage(Text.of("- Sneak-break a sign to break it and start re-pairing"));
        player.sendMessage(Text.of("- Sneak-right-click to edit sign text"));
    }
}
