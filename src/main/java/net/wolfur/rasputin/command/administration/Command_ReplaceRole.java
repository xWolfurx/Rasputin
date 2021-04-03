package net.wolfur.rasputin.command.administration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.core.Command;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_ReplaceRole implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(args.length == 2) {
            Role targetRole = Main.getRoleManager().getRole(args[0]);
            Role replaceRole = Main.getRoleManager().getRole(args[1]);

            if(targetRole == null) {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Die Rolle '" + args[0] + "' existiert nicht in der Datenbank.").build()).complete();
                return;
            }

            if(replaceRole == null) {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Die Rolle '" + args[1] + "' existiert nicht in der Datenbank.").build()).complete();
                return;
            }

            for(BungieUser bungieUser : Main.getCoreManager().getBungieUserManager().getBungieUsers().values()) {
                if(bungieUser.isRegistered()) {
                    if(bungieUser.hasRole(targetRole)) {
                        bungieUser.removeRole(targetRole);
                        bungieUser.addRole(replaceRole);
                    }
                }
            }
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("Die Rolle '" + args[0] + "' wurde mit der Rolle '" + args[1] + "' ersetzt.").build()).complete();
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .ReplaceRole <Role> <ReplaceRole>").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }
}
