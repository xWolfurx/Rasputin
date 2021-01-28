package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_DSC implements Command {

    private static long[] allTriumphs = { 3185876102L, 2699580344L, 3560923614L, 518342793L, 3834307795L, 3875695735L, 3200831458L, 513707022L, 564366615L, 134885948L, 3323713181L,
                                            992355976L, 22094034L, 4216504853L, 64856166L, 3771160417L, 337542929L, 1277450448L, 2530940166L, 1487317889L };

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            if(args.length == 0) {
                bungieUser.requestProfile(ComponentType.METRICS);
                bungieUser.requestProfile(ComponentType.RECORDS);
                bungieUser.requestProfile(ComponentType.COLLECTIBLES);
                bungieUser.requestHistoricalStats();

                JsonObject deepStoneCryptDataObject = bungieUser.getProfile(ComponentType.METRICS);
                List<JsonObject> deepStoneCryptActivities = bungieUser.getHistoricalStats(910380154L);

                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, deepStoneCryptDataObject, deepStoneCryptActivities).build()).queue();
            } else if(args.length == 1) {
                User targetUser = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                if(targetUser != null) {
                    BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                    if (targetBungieUser.isRegistered()) {
                        targetBungieUser.requestProfile(ComponentType.METRICS);
                        targetBungieUser.requestProfile(ComponentType.RECORDS);
                        targetBungieUser.requestProfile(ComponentType.COLLECTIBLES);
                        targetBungieUser.requestHistoricalStats();

                        JsonObject deepStoneCryptDataObject = targetBungieUser.getProfile(ComponentType.METRICS);
                        List<JsonObject> deepStoneCryptActivities = targetBungieUser.getHistoricalStats(910380154L);

                        event.getTextChannel().sendMessage(this.createEmbedBuilder(targetBungieUser, deepStoneCryptDataObject, deepStoneCryptActivities).build()).queue();
                    } else {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der angegebene User hat sich noch nicht registriert.").build()).queue(message -> {
                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                        });
                    }
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User existiert nicht.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .DSC [@Spieler]").build()).queue(message -> {
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

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, JsonObject deepStoneCryptDataObject, List<JsonObject> deepStoneCryptActivities) {
        JsonObject metricsData = deepStoneCryptDataObject.getAsJsonObject("Response").getAsJsonObject("metrics").getAsJsonObject("data").getAsJsonObject("metrics");

        int totalCompletions = metricsData.getAsJsonObject("954805812").getAsJsonObject("objectiveProgress").get("progress").getAsInt();

        int kills = 0, deaths = 0, assists = 0, precisionKills = 0, completedTriumphs = 0, maxTriumphs = allTriumphs.length;
        long playtime = 0L, fastestCompletion = 0L;
        double killsDeathsRatio = 0.0D, killsDeathsAssistsRatio = 0.0D;
        boolean bungieError = false;

        if(deepStoneCryptActivities.size() > 0) {
            for(JsonObject activity : deepStoneCryptActivities) {
                try {
                    long tempFastestCompletion = activity.getAsJsonObject("values").getAsJsonObject("fastestCompletionMsForActivity").getAsJsonObject("basic").get("value").getAsLong();
                    playtime += activity.getAsJsonObject("values").getAsJsonObject("activitySecondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
                    kills += activity.getAsJsonObject("values").getAsJsonObject("activityKills").getAsJsonObject("basic").get("value").getAsInt();
                    deaths += activity.getAsJsonObject("values").getAsJsonObject("activityDeaths").getAsJsonObject("basic").get("value").getAsInt();
                    assists += activity.getAsJsonObject("values").getAsJsonObject("activityAssists").getAsJsonObject("basic").get("value").getAsInt();
                    precisionKills += activity.getAsJsonObject("values").getAsJsonObject("activityPrecisionKills").getAsJsonObject("basic").get("value").getAsInt();
                    killsDeathsRatio += activity.getAsJsonObject("values").getAsJsonObject("activityKillsDeathsRatio").getAsJsonObject("basic").get("value").getAsDouble();
                    killsDeathsAssistsRatio += activity.getAsJsonObject("values").getAsJsonObject("activityKillsDeathsAssists").getAsJsonObject("basic").get("value").getAsDouble();

                    if(fastestCompletion == 0L) fastestCompletion = tempFastestCompletion;
                    if(tempFastestCompletion < fastestCompletion) fastestCompletion = tempFastestCompletion;
                } catch (NullPointerException e) {
                    continue;
                }
            }
        } else {
            bungieError = true;
            Logger.warning("No 'Deep Stone Crypt' activities found for " + targetUser.getUser().getName() + ".", true);
        }

        boolean flawless = targetUser.ownsTriumph(3560923614L);
        boolean dayOne = targetUser.ownsTriumph(2699580344L);
        boolean eyesOfTomorrow = targetUser.ownsCollectible(753200559L);
        boolean seal = targetUser.ownsTriumph(540377256L);

        if(!dayOne) maxTriumphs--;

        for(long triumphHash : allTriumphs) {
            if (targetUser.ownsTriumph(triumphHash)) completedTriumphs++;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Statistics > Deep Stone Crypt: " + targetUser.getUser().getName())
                .addField("Completions", totalCompletions + "x", true)
                .addField("Fastest Completion", bungieError ? "Bungie API Error" : TimeUtil.timeToString(fastestCompletion, true), true)
                .addField("Playtime", bungieError ? "Bungie API Error" : TimeUtil.timeToString(playtime * 1000, true), true)
                .addField("Kills", bungieError ? "Bungie API Error" : this.formatInteger(kills), true)
                .addField("Deaths", bungieError ? "Bungie API Error" : this.formatInteger(deaths), true)
                .addField("Assists", bungieError ? "Bungie API Error" : this.formatInteger(assists), true)
                .addField("K/D", bungieError ? "Bungie API Error" : String.valueOf(this.round((killsDeathsRatio / targetUser.getDestinyCharacters().size()), 2)) , true)
                .addField("(K+A)/D", bungieError ? "Bungie API Error" : String.valueOf(this.round((killsDeathsAssistsRatio / targetUser.getDestinyCharacters().size()), 2)), true)
                .addField("Precision Kills", bungieError ? "Bungie API Error" : String.valueOf(precisionKills), true)
                .addField("Flawless", flawless ? "Abgeschlossen" : "Nicht abgeschlossen", true)
                .addField("Day One", dayOne ? "Abgeschlossen" : "Nicht abgeschlossen", true)
                .addField("Triumphs", completedTriumphs + "/" + maxTriumphs, true)
                .addField("Eyes of Tomorrow", eyesOfTomorrow ? "Erhalten" : "Nicht erhalten", true)
                .addField("Seal", seal ? Utils.getEmote("DescendantUnlocked").getAsMention() : Utils.getEmote("DescendantLocked").getAsMention(), true)
                .setThumbnail("https://www.bungie.net/common/destiny2_content/icons/8b1bfd1c1ce1cab51d23c78235a6e067.png");

        return embedBuilder;
    }

    private double round(double value, int decimalPoints) {
        double d = Math.pow(10, decimalPoints);
        return Math.round(value * d) / d;
    }

    private String formatInteger(int value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("###,###.###", symbols);

        return decimalFormat.format(value);
    }
}
