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

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_LW implements Command {

    private static long[] allTriumphs = { 3448775736L, 4078980921L, 717856991L, 628370196L, 1359055399L, 380332968L, 3000516033L, 2826160801L, 623283604L, 2588923804L, 342038729L,
                                            1847670729L, 3533973498L, 989244596L, 3234595894L, 1711136422L };

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(Main.getFileManager().getChannelFile().isCommandChannel(event.getTextChannel().getIdLong())) {
            BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
            if(bungieUser.isRegistered()) {
                if(args.length == 0) {
                    bungieUser.requestProfile(ComponentType.METRICS);
                    bungieUser.requestProfile(ComponentType.RECORDS);
                    bungieUser.requestProfile(ComponentType.COLLECTIBLES);
                    bungieUser.requestHistoricalStats();

                    JsonObject lastWishDataObject = bungieUser.getProfile(ComponentType.METRICS);
                    List<JsonObject> lastWishActivities = bungieUser.getHistoricalStats(2122313384L);

                    event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, lastWishDataObject, lastWishActivities).build()).queue();
                } else if(args.length == 1) {
                    User targetUser = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                    if (targetUser != null) {
                        BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                        if (targetBungieUser.isRegistered()) {
                            targetBungieUser.requestProfile(ComponentType.METRICS);
                            targetBungieUser.requestProfile(ComponentType.RECORDS);
                            targetBungieUser.requestProfile(ComponentType.COLLECTIBLES);
                            targetBungieUser.requestHistoricalStats();

                            JsonObject lastWishDataObject = targetBungieUser.getProfile(ComponentType.METRICS);
                            List<JsonObject> lastWishActivities = targetBungieUser.getHistoricalStats(2122313384L);

                            event.getTextChannel().sendMessage(this.createEmbedBuilder(targetBungieUser, lastWishDataObject, lastWishActivities).build()).queue();
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
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .LW [@Player]").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte registriere dich, um diesen Befehl nutzen zu können." + "\n\n" + "Registriere dich mit **.Register**.").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, JsonObject lastWishDataObject, List<JsonObject> lastWishActivities) {
        JsonObject metricsData = lastWishDataObject.getAsJsonObject("Response").getAsJsonObject("metrics").getAsJsonObject("data").getAsJsonObject("metrics");

        int totalCompletions = metricsData.getAsJsonObject("905240985").getAsJsonObject("objectiveProgress").get("progress").getAsInt();

        int kills = 0, deaths = 0, assists = 0, precisionKills = 0, completedTriumphs = 0, maxTriumphs = allTriumphs.length;
        long playtime = 0L, fastestCompletion = 0L;
        double killsDeathsRatio = 0.0D, killsDeathsAssistsRatio = 0.0D;
        boolean bungieError = false;

        if(lastWishActivities.size() > 0) {
            for(JsonObject activity : lastWishActivities) {
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
            Logger.warning("No 'Last Wish' activities found for " + targetUser.getUser().getName() + ".", true);
        }

        boolean flawless = targetUser.ownsTriumph(380332968L);
        boolean dayOne = targetUser.ownsCollectible(1171206947L);
        boolean oneThousandVoices = targetUser.ownsCollectible(199171385L);

        if(dayOne) maxTriumphs++;

        for(long triumphHash : allTriumphs) {
            if (targetUser.ownsTriumph(triumphHash)) completedTriumphs++;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Statistics > Last Wish: " + targetUser.getUser().getName())
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
                .addField("One Thousand Voices", oneThousandVoices ? "Erhalten" : "Nicht erhalten", true)
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
