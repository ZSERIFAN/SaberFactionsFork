package com.massivecraft.factions;

import com.massivecraft.factions.util.particle.BukkitParticleProvider;
import com.massivecraft.factions.util.particle.PacketParticleProvider;
import com.massivecraft.factions.util.particle.darkblade12.ReflectionUtils;
import org.bukkit.Bukkit;

public class Version {

    public static void versionInfo() {
        short version = Short.parseShort(ReflectionUtils.PackageType.getServerVersion().split("_")[1]);
        switch (version) {
            case 7:
                FactionsPlugin.instance.log("Minecraft com.massivecraft.factions.Version 1.7 found, disabling banners, itemflags inside GUIs, corners, and Titles.");
                break;
            case 8:
                FactionsPlugin.instance.log("Minecraft com.massivecraft.factions.Version 1.8 found, Title Fadeouttime etc will not be configurable.");
                break;
            case 12:
                break;
            case 13:
                FactionsPlugin.instance.log("Minecraft com.massivecraft.factions.Version 1.13 found, New Items will be used.");
                break;
            case 14:
            case 15:
            case 16:
                FactionsPlugin.instance.log("Minecraft com.massivecraft.factions.Version 1." + version + " found!");
                break;
        }
    }

    public static void initParticleProvider() {
        if (FactionsPlugin.instance.version <= 13) // Before 1.13
            FactionsPlugin.instance.particleProvider = new PacketParticleProvider();
         else
            FactionsPlugin.instance.particleProvider = new BukkitParticleProvider();
        Bukkit.getLogger().info(FactionsPlugin.instance.txt.parse("Using %1s as a particle provider", FactionsPlugin.instance.particleProvider.name()));
    }

    public static void initNonPacketParticles() {
        if (FactionsPlugin.instance.version > 8) {
            FactionsPlugin.instance.useNonPacketParticles = true;
            FactionsPlugin.instance.log("Minecraft com.massivecraft.factions.Version 1.9 or higher found, using non packet based particle API");
        }
    }

}
