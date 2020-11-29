package com.massivecraft.factions;

import com.massivecraft.factions.cmd.audit.FLogManager;
import com.massivecraft.factions.cmd.reserve.ListParameterizedType;
import com.massivecraft.factions.cmd.reserve.ReserveObject;
import com.massivecraft.factions.zcore.MPlugin;
import com.massivecraft.factions.zcore.file.impl.FileManager;
import com.massivecraft.factions.zcore.util.TL;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DataUtils {

    public static void initReserves() {
        String path = Paths.get(FactionsPlugin.getInstance().getDataFolder().getAbsolutePath()).toAbsolutePath().toString() + File.separator + "data" + File.separator + "reserves.json";
        File file = new File(path);
        try {
            String json;
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            json = String.join("", Files.readAllLines(Paths.get(file.getPath()))).replace("\n", "").replace("\r", "");
            if (json.equalsIgnoreCase("")) {
                Files.write(Paths.get(path), "[]".getBytes());
                json = "[]";
            }
            FactionsPlugin.getInstance().reserveObjects = FactionsPlugin.getInstance().getGsonBuilder().create().fromJson(json, new ListParameterizedType(ReserveObject.class));
            if (FactionsPlugin.getInstance().reserveObjects == null) FactionsPlugin.getInstance().reserveObjects = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveReserves() {
        try {
            String path = Paths.get(FactionsPlugin.getInstance().getDataFolder().getAbsolutePath()).toAbsolutePath().toString() + File.separator + "data" + File.separator + "reserves.json";
            File file = new File(path);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            Files.write(Paths.get(file.getPath()), FactionsPlugin.getInstance().getGsonBuilder().create().toJson(FactionsPlugin.getInstance().reserveObjects).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean setupPermissions() {
        try {
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp != null) FactionsPlugin.getInstance().perms = rsp.getProvider();
        } catch (NoClassDefFoundError ex) {
            return false;
        }
        return FactionsPlugin.getInstance().perms != null;
    }

    public static boolean setupPlayervaults() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PlayerVaults");
        return plugin != null && plugin.isEnabled();
    }

}
