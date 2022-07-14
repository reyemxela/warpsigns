package net.reyemxela.warpsigns.utils;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.reyemxela.warpsigns.Coords;
import net.reyemxela.warpsigns.PairingInfo;
import net.reyemxela.warpsigns.WarpSigns;

public class Teleport {
    public static void teleport(ServerPlayerEntity player, Coords signCoords) {
        PairingInfo destInfo = WarpSigns.warpSignData.get(signCoords.getKey());
        ServerWorld newWorld = destInfo.pairedSignDest.getWorld();
        ServerWorld prevWorld = player.getWorld();
        BlockPos prevPos = player.getBlockPos();

        prevWorld.playSound(null, prevPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1f, 1f);
        prevWorld.sendEntityStatus(player, EntityStatuses.ADD_PORTAL_PARTICLES);

        player.teleport(
                newWorld,
                destInfo.pairedSignDest.getX() + 0.5f,
                destInfo.pairedSignDest.getY(),
                destInfo.pairedSignDest.getZ() + 0.5f,
                destInfo.facing,
                player.getPitch()
        );

        newWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1f, 1f);
        newWorld.sendEntityStatus(player, EntityStatuses.ADD_PORTAL_PARTICLES);
    }
}
