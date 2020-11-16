package com.massivecraft.factions;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.Objects;

public class FLocation implements Serializable {
    private static final long serialVersionUID = -8292915234027387983L;
    private static final boolean worldBorderSupport;

    static {
        boolean worldBorderClassPresent = false;
        try {
            Class.forName("org.bukkit.WorldBorder");
            worldBorderClassPresent = true;
        } catch (ClassNotFoundException ignored) {
        }
        worldBorderSupport = worldBorderClassPresent;
    }

    private String worldName = "world";
    private int x = 0;
    private int z = 0;

    //----------------------------------------------//
    // Constructors
    //----------------------------------------------//

    public FLocation() {

    }

    public FLocation(String worldName, int x, int z) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }

    public FLocation(Location location) {
        this(Objects.requireNonNull(location.getWorld()).getName(), blockToChunk(location.getBlockX()), blockToChunk(location.getBlockZ()));
    }

    public FLocation(Player player) {
        this(player.getLocation());
    }

    public FLocation(FPlayer fplayer) {
        this(fplayer.getPlayer());
    }

    public FLocation(Block block) {
        this(block.getLocation());
    }

    //----------------------------------------------//
    // Getters and Setters
    //----------------------------------------------//

    public static FLocation fromString(String string) {
        int index = string.indexOf(",");
        int start = 1;
        String worldName = string.substring(start, index);
        start = index + 1;
        index = string.indexOf(",", start);
        int x = Integer.parseInt(string.substring(start, index));
        int y = Integer.parseInt(string.substring(index + 1, string.length() - 1));
        return new FLocation(worldName, x, y);
    }

    // bit-shifting is used because it's much faster than standard division and multiplication
    public static int blockToChunk(int blockVal) {    // 1 chunk is 16x16 blocks
        return blockVal >> 4;   // ">> 4" == "/ 16"
    }

    public static int chunkToBlock(int chunkVal) {
        return chunkVal << 4;   // "<< 4" == "* 16"
    }

    public Chunk getChunk() {
        return Bukkit.getWorld(worldName).getChunkAt(x, z);
    }

    public String getWorldName() {
        return worldName;
    }

    //----------------------------------------------//
    // Block/Chunk/Region Value Transformation
    //----------------------------------------------//

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public long getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public long getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getCoordString() {
        return "" + x + "," + z;
    }

    public String formatXAndZ(String splitter) {
        return chunkToBlock(this.x) + "x" + splitter + " " + chunkToBlock(this.z) + "z";
    }

    //----------------------------------------------//
    // Misc Geometry
    //----------------------------------------------//

    @Override
    public String toString() {
        return "[" + this.getWorldName() + "," + this.getCoordString() + "]";
    }

    public FLocation getRelative(int dx, int dz) {
        return new FLocation(this.worldName, this.x + dx, this.z + dz);
    }

    public double getDistanceTo(FLocation that) {
        double dx = that.x - this.x;
        double dz = that.z - this.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public boolean isInChunk(Location loc) {
        if (loc == null) return false;
        if (loc.getWorld() == null) return false;

        Chunk chunk = loc.getChunk();
        return loc.getWorld().getName().equalsIgnoreCase(getWorldName()) && chunk.getX() == x && chunk.getZ() == z;
    }

    public boolean isOutsideWorldBorder(int buffer) {
        if (!worldBorderSupport) return false;
        WorldBorder border = getWorld().getWorldBorder();
        Location center = border.getCenter();
        double size = border.getSize();

        int bufferBlocks = buffer << 4;

        double borderMinX = (center.getX() - size / 2.0D) + bufferBlocks;
        double borderMinZ = (center.getZ() - size / 2.0D) + bufferBlocks;
        double borderMaxX = (center.getX() + size / 2.0D) - bufferBlocks;
        double borderMaxZ = (center.getZ() + size / 2.0D) - bufferBlocks;

        int chunkMinX = this.x << 4;
        int chunkMaxX = chunkMinX | 15;
        int chunkMinZ = this.z << 4;
        int chunkMaxZ = chunkMinZ | 15;
        return (chunkMinX >= borderMaxX) || (chunkMinZ >= borderMaxZ) || (chunkMaxX <= borderMinX) || (chunkMaxZ <= borderMinZ);
    }

    //----------------------------------------------//
    // Comparison
    //----------------------------------------------//

    @Override
    public int hashCode() {
        // should be fast, with good range and few hash collisions: (x * 512) + z + worldName.hashCode
        return (this.x << 9) + this.z + (this.worldName != null ? this.worldName.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FLocation)) return false;
        FLocation that = (FLocation) obj;
        return this.x == that.x && this.z == that.z && (Objects.equals(this.worldName, that.worldName));
    }
}