package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.Util;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.frame.fshield.FShieldFrame;
import com.massivecraft.factions.zcore.util.TL;

public class CmdForceShield extends FCommand {
    public CmdForceShield() {
        this.aliases.addAll(Aliases.forceshield);
        this.requiredArgs.add("faction");

        this.requirements = new CommandRequirements.Builder(Permission.FORCESHIELD)
                .build();
    }

    @Override
    public void perform(CommandContext context) {

        if(!Permission.FORCESHIELD.has(context.fPlayer.getPlayer(), true)){
            return;
        }
        Faction faction = null;
        if (context.argIsSet(0))
            faction = context.argAsFaction(0);
        if (faction == null)
            return;
        if(faction.getId().equals("0") || faction.getId().equals("-1") || faction.getId().equals("-2")) {
            context.fPlayer.msg(Util.color("&c&l[!] &7Player is not in a faction!"));
            return;
        }
        if(faction.getShieldRunning() || faction.getShieldPlanned()){
            faction.setShieldRunning(false);
            faction.setShieldPlanned(false);
            context.fPlayer.msg(Util.color("&c&l[!] &7Cancelled shield for faction &c" + faction.getTag()));
            faction.msg(Util.color(TL.FORCESHIELD_CANCELLED_BY_ADMIN.toString().replace("{player}", context.player.getName())));
            return;
        } else {
            context.fPlayer.msg(Util.color("&c&l[!] &7Faction &c"  + faction.getTag() + "&7 doesn't have a scheduled shield!"));
            return;
        }
    }


    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_FORCESHIELD_DESCRIPTION;
    }

}
