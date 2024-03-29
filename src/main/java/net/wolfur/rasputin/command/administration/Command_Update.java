package net.wolfur.rasputin.command.administration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.core.Command;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Update implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("Raids")) {
                long startTime = System.currentTimeMillis();
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setDescription("Importing raids from database." + "\n" + "Please wait...").build()).complete();
                Main.getCoreManager().getRaidManager().resetRaidManager();
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("Update successfully. (" + (System.currentTimeMillis() - startTime) + " ms)").build()).complete();
            } else if(args[0].equalsIgnoreCase("Weapons")) {
                long startTime = System.currentTimeMillis();
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setDescription("Importing weapons from database." + "\n" + "Please wait...").build()).complete();
                Main.getWeaponManager().reloadWeapons();
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("Update successfully. (" + (System.currentTimeMillis() - startTime) + " ms)").build()).complete();
            } else if(args[0].equalsIgnoreCase("Emotes")) {
                long startTime = System.currentTimeMillis();
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setDescription("Importing emotes from database." + "\n" + "Please wait...").build()).complete();
                Main.getEmoteManager().reloadEmotes();
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("Update successfully. (" + (System.currentTimeMillis() - startTime) + " ms)").build()).complete();
            } else if(args[0].equalsIgnoreCase("roles")) {
                long startTime = System.currentTimeMillis();
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setDescription("Importing roles from database." + "\n" + "Please wait...").build()).complete();
                Main.getRoleManager().reloadRoles();
                for(BungieUser bungieUser : Main.getCoreManager().getBungieUserManager().getBungieUsers().values()) {
                    if(bungieUser.isRegistered()) bungieUser.getClanUser().checkClanUser();
                }
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("Update successfully. (" + (System.currentTimeMillis() - startTime) + " ms)").build()).complete();
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Dieser Typ existiert nicht. \n\nBitte verwende **.help update** um dir die möglichen Typen aufzulisten.").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Update <Type>").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }
}
