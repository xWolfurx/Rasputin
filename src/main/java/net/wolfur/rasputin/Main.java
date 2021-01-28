package net.wolfur.rasputin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.wolfur.rasputin.command.*;
import net.wolfur.rasputin.command.clan.Command_Clan;
import net.wolfur.rasputin.command.fun.Command_Huen;
import net.wolfur.rasputin.command.moderation.Command_Ping;
import net.wolfur.rasputin.command.notify.Command_Notify;
import net.wolfur.rasputin.command.punish.*;
import net.wolfur.rasputin.command.raid.Command_Raid;
import net.wolfur.rasputin.command.registration.Command_Register;
import net.wolfur.rasputin.command.reset.Command_Daily;
import net.wolfur.rasputin.command.statistic.*;
import net.wolfur.rasputin.command.vendor.Command_Gunsmith;
import net.wolfur.rasputin.command.vendor.Command_Spider;
import net.wolfur.rasputin.command.vendor.Command_Xur;
import net.wolfur.rasputin.core.CommandHandler;
import net.wolfur.rasputin.database.SQLManager;
import net.wolfur.rasputin.file.builder.yaml.FileConfiguration;
import net.wolfur.rasputin.listeners.Event_GuildMessageReactionAddEvent;
import net.wolfur.rasputin.listeners.Event_MessageReceivedEvent;
import net.wolfur.rasputin.manager.CoreManager;
import net.wolfur.rasputin.manager.FileManager;
import net.wolfur.rasputin.task.ReloadTask;
import net.wolfur.rasputin.task.VendorNotificationTask;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.web.WebServer;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;

public class Main {

    private static JDABuilder builder;
    private static JDA jda;

    private static Guild guild;

    private static long startTime;

    private static WebServer webServer;

    private static FileManager fileManager;
    private static SQLManager sqlManager;
    private static CoreManager coreManager;

    private static ReloadTask reloadTask;
    private static VendorNotificationTask vendorNotificationTask;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.error("Rasputin is shutting down...", true);
            Logger.warning("Closing database connection...", true);
            Main.sqlManager.getUpdater().saveAll();
            Main.sqlManager.getUpdater().setActive(false);
            Main.sqlManager.closeConnection();
            Logger.info("Disconnected from database.", true);
            Main.webServer.stop();
            Main.reloadTask.stop();
            Main.vendorNotificationTask.stop();
        }));

        Main.startTime = System.currentTimeMillis();

        System.out.println(" ____       _      ____    ____    _   _   _____   ___   _   _");
        System.out.println("|  _ \\     / \\    / ___|  |  _ \\  | | | | |_   _| |_ _| | \\ | |");
        System.out.println("| |_) |   / _ \\   \\___ \\  | |_) | | | | |   | |    | |  |  \\| |");
        System.out.println("|  _ <   / ___ \\   ___) | |  __/  | |_| |   | |    | |  | |\\  |");
        System.out.println("|_| \\_\\ /_/   \\_\\ |____/  |_|      \\___/    |_|   |___| |_| \\_|");
        System.out.println(" ");
        System.out.println("  ✦ Software by Wolfur.");
        System.out.println("  ✦ You´re not allowed to edit this resource.");
        System.out.println("  ✦ Starting initialization... - Rasputin version " + getVersion());
        System.out.println(" ");

        Main.fileManager = new FileManager();

        if(getFileManager().getChannelFile().getChannel("log") == null || getFileManager().getChannelFile().getChannel("log").getChannelId() == -1L) {
            System.out.println("Please configure id for log channel.");
            try {
                Thread.sleep(5000L);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(-1);
        }

        if(getFileManager().getChannelFile().getChannel("error") == null || getFileManager().getChannelFile().getChannel("error").getChannelId() == -1L) {
            System.out.println("Please configure id for error channel.");
            try {
                Thread.sleep(5000L);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(-1);
        }

        Logger.info("Trying to connect to database. Please wait...", false);
        if(!Main.loadDatabase()) {
            Logger.error("Can´t connect to database. Please check file 'config.yml'!", false);
            try {
                Thread.sleep(5000L);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(-1);
        } else {
            Logger.info("Successfully connected to database.", false);
        }

        Logger.info("Initialize bot...", false);

        try {
            Main.webServer = new WebServer(5080);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Main.builder = JDABuilder.createDefault(Main.getFileManager().getConfigFile().getToken());
        Main.builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        Main.builder.setAutoReconnect(true);
        Main.builder.setStatus(OnlineStatus.ONLINE);
        Main.builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        Main.builder.setActivity(Activity.playing("REBOOTING SYSTEM..."));

        Main.loadEvents();
        Logger.info("Loaded " + Main.loadCommands() + " commands.", true);

        try {
            Main.jda = Main.builder.build();
        } catch (LoginException e) {
            Logger.error("Initialize failed.", false);
            e.printStackTrace();
        }

        while(guild == null) {
            Logger.error("Can´t find guild with id '" + getFileManager().getConfigFile().getGuildId() + "'.", false);
            Logger.error("Try again in five seconds...", false);

            for(Guild guild : getJDA().getGuilds()) {
                if(guild.getIdLong() == getFileManager().getConfigFile().getGuildId()) {
                    Main.guild = guild;
                }
            }

            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Main.coreManager = new CoreManager();

        Main.reloadTask = new ReloadTask();
        Main.reloadTask.start();

        Main.vendorNotificationTask = new VendorNotificationTask();
        Main.vendorNotificationTask.start();

        Logger.info("Initialize successfully.", true);
    }

    public static String getVersion() {
        try {
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("/pom.properties"));

            return properties.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown version";
    }

    private static boolean loadDatabase() {
        FileConfiguration cfg = Main.getFileManager().getConfigFile().getConfig();
        String host = cfg.getString("MySQL.Host");
        String port = cfg.getString("MySQL.Port");
        String username = cfg.getString("MySQL.Username");
        String password = cfg.getString("MySQL.Password");
        String database = cfg.getString("MySQL.Database");
        Main.sqlManager = new SQLManager(host, port, username, password, database);
        return Main.sqlManager.openConnection();
    }

    private static void loadEvents() {
        Main.builder.addEventListeners(new Event_MessageReceivedEvent());
        Main.builder.addEventListeners(new Event_GuildMessageReactionAddEvent());
    }

    private static int loadCommands() {
        //BUNGIE COMMANDS

        //MODERATION
        CommandHandler.commands.put("ping", new Command_Ping());

        CommandHandler.commands.put("raid", new Command_Raid());
        CommandHandler.commands.put("ban", new Command_Ban());
        CommandHandler.commands.put("tempban", new Command_Tempban());
        CommandHandler.commands.put("unban", new Command_Unban());
        CommandHandler.commands.put("activebans", new Command_ActiveBans());
        CommandHandler.commands.put("baninfo", new Command_BanInfo());
        CommandHandler.commands.put("help", new Command_Help());
        CommandHandler.commands.put("xur", new Command_Xur());
        CommandHandler.commands.put("spider", new Command_Spider());
        CommandHandler.commands.put("gunsmith", new Command_Gunsmith());
        CommandHandler.commands.put("clear", new Command_Clear());

        CommandHandler.commands.put("register", new Command_Register());
        CommandHandler.commands.put("characters", new Command_Characters());
        CommandHandler.commands.put("gos", new Command_GoS());
        CommandHandler.commands.put("dsc", new Command_DSC());
        CommandHandler.commands.put("lw", new Command_LW());
        CommandHandler.commands.put("time", new Command_Time());
        CommandHandler.commands.put("last", new Command_Last());
        CommandHandler.commands.put("trials", new Command_Trials());
        CommandHandler.commands.put("loadout", new Command_Loadout());
        CommandHandler.commands.put("prophecy", new Command_Prophecy());
        CommandHandler.commands.put("pit", new Command_Pit());
        CommandHandler.commands.put("throne", new Command_Throne());
        CommandHandler.commands.put("ranking", new Command_Ranking());
        CommandHandler.commands.put("status", new Command_Status());

        CommandHandler.commands.put("clan", new Command_Clan());

        CommandHandler.commands.put("daily", new Command_Daily());

        CommandHandler.commands.put("notify", new Command_Notify());

        CommandHandler.commands.put("huen", new Command_Huen());

        return CommandHandler.commands.size();
    }

    public static FileManager getFileManager() {
        return Main.fileManager;
    }

    public static SQLManager getSQLManager() {
        return Main.sqlManager;
    }

    public static JDABuilder getBuilder() {
        return Main.builder;
    }

    public static JDA getJDA() {
        return Main.jda;
    }

    public static Guild getGuild() {
        return Main.guild;
    }

    public static CoreManager getCoreManager() {
        return Main.coreManager;
    }

    public static ReloadTask getReloadTask() {
        return Main.reloadTask;
    }

    public static VendorNotificationTask getVendorNotificationTask() {
        return Main.vendorNotificationTask;
    }

    public static long getStartTime() {
        return Main.startTime;
    }

}

