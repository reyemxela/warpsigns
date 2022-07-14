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
import java.util.Map;
import java.util.Objects;

public class Pairing {
    public static final HashMap<String, PairingInfo> playerPairing = new HashMap<>();
    public static final String globalPairingName = "__GLOBAL_PAIRING__";
    private static ServerPlayerEntity globalPairingPlayer;

    public static void startPairing(ServerPlayerEntity player, String pairingName, SignBlockEntity signEntity, Coords signCoords) {
        PairingInfo startInfo = new PairingInfo(signCoords, signEntity);
        playerPairing.put(pairingName, startInfo);

        if (Objects.equals(pairingName, globalPairingName)) {
            globalPairingPlayer = player; // keep track of both players to notify when pairing is finished
            player.sendMessage(Text.of("Started global pairing"));
        } else {
            player.sendMessage(Text.of("Started pairing"));
        }
        WarpSigns.LOGGER.info("Started pairing sign at " + signCoords);
    }

    public static void finishPairing(ServerPlayerEntity player, String pairingName, SignBlockEntity signEntity, Coords signCoords) {
        PairingInfo startInfo = playerPairing.get(pairingName);
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
        playerPairing.remove(pairingName);
        Save.saveData();
    }

    public static ActionResult useSign(ServerPlayerEntity player, World world, Hand hand, SignBlockEntity sign, BlockPos pos) {
        Coords signCoords = new Coords(pos.getX(), pos.getY(), pos.getZ(), (ServerWorld)world);
        Item heldItem = player.getStackInHand(hand).getItem();

        boolean isPaired = WarpSigns.warpSignData.containsKey(signCoords.getKey());
        boolean holdingPairingItem = heldItem == Registry.ITEM.get(Identifier.tryParse(Config.pairingItem));
        boolean holdingAir = heldItem == Items.AIR;
        boolean isSneaking = player.isSneaking();

        if (holdingPairingItem) {
            if (isPaired) {
                return ActionResult.PASS;
            }

            String pairingName;
            if (isSneaking) {
                pairingName = Pairing.globalPairingName;
            } else {
                pairingName = player.getName().getString();
            }

            if (Pairing.playerPairing.containsKey(pairingName)) {
                if (Objects.equals(Pairing.playerPairing.get(pairingName).pairedSign.getKey(), signCoords.getKey())) {
                    Pairing.playerPairing.remove(pairingName); // same player clicked same sign, cancel pairing
                    player.sendMessage(Text.of("Cancelled pairing"));
                } else {
                    Pairing.finishPairing(player, pairingName, sign, signCoords);
                }
            } else {
                Pairing.startPairing(player, pairingName, sign, signCoords);
            }
            return ActionResult.SUCCESS;
        } else if (holdingAir) {
            if (isSneaking) {
                EditSign.editSign(player, sign);
                return ActionResult.CONSUME;
            } else if (isPaired) {
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
        if (WarpSigns.warpSignData.containsKey(brokenSignCoords.getKey())) {
            if (player != null) {
                if (player.isSneaking()) {
                    String pairingName = player.getName().getString();
                    if (playerPairing.containsKey(pairingName)) {
                        player.sendMessage(Text.of("Pairing already in progress, can't start re-pairing"));
                        return false;
                    }
                    playerPairing.put(pairingName, WarpSigns.warpSignData.get(brokenSignCoords.getKey()));
                    player.sendMessage(Text.of("Re-pairing"));
                } else {
                    player.sendMessage(Text.of("Warp Sign removed"));
                }
            }
            WarpSigns.warpSignData.remove(WarpSigns.warpSignData.get(brokenSignCoords.getKey()).pairedSign.getKey());
            WarpSigns.warpSignData.remove(brokenSignCoords.getKey());
            ItemStack stack = new ItemStack(Registry.ITEM.get(Identifier.tryParse(Config.pairingItem)));
            ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            world.spawnEntity(itemEntity);
            Save.saveData();
        } else {
            for (Map.Entry<String, PairingInfo> p : playerPairing.entrySet()) {
                if (Objects.equals(p.getValue().pairedSign.getKey(), brokenSignCoords.getKey())) {
                    playerPairing.remove(p.getKey());
                    break;
                }
            }
        }
        return true;
    }
}
