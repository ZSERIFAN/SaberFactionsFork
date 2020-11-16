package com.massivecraft.factions.zcore.frame.fshield;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class FShieldListener implements Listener {

    @EventHandler
    public void onTnt(EntityExplodeEvent e){
        if(e.getEntityType() == EntityType.PRIMED_TNT){
            Faction fac = Board.getInstance().getFactionAt(new FLocation(e.getLocation()));
            if(fac.getShieldRunning()) e.setCancelled(true);
        }
    }
}
