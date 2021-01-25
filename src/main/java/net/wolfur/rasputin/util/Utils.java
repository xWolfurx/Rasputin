package net.wolfur.rasputin.util;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.wolfur.rasputin.Main;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

    public static final Consumer<Throwable> ignore = ignored -> {

    };

}
