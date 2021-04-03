package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.character.DestinyCharacter;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.bungie.type.DestinyActivityModeType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;
import net.wolfur.rasputin.util.Utils;
import sun.security.krb5.internal.crypto.Des;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Command_Time implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            if(args.length == 0) {
                bungieUser.requestStats();
                bungieUser.requestHistoricalStats();
                bungieUser.requestDestinyActivityHistory(DestinyActivityModeType.TRIALS_OF_OSIRIS, 250, true);

                List<JsonObject> stats = bungieUser.getStats();
                List<JsonObject> trials = bungieUser.getActivityHistory(DestinyActivityModeType.TRIALS_OF_OSIRIS);

                List<JsonObject> tower = bungieUser.getHistoricalStats(1502633527L);
                tower.addAll(bungieUser.getHistoricalStats(3737830648L));
                tower.addAll(bungieUser.getHistoricalStats(3903562779L));

                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, stats, trials, tower).build()).queue();
            } else if(args.length == 1) {
                User targetUser = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                if(targetUser != null) {
                    BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                    if(targetBungieUser.isRegistered()) {
                        targetBungieUser.requestStats();
                        targetBungieUser.requestHistoricalStats();
                        targetBungieUser.requestDestinyActivityHistory(DestinyActivityModeType.TRIALS_OF_OSIRIS, 250, true);

                        List<JsonObject> stats = targetBungieUser.getStats();
                        List<JsonObject> trials = targetBungieUser.getActivityHistory(DestinyActivityModeType.TRIALS_OF_OSIRIS);

                        List<JsonObject> tower = targetBungieUser.getHistoricalStats(1502633527L);
                        tower.addAll(targetBungieUser.getHistoricalStats(3737830648L));
                        tower.addAll(targetBungieUser.getHistoricalStats(3903562779L));

                        event.getTextChannel().sendMessage(this.createEmbedBuilder(targetBungieUser, stats, trials, tower).build()).queue();
                    } else {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Dieser User ist noch nicht registriert.").build()).queue(message -> {
                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                        });
                    }
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User existiert nicht.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Time [@Player]").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte registriere dich, um diesen Befehl nutzen zu können." + "\n\n" + "Registriere dich mit **.Register**.").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, List<JsonObject> stats, List<JsonObject> trials, List<JsonObject> tower) {

        boolean bungieError = false, trialsBungieError = false;

        long globalPlayTime = 0L;
        long pvpPlaytime = 0L, patrolPlaytime = 0L, raidPlaytime = 0L, storyPlaytime = 0L, strikePlaytime = 0L, pvePlaytime = 0L, gambitPlaytime = 0L;
        long trialsPlaytime = 0L;
        long towerPlaytime = 0L;

        for(DestinyCharacter destinyCharacter : targetUser.getDestinyCharacters()) {
            globalPlayTime += (destinyCharacter.getMinutesPlayedTotal() * 60);
        }

        for(JsonObject towerObject : tower) {
            towerPlaytime += towerObject.getAsJsonObject("values").getAsJsonObject("activitySecondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
        }

        if(stats.size() > 0) {
            for(JsonObject activity : stats) {
                try {
                    pvpPlaytime += activity.getAsJsonObject("Response").getAsJsonObject("allPvP").getAsJsonObject("allTime").getAsJsonObject("secondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
                    patrolPlaytime += activity.getAsJsonObject("Response").getAsJsonObject("patrol").getAsJsonObject("allTime").getAsJsonObject("secondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
                    raidPlaytime += activity.getAsJsonObject("Response").getAsJsonObject("raid").getAsJsonObject("allTime").getAsJsonObject("secondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
                    storyPlaytime += activity.getAsJsonObject("Response").getAsJsonObject("story").getAsJsonObject("allTime").getAsJsonObject("secondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
                    strikePlaytime += activity.getAsJsonObject("Response").getAsJsonObject("allStrikes").getAsJsonObject("allTime").getAsJsonObject("secondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
                    pvePlaytime += activity.getAsJsonObject("Response").getAsJsonObject("allPvE").getAsJsonObject("allTime").getAsJsonObject("secondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
                    gambitPlaytime += activity.getAsJsonObject("Response").getAsJsonObject("allPvECompetitive").getAsJsonObject("allTime").getAsJsonObject("secondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
                } catch (NullPointerException e) {
                    continue;
                }
            }
        } else {
            bungieError = true;
            Logger.warning("No stats found for " + targetUser.getUser().getName() + ".", true);
        }

        if(trials.size() > 0) {
            for(JsonObject trialsObject : trials) {
                try {
                    trialsPlaytime += trialsObject.getAsJsonObject("values").getAsJsonObject("timePlayedSeconds").getAsJsonObject("basic").get("value").getAsLong();
                } catch (NullPointerException e) {
                    continue;
                }
            }
        } else {
            trialsBungieError = true;
            Logger.warning("No 'Trials of Osiris' activities found for " + targetUser.getUser().getName() + ".", true);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Statistics > Time Played Summary: " + targetUser.getUser().getName())
                .addField("Total w/ Orbit", TimeUtil.timeToString(globalPlayTime * 1000, true), true)
                .addField("Tower", TimeUtil.timeToString(towerPlaytime * 1000, true), true)
                .addField("PvE", bungieError ? "Bungie API Error" : TimeUtil.timeToString(pvePlaytime * 1000, true), true)
                .addField("Crucible", bungieError ? "Bungie API Error" : TimeUtil.timeToString(pvpPlaytime * 1000, true), true)
                .addField("Gambit", bungieError ? "Bungie API Error" : TimeUtil.timeToString(gambitPlaytime * 1000, true), true)
                .addField("Patrols", bungieError ? "Bungie API Error" : TimeUtil.timeToString(patrolPlaytime * 1000, true), true)
                .addField("Strikes", bungieError ? "Bungie API Error" : TimeUtil.timeToString(strikePlaytime * 1000, true), true)
                .addField("Raids", bungieError ? "Bungie API Error" : TimeUtil.timeToString(raidPlaytime * 1000, true), true)
                .addField("Trials of Osiris", trialsBungieError ? "Bungie API Error" : TimeUtil.timeToString(trialsPlaytime * 1000, true), true)
                .setThumbnail("http://vhost106.dein-gameserver.tech/rasputin.png");

        return embedBuilder;
    }

    private double round(double value, int decimalPoints) {
        double d = Math.pow(10, decimalPoints);
        return Math.round(value * d) / d;
    }
}
