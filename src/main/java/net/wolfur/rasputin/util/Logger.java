package net.wolfur.rasputin.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.web.StatusCode;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static FileWriter fileWriter;

    public static String Reset = (char)27 + "[0m";
    public static String Red = (char)27 + "[31m";
    public static String Green = (char)27 + "[32m";
    public static String Yellow = (char)27 + "[33m";
    public static String Blue = (char)27 + "[34m";
    public static String Magenta = (char)27 + "[35m";
    public static String Cyan = (char)27 + "[36m";
    public static String Bold = (char)27 + "[1m";
    public static String StopBold = (char)27 + "[21m";
    public static String Underline = (char)27 + "[4m";
    public static String StopUnderline = (char)27 + "[24m";

    static {
        try {
            fileWriter = new FileWriter("logs/" + simpleDateFormat.format(new Date()) + ".json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void error(Object message, boolean discordMessage) {
        System.out.println(simpleDateFormat.format(new Date()) + " - [" + Red + "ERROR" + Reset + "] -> " + message.toString() + Reset);
        try {
            fileWriter.write(simpleDateFormat.format(new Date()) + " - [ERROR] -> " + message.toString() + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(discordMessage) {
            if (Main.getJDA() != null) {
                TextChannel textChannel = Main.getGuild().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("error").getChannelId());
                textChannel.sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("[" + simpleDateFormat.format(new Date()) + "] " + message.toString()).build()).queue();
            }
        }
    }

    public static void warning(Object message, boolean discordMessage) {
        System.out.println(simpleDateFormat.format(new Date()) + " - [" + Yellow + "WARNING" + Reset + "] -> " + message.toString() + Reset);
        try {
            fileWriter.write(simpleDateFormat.format(new Date()) + " - [WARNING] -> " + message.toString() + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(discordMessage) {
            if (Main.getJDA() != null) {
                TextChannel textChannel = Main.getGuild().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("log").getChannelId());
                textChannel.sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setDescription("[" + simpleDateFormat.format(new Date()) + "] " + message.toString()).build()).queue();
            }
        }
    }

    public static void info(Object message, boolean discordMessage) {
        System.out.println(simpleDateFormat.format(new Date()) + " - [" + Green + "INFO" + Reset + "] -> " + message.toString() + Reset);
        try {
            fileWriter.write(simpleDateFormat.format(new Date()) + " - [INFO] -> " + message.toString() + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(discordMessage) {
            if (Main.getJDA() != null) {
                TextChannel textChannel = Main.getGuild().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("log").getChannelId());
                textChannel.sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("[" + simpleDateFormat.format(new Date()) + "] " + message.toString()).build()).queue();
            }
        }
    }

    public static void requestRefused(Object message, int statusCode, User user) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setColor(Color.RED)
                                    .setDescription("[" + simpleDateFormat.format(new Date()) + "] " + message.toString() + "\n" +
                                            "(" + StatusCode.getByCode(statusCode).getBetterName() + ")" + "\n" +
                                            "Affected User: " + user.getAsMention());

        if(statusCode == 503) {
            Utils.handleMaintenance(true);
        } else {
            if(Main.isMaintenance()) {
                Utils.handleMaintenance(false);
            }
        }

        System.out.println(simpleDateFormat.format(new Date()) + " - [" + Green + "INFO" + Reset + "] -> " + message.toString() + Reset);
        try {
            fileWriter.write(simpleDateFormat.format(new Date()) + " - [INFO] -> " + message.toString() + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Main.getJDA() != null) {
            TextChannel textChannel = Main.getGuild().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("error").getChannelId());
            textChannel.sendMessage(embedBuilder.build()).queue();
        }
    }

}
