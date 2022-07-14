package net.reyemxela.warpsigns;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;

import java.util.Collection;


public class PairingInfo {
    public final Coords pairedSign;
    public final Coords pairedSignDest;
    public final int facing;
    private static final int warpOffset = 1;

    public PairingInfo(Coords signCoords, SignBlockEntity signEntity) {
        this.pairedSign = signCoords;
        this.facing = getFacing(signEntity);
        this.pairedSignDest = getDestCoords(signCoords, this.facing);
    }

    public PairingInfo(Coords pairedSign, Coords pairedSignDest, int facing) {
        this.pairedSign = pairedSign;
        this.facing = facing;
        this.pairedSignDest = pairedSignDest;
    }

    public String getKey() {
        return pairedSign.getKey();
    }


    private int getFacing(SignBlockEntity entity) {
//        0  (0)   faces south (+Z)
//        4  (90)  faces west  (-X)
//        8  (180) faces north (-Z)
//        12 (270) faces east  (+X)

        BlockState state = entity.getCachedState();
        Collection<Property<?>> props = state.getProperties();
        int yaw = 0;
        if (props.contains(SignBlock.ROTATION)) {
            yaw = ((state.get(SignBlock.ROTATION)+2)%16)/4*90;
        } else if (props.contains(WallSignBlock.FACING)) {
            yaw = (int)state.get(WallSignBlock.FACING).asRotation();
        }
        return yaw;
    }

    private Coords getDestCoords(Coords coords, int facing) {
        Coords newCoords = new Coords(coords);

        switch (facing) {
            case 0 -> newCoords.setZ(newCoords.getZ() + warpOffset);
            case 90 -> newCoords.setX(newCoords.getX() - warpOffset);
            case 180 -> newCoords.setZ(newCoords.getZ() - warpOffset);
            case 270 -> newCoords.setX(newCoords.getX() + warpOffset);
        }

        if (isEmpty(newCoords)) {
            for (int i = 0; i < 5; i++) {
                if (!isEmpty(newCoords.offset(0, -i, 0))) {
                    // found ground, go back up one
                    return newCoords.offset(0, -i + 1, 0);
                }
            }
        }
        // no empty space found, fall back to the sign's position
        return coords;
    }

    private boolean isEmpty(Coords coords) {
        ServerWorld world = coords.getWorld();
        return world.getBlockState(coords.getBlockPos()).getBlock() == Blocks.AIR
                && world.getBlockState(coords.offset(0, 1, 0).getBlockPos()).getBlock() == Blocks.AIR;
    }
}
