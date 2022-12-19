package net.reyemxela.warpsigns;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.reyemxela.warpsigns.config.Config;
import net.reyemxela.warpsigns.utils.Pairing;
import net.reyemxela.warpsigns.utils.Save;

public class Handlers {
    public static void serverLoadHandler(MinecraftServer server) {
        WarpSigns.serverInstance = server;
        Save.initialize();
        Config.initialize();
    }

    public static ActionResult clickHandler(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (world.isClient) { return ActionResult.PASS; }
        if (hand != Hand.MAIN_HAND) { return ActionResult.PASS; }
        BlockPos pos = hit.getBlockPos();
        if (!(world.getBlockEntity(pos) instanceof SignBlockEntity sign)) { return ActionResult.PASS; }

        return Pairing.useSign((ServerPlayerEntity)player, world, hand, sign, pos);
    }

    public static boolean blockBreakHandler(World world, PlayerEntity player, BlockPos pos, BlockState ignoredState, BlockEntity entity) {
        if (world.isClient) { return true; }
        if (entity == null || entity.getType() != BlockEntityType.SIGN) { return true; }
        return Pairing.breakSign((ServerWorld)world, (ServerPlayerEntity)player, pos);
    }
}