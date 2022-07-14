package net.reyemxela.warpsigns;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class Coords {
    private int x, y, z;
    private final ServerWorld world;

    public Coords(int x, int y, int z, ServerWorld world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public Coords(Coords coords) { this(coords.x, coords.y, coords.z, coords.world); }

    public Coords(String coords) {
        String[] splitString = coords.split("/");
        String[] splitCoords = splitString[0].split(",");
        this.x = Integer.parseInt(splitCoords[0]);
        this.y = Integer.parseInt(splitCoords[1]);
        this.z = Integer.parseInt(splitCoords[2]);
        this.world = strToWorld(splitString[1]);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public ServerWorld getWorld() { return world; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setZ(int z) { this.z = z; }

    public String getStr() { return String.format("%d,%d,%d", x, y, z); }
    public String getKey() { return getStr() + "/" + worldToStr(); }
    public BlockPos getBlockPos() { return new BlockPos(x, y, z); }
    public Coords offset(int x, int y, int z) { return new Coords(this.x + x, this.y + y, this.z + z, this.world); }

    private String worldToStr() { return world.getDimensionKey().getValue().toString(); }
    private static ServerWorld strToWorld(String world) { return WarpSigns.serverInstance.getWorld(RegistryKey.of(Registry.WORLD_KEY, Identifier.tryParse(world))); }
}
