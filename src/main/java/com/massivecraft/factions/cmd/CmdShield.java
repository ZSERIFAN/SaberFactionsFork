package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.frame.fshield.FShieldFrame;
import com.massivecraft.factions.zcore.util.TL;
import org.bukkit.ChatColor;

public class CmdShield extends FCommand {

    public CmdShield() {
        this.aliases.addAll(Aliases.shield);

        this.requirements = new CommandRequirements.Builder(Permission.SHIELD)
                .build();
    }

    private String formatTime(long x){
        return String.format("%02d:%02d:%02d", (x / (1000 * 60 * 60)) % 24, (x / (1000 * 60)) % 60, (x / 1000) % 60);
    }

    @Override
    public void perform(CommandContext context) {

        if (!FactionsPlugin.getInstance().getConfig().getBoolean("fshield.Enabled")) {
            context.fPlayer.msg(TL.COMMAND_SHIELD_DISABLED);
            return;
        }
        if(context.fPlayer.getFactionId().equals("0") || context.fPlayer.getFactionId().equals("-1") || context.fPlayer.getFactionId().equals("-2")){
            context.fPlayer.msg(TL.COMMAND_SHOW_NOFACTION_SELF);
            return;
        }

        if(context.fPlayer.getFaction().getShieldPlanned() && !context.fPlayer.getFaction().getShieldRunning()) {

            context.fPlayer.msg(TL.SHIELD_PLANNED.toString().replace("{from}", formatTime(context.fPlayer.getFaction().getShieldStart())).replace("{to}", formatTime(context.fPlayer.getFaction().getShieldEnd())));
            return;
        }

        if(context.fPlayer.getFaction().getShieldRunning()){
            context.fPlayer.msg(TL.SHIELD_ALREADY_RUNNING.toString().replace("{time}", formatTime(context.fPlayer.getFaction().getShieldEnd()-System.currentTimeMillis())));
            return;
        }

        new FShieldFrame(context.faction).buildGUI(context.fPlayer);
    }


    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SHIELD_DESCRIPTION;
    }
}
