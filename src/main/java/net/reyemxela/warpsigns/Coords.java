package net.reyemxela.warpsigns;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;

public class Coords extends BlockPos {
    private final ServerWorld world;

    public Coords(int x, int y, int z, ServerWorld world) {
        super(x, y, z);
        this.world = world;
    }

    public Coords(Coords coords) { this(coords.getX(), coords.getY(), coords.getZ(), coords.getWorld()); }

    public Coords(String coords) {
        super(0, 0, 0);
        String[] splitString = coords.split("/");
        int[] splitCoords = Arrays.stream(splitString[0].split(",")).mapToInt(Integer::parseInt).toArray();
        setX(splitCoords[0]);
        setY(splitCoords[1]);
        setZ(splitCoords[2]);
        this.world = strToWorld(splitString[1]);
    }

    public ServerWorld getWorld() { return world; }

    public String getStr() { return String.format("%d,%d,%d", getX(), getY(), getZ()); }
    public String getKey() { return getStr() + "/" + worldToStr(); }

    public Coords offset(Direction direction, int i) {
        return i == 0 ? this : new Coords(this.getX() + direction.getOffsetX() * i, this.getY() + direction.getOffsetY() * i, this.getZ() + direction.getOffsetZ() * i, this.getWorld());
    }

    private String worldToStr() { return world.getDimensionKey().getValue().toString(); }
    private static ServerWorld strToWorld(String world) { return WarpSigns.serverInstance.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(world))); }
}
