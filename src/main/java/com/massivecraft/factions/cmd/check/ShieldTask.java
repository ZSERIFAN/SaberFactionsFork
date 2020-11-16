package com.massivecraft.factions.cmd.check;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.Bukkit;

public class ShieldTask implements Runnable {
    private FactionsPlugin plugin;

    public ShieldTask(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    private String formatTime(long x){
        return String.format("%02d:%02d:%02d", (x / (1000 * 60 * 60)) % 24, (x / (1000 * 60)) % 60, (x / 1000) % 60);
    }
    @Override
    public void run() {
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if (faction.getShieldPlanned() && !faction.getShieldRunning()) {
                if (System.currentTimeMillis() - faction.getShieldStart() > 0) {
                    faction.setShieldRunning(true);
                    faction.msg(TL.SHIELD_STARTED.toString().replace("{to}", formatTime(faction.getShieldEnd())));
                }
                continue;
            }
            if (faction.getShieldPlanned() && faction.getShieldRunning()) {
                if (faction.getShieldEnd() - System.currentTimeMillis() < 0) {
                    faction.msg(TL.SHIELD_EXPIRED_MESSAGE);
                    faction.setShieldRunning(false);
                    faction.setShieldPlanned(false);
                }
                continue;
            }
            if (!faction.getShieldPlanned() && !faction.getShieldRunning())
                continue;
            faction.msg(TL.SHIELD_EXPIRED_MESSAGE);
            faction.setShieldRunning(false);
            faction.setShieldPlanned(false);
        }
    }
}
