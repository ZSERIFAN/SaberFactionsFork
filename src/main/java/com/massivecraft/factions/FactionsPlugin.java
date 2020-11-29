package com.massivecraft.factions;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.cmd.*;
import com.massivecraft.factions.cmd.audit.FLogManager;
import com.massivecraft.factions.cmd.reserve.ReserveAdapter;
import com.massivecraft.factions.cmd.reserve.ReserveObject;
import com.massivecraft.factions.listeners.*;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.util.*;
import com.massivecraft.factions.util.adapters.*;
import com.massivecraft.factions.util.particle.ParticleProvider;
import com.massivecraft.factions.util.particle.darkblade12.ReflectionUtils;
import com.massivecraft.factions.util.timer.TimerManager;
import com.massivecraft.factions.zcore.CommandVisibility;
import com.massivecraft.factions.zcore.MPlugin;
import com.massivecraft.factions.zcore.file.impl.FileManager;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.Permissable;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.io.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;


public class FactionsPlugin extends MPlugin {

    /*
    Main thread
    Completely recoded & cleaned up by @thmihnea
     */

    public static FactionsPlugin instance;
    public int threads = getConfig().getInt("cpu-threads");
    public static boolean cachedRadiusClaim;
    public static Permission perms = null;
    public static boolean startupFinished = false;
    public boolean PlaceholderApi;
    public static YamlConfiguration LANG;
    public static File LANG_FILE;
    public FCmdRoot cmdBase;
    public CmdAutoHelp cmdAutoHelp;
    public short version;
    public boolean useNonPacketParticles = false;
    public List<String> itemList = getConfig().getStringList("fchest.Items-Not-Allowed");
    public FactionsPlayerListener factionsPlayerListener;
    private boolean locked = false;
    private Integer AutoLeaveTask = null;
    public boolean hookedPlayervaults;
    public ClipPlaceholderAPIManager clipPlaceholderAPIManager;
    public SeeChunkUtil seeChunkUtil;
    public ParticleProvider particleProvider;
    public boolean mvdwPlaceholderAPIManager = false;
    public Listener[] eventsListener;
    public FLogManager fLogManager;
    public List<ReserveObject> reserveObjects;
    private FileManager fileManager;
    public TimerManager timerManager;
    public static File data = new File("plugins/Factions/data");

    public FactionsPlugin() {
        instance = this;
    }

    public static FactionsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        log("==== Setup ====");
        Util.checkVault();
        version = Short.parseShort(ReflectionUtils.PackageType.getServerVersion().split("_")[1]);
        Version.versionInfo();
        Util.migrateFPlayerLeaders();
        log("==== End Setup ====");
        int pluginId = 7013;
        new Metrics(this, pluginId);
        if (!preEnable()) {
            this.loadSuccessful = false;
            return;
        }
        Util.initSetup();
        this.loadSuccessful = true;
    }

    @Override
    public void onDisable() {
        if (this.AutoLeaveTask != null) {
            getServer().getScheduler().cancelTask(this.AutoLeaveTask);
            this.AutoLeaveTask = null;
        }
        Conf.saveSync();
        timerManager.saveTimerData();
        fLogManager.saveLogs();
        DataUtils.saveReserves();
        super.onDisable();
    }

    public void startAutoLeaveTask(boolean restartIfRunning) {
        if (AutoLeaveTask != null) {
            if (!restartIfRunning) return;
            this.getServer().getScheduler().cancelTask(AutoLeaveTask);
        }
        if (Conf.autoLeaveRoutineRunsEveryXMinutes > 0.0) {
            long ticks = (long) (20 * 60 * Conf.autoLeaveRoutineRunsEveryXMinutes);
            AutoLeaveTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new AutoLeaveTask(), ticks, ticks);
        }
    }

    @Override
    public void postAutoSave() {
        Conf.save();
    }

    @Override
    public boolean logPlayerCommands() {
        return Conf.logPlayerCommands;
    }

    @Override
    public boolean handleCommand(CommandSender sender, String commandString, boolean testOnly) {
        return sender instanceof Player && FactionsPlayerListener.preventCommand(commandString, (Player) sender) || super.handleCommand(sender, commandString, testOnly);
    }

    @Override
    public GsonBuilder getGsonBuilder() {
        Type mapFLocToStringSetType = new TypeToken<Map<FLocation, Set<String>>>() {
        }.getType();

        Type accessTypeAdatper = new TypeToken<Map<Permissable, Map<PermissableAction, Access>>>() {
        }.getType();

        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().enableComplexMapKeySerialization().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
                .registerTypeAdapter(accessTypeAdatper, new PermissionsMapTypeAdapter())
                .registerTypeAdapter(LazyLocation.class, new MyLocationTypeAdapter())
                .registerTypeAdapter(mapFLocToStringSetType, new MapFLocToStringSetTypeAdapter())
                .registerTypeAdapter(Inventory.class, new InventoryTypeAdapter())
                .registerTypeAdapter(ReserveObject.class, new ReserveAdapter())
                .registerTypeAdapter(Location.class, new LocationTypeAdapter())
                .registerTypeAdapterFactory(EnumTypeAdapter.ENUM_FACTORY);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argsList = new LinkedList<>(Arrays.asList(args));
        CommandContext context = new CommandContext(sender, argsList, alias);
        List<FCommand> commandsList = cmdBase.subCommands;
        FCommand commandsEx = cmdBase;
        List<String> completions = new ArrayList<>();
        switch (context.args.size()) {
            case 0:
                for (FCommand subCommand : commandsEx.subCommands) {
                    if (subCommand.requirements.playerOnly && sender.hasPermission(subCommand.requirements.permission.node) && subCommand.visibility != CommandVisibility.INVISIBLE)
                        completions.addAll(subCommand.aliases);
                }
                return completions;
            case 1:
                for (; !commandsList.isEmpty() && !context.args.isEmpty(); context.args.remove(0)) {
                    String cmdName = context.args.get(0).toLowerCase();
                    boolean toggle = false;
                    for (FCommand fCommand : commandsList) {
                        for (String s : fCommand.aliases) {
                            if (s.startsWith(cmdName)) {
                                commandsList = fCommand.subCommands;
                                completions.addAll(fCommand.aliases);
                                toggle = true;
                                break;
                            }
                        }
                        if (toggle) break;
                    }
                }
                String lastArg = args[args.length - 1].toLowerCase();
                completions = completions.stream()
                        .filter(m -> m.toLowerCase().startsWith(lastArg))
                        .collect(Collectors.toList());
                return completions;
            default:
                String lastArgm = args[args.length - 1].toLowerCase();
                for (Role value : Role.values()) completions.add(value.nicename);
                for (Relation value : Relation.values()) completions.add(value.nicename);
                for (Player player : Bukkit.getServer().getOnlinePlayers()) completions.add(player.getName());
                for (Faction faction : Factions.getInstance().getAllFactions())
                    completions.add(ChatColor.stripColor(faction.getTag()));
                completions = completions.stream().filter(m -> m.toLowerCase().startsWith(lastArgm)).collect(Collectors.toList());
                return completions;
        }
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public boolean getLocked() {
        return this.locked;
    }

    public void setLocked(boolean val) {
        this.locked = val;
        this.setAutoSave(val);
    }

    public void initConfig() {
        saveDefaultConfig();
        this.reloadConfig();
        Conf.load();
        if (!data.exists()) data.mkdir();
        fileManager = new FileManager();
        fileManager.setupFiles();
        fLogManager = new FLogManager();
    }

    public static File getLangFile() {
        return LANG_FILE;
    }
}
