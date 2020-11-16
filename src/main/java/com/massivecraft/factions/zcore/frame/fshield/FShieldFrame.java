package com.massivecraft.factions.zcore.frame.fshield;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.Util;
import com.massivecraft.factions.util.ItemBuilder;
import com.massivecraft.factions.util.timer.DateTimeFormats;
import com.massivecraft.factions.zcore.util.TL;
import me.lucko.helper.item.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class FShieldFrame {

    private Gui gui;

    public FShieldFrame(Faction f) {
        this.gui = new Gui(FactionsPlugin.instance, 5,
                ChatColor.translateAlternateColorCodes('&', FactionsPlugin.instance.getConfig()
                        .getString("fshield.Title")));
    }

    private String formatTime(long x){
        return String.format("%02d:%02d:%02d", (x / (1000 * 60 * 60)) % 24, (x / (1000 * 60)) % 60, (x / 1000) % 60);
    }

    public void buildGUI(FPlayer fplayer) {
        PaginatedPane pane = new PaginatedPane(0, 0, 9, this.gui.getRows());
        List<GuiItem> GUIItems = new ArrayList<>();
        int i;
        for (i = 1; i < 25; i++) {
            ItemStack item = new ItemStack(Material.valueOf(FactionsPlugin.instance.getConfig().getString("fshield.DisplayItem.Type")));
            ItemMeta m = item.getItemMeta();
            long now = System.currentTimeMillis();
            String timenow = formatTime(now);

            long from = now+i*3600000;
            String timefrom = formatTime(from);


            long to = from;
            to += FactionsPlugin.instance.getConfig().getInt("fshield.Duration") * 3600000;
            String timeto = formatTime(to);

            m.setDisplayName(Util.color(FactionsPlugin.instance.getConfig().getString("fshield.DisplayItem.Name").replace("{hours}", "" + i)));
            ArrayList<String> lore = new ArrayList<String>();
            for(String l : FactionsPlugin.instance.getConfig().getStringList("fshield.DisplayItem.Lore")){
                lore.add(Util.color(l.replace("{now}", formatTime(now)).replace("{from}", formatTime(from)).replace("{to}", formatTime(to))));
            }
            m.setLore(lore);
            item.setItemMeta(m);

            long finalFrom = from;
            long finalTo = to;
            GUIItems.add(new GuiItem(item, e -> {
                fplayer.getFaction().setShieldPlanned(true);
                fplayer.getFaction().setShieldRunning(false);
                fplayer.getFaction().setShieldStart(finalFrom);
                fplayer.getFaction().setShieldEnd(finalTo);
                fplayer.msg(TL.SHIELD_SCHEDULED.toString().replace("{from}", formatTime(finalFrom)).replace("{to}", formatTime(finalTo)));
                e.setCancelled(true);
                fplayer.getPlayer().closeInventory();
            }
            ));
        }
        pane.populateWithGuiItems(GUIItems);
        gui.addPane(pane);
        gui.update();
        gui.show(fplayer.getPlayer());
    }
}
