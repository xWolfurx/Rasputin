package net.wolfur.rasputin.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.wolfur.rasputin.Main;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Utils {

    public static String convertDateToString(int day) {
        switch(day) {
            case 0:
                return "Sonntag";
            case 1:
                return "Montag";
            case 2:
                return "Dienstag";
            case 3:
                return "Mittwoch";
            case 4:
                return "Donnerstag";
            case 5:
                return "Freitag";
            case 6:
                return "Samstag";
            default:
                return "Unknown";
        }
    }

    public static Emote getEmote(String name) {
        for(Emote emote : Main.getJDA().getEmotes()) {
            if(emote.getName().equalsIgnoreCase(name)) {
                return emote;
            }
        }
        return getErrorEmote();
    }


    private static Emote getErrorEmote() {
        for(Emote emote : Main.getJDA().getEmotes()) {
            if(emote.getName().equalsIgnoreCase("database_error")) {
                return emote;
            }
        }
        return null;
    }

    public static boolean hasRole(Member member, Role role) {
        if(member.getRoles().contains(role)) {
            return true;
        } else {
            return false;
        }
    }

    public static Role getRoleByName(String name) {
        for(Role role : Main.getJDA().getRoles()) {
            if(role.getName().equals(name)) {
                return role;
            }
        }
        return null;
    }

    public static void handleMaintenance(boolean active) {
        if(active) {
            if(!Main.isMaintenance()) {
                Main.setMaintenance(true);
                TextChannel textChannel = Main.getGuild().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("talk").getChannelId());
                if(textChannel != null) {
                    textChannel.sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bungie.net has been brought offline for maintenance." + "\n" + "Commands are disabled.").build()).complete();
                }
            }
        } else {
            if(Main.isMaintenance()) {
                Main.setMaintenance(false);
                Main.getCoreManager().getBungieUserManager().checkUsers();
                TextChannel textChannel = Main.getGuild().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("talk").getChannelId());
                if(textChannel != null) {
                    textChannel.sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bungie.net maintenance is completed." + "\n" + "Commands were reactivated.").build()).complete();
                }
            }
        }
    }
    public static final Consumer<Throwable> ignore = ignored -> {

    };

}
