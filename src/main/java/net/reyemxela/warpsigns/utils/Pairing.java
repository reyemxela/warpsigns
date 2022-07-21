package net.reyemxela.warpsigns.utils;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.reyemxela.warpsigns.Coords;
import net.reyemxela.warpsigns.PairingInfo;
import net.reyemxela.warpsigns.WarpSigns;
import net.reyemxela.warpsigns.config.Config;

import java.util.HashMap;
import java.util.Objects;

public class Pairing {
    public static final HashMap<String, PairingInfo> playerPairingSign = new HashMap<>();
    public static final HashMap<String, String> signPairingPlayer = new HashMap<>();
    public static final String globalPairingName = "__GLOBAL_PAIRING__";
    private static ServerPlayerEntity globalPairingPlayer;

    public static void startPairing(ServerPlayerEntity player, String pairingName, SignBlockEntity signEntity, Coords signCoords) {
        PairingInfo startInfo = new PairingInfo(signCoords, signEntity);
        playerPairingSign.put(pairingName, startInfo);
        signPairingPlayer.put(startInfo.getKey(), pairingName);

        if (Objects.equals(pairingName, globalPairingName)) {
            globalPairingPlayer = player; // keep track of both players to notify when pairing is finished
            player.sendMessage(Text.of("Started global pairing"));
        } else {
            player.sendMessage(Text.of("Started pairing"));
        }
        WarpSigns.LOGGER.info("Started pairing sign at " + signCoords);
    }

    public static void finishPairing(ServerPlayerEntity player, String pairingName, SignBlockEntity signEntity, Coords signCoords) {
        PairingInfo startInfo = playerPairingSign.get(pairingName);
        PairingInfo endInfo = new PairingInfo(signCoords, signEntity);
        WarpSigns.warpSignData.put(startInfo.getKey(), endInfo);
        WarpSigns.warpSignData.put(endInfo.getKey(), startInfo);

        if (Objects.equals(pairingName, globalPairingName)) {
            player.sendMessage(Text.of("Finished global pairing"));
            if (player != globalPairingPlayer)
                globalPairingPlayer.sendMessage(Text.of("Finished global pairing"));
            globalPairingPlayer = null;
        } else {
            player.sendMessage(Text.of("Finished pairing"));
        }
        WarpSigns.LOGGER.info(String.format("Finished pairing %s in %s to %s in %s",
                startInfo.pairedSign.getStr(),
                startInfo.pairedSign.getWorld(),
                endInfo.pairedSign.getStr(),
                endInfo.pairedSign.getWorld()));

        player.getMainHandStack().decrement(1);
        playerPairingSign.remove(pairingName);
        signPairingPlayer.remove(startInfo.getKey());
        Save.saveData();
    }

    public static ActionResult useSign(ServerPlayerEntity player, World world, Hand hand, SignBlockEntity sign, BlockPos pos) {
        Coords signCoords = new Coords(pos.getX(), pos.getY(), pos.getZ(), (ServerWorld)world);
        String signKey = signCoords.getKey();
        Item heldItem = player.getStackInHand(hand).getItem();

        boolean isSignPairing = signPairingPlayer.containsKey(signKey);
        boolean isSignPaired = WarpSigns.warpSignData.containsKey(signKey);
        boolean holdingPairingItem = heldItem == Registry.ITEM.get(Identifier.tryParse(Config.pairingItem));
        boolean holdingAir = heldItem == Items.AIR;
        boolean isSneaking = player.isSneaking();

        if (holdingPairingItem) {
            String pairingName = isSneaking ? globalPairingName : player.getName().getString();
            boolean isPlayerPairing = playerPairingSign.containsKey(pairingName);

            if (isSignPaired) {
                return ActionResult.PASS;
            }

            if (isSignPairing) {
                if (Objects.equals(signPairingPlayer.get(signKey), player.getName().getString())) {
                    playerPairingSign.remove(pairingName); // same player clicked same sign, cancel pairing
                    signPairingPlayer.remove(signKey);
                    player.sendMessage(Text.of("Cancelled pairing"));
                }
                return ActionResult.PASS;
            }

            if (isPlayerPairing) {
                finishPairing(player, pairingName, sign, signCoords);
            } else {
                startPairing(player, pairingName, sign, signCoords);
            }

            return ActionResult.SUCCESS;
        } else if (holdingAir) {
            if (isSneaking) {
                EditSign.editSign(player, sign);
                return ActionResult.CONSUME;
            } else if (isSignPaired) {
                Teleport.teleport(player, signCoords);
                return ActionResult.SUCCESS;
            } else {
                Help.showHelp(player);
            }
        }

        return ActionResult.PASS;
    }

    public static boolean breakSign(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
        Coords brokenSignCoords = new Coords(pos.getX(), pos.getY(), pos.getZ(), world);
        String brokenSignKey = brokenSignCoords.getKey();
        boolean isSignPaired = WarpSigns.warpSignData.containsKey(brokenSignKey);
        boolean isSignPairing = signPairingPlayer.containsKey(brokenSignKey);
        if (isSignPaired) {
            PairingInfo otherSignInfo = WarpSigns.warpSignData.get(brokenSignKey);
            if (player != null) {
                if (player.isSneaking()) {
                    String pairingName = player.getName().getString();
                    if (playerPairingSign.containsKey(pairingName)) {
                        player.sendMessage(Text.of("Pairing already in progress, can't start re-pairing"));
                        return false;
                    }
                    playerPairingSign.put(pairingName, otherSignInfo);
                    signPairingPlayer.put(otherSignInfo.getKey(), pairingName);
                    player.sendMessage(Text.of("Re-pairing"));
                } else {
                    player.sendMessage(Text.of("Warp Sign removed"));
                }
            }
            WarpSigns.warpSignData.remove(otherSignInfo.getKey());
            WarpSigns.warpSignData.remove(brokenSignKey);
            ItemStack stack = new ItemStack(Registry.ITEM.get(Identifier.tryParse(Config.pairingItem)));
            ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            world.spawnEntity(itemEntity);
            Save.saveData();
        } else if (isSignPairing) {
            playerPairingSign.remove(signPairingPlayer.get(brokenSignKey));
            signPairingPlayer.remove(brokenSignKey);
        }
        return true;
    }
}
