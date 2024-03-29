package net.reyemxela.warpsigns.utils;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.reyemxela.warpsigns.Coords;
import net.reyemxela.warpsigns.PairingInfo;
import net.reyemxela.warpsigns.WarpSigns;

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
        boolean holdingPairingItem = heldItem == Registries.ITEM.get(Identifier.tryParse(WarpSigns.config.pairingItem));
        boolean holdingAir = heldItem == Items.AIR;
        boolean isSneaking = player.isSneaking();

        if (holdingPairingItem) {
            String pairingName = isSneaking ? globalPairingName : player.getName().getString();
            boolean isPlayerPairing = playerPairingSign.containsKey(pairingName);

            if (isSignPaired) {
                return ActionResult.PASS;
            }
            if (!isAllowed(player)) {
                player.sendMessage(Text.of("You don't have permission to pair signs"));
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
                if (!isSignPaired || isAllowed(player)) {
                    EditSign.editSign(player, sign);
                    return ActionResult.CONSUME;
                } else {
                    player.sendMessage(Text.of("You don't have permission to edit this sign"));
                }
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
        // TODO:
        //  when cancelling a sign break, the client will desync and not show the text on the sign.
        //  the text is still there, and disconnecting/reconnecting to the server will show it again.
        //  not sure if this is something that can easily be fixed or not.
        Coords brokenSignCoords = new Coords(pos.getX(), pos.getY(), pos.getZ(), world);
        String brokenSignKey = brokenSignCoords.getKey();
        boolean isSignPaired = WarpSigns.warpSignData.containsKey(brokenSignKey);
        boolean isSignPairing = signPairingPlayer.containsKey(brokenSignKey);
        if (isSignPaired) {
            PairingInfo otherSignInfo = WarpSigns.warpSignData.get(brokenSignKey);
            if (player != null) {
                if (!isAllowed(player)) {
                    player.sendMessage(Text.of("You don't have permission to break this sign"));
                    return false;
                }
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
            ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.tryParse(WarpSigns.config.pairingItem)));
            ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            world.spawnEntity(itemEntity);
            Save.saveData();
        } else if (isSignPairing) {
            if (player != null && !isAllowed(player)) {
                player.sendMessage(Text.of("You don't have permission to break this sign"));
            }
            playerPairingSign.remove(signPairingPlayer.get(brokenSignKey));
            signPairingPlayer.remove(brokenSignKey);
        }
        return true;
    }

    private static boolean isAllowed(ServerPlayerEntity player) {
        return !WarpSigns.config.adminOnly || WarpSigns.serverInstance.getPlayerManager().isOperator(player.getGameProfile());
    }
}
