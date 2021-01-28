package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.bungie.type.DestinyActivityModeType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Command_Pit implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if (bungieUser.isRegistered()) {
            if(args.length == 0) {
                bungieUser.requestProfile(ComponentType.METRICS);
                bungieUser.requestDestinyActivityHistory(DestinyActivityModeType.DUNGEON, 250, true);

                JsonObject pitOfHeresyDataObject = bungieUser.getProfile(ComponentType.METRICS);
                List<JsonObject> dungeonActivities = bungieUser.getActivityHistory(DestinyActivityModeType.DUNGEON);

                List<JsonObject> pitOfHeresyActivities = dungeonActivities.stream().filter(activity -> activity.getAsJsonObject("activityDetails").get("directorActivityHash").getAsLong() == 1375089621L).collect(Collectors.toList());

                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, pitOfHeresyDataObject, pitOfHeresyActivities).build()).complete();
            } else if(args.length == 1) {
                User targetUser = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                if (targetUser != null) {
                    BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                    if (targetBungieUser.isRegistered()) {
                        targetBungieUser.requestProfile(ComponentType.METRICS);
                        targetBungieUser.requestDestinyActivityHistory(DestinyActivityModeType.DUNGEON, 250, true);

                        JsonObject pitOfHeresyDataObject = targetBungieUser.getProfile(ComponentType.METRICS);
                        List<JsonObject> dungeonActivities = targetBungieUser.getActivityHistory(DestinyActivityModeType.DUNGEON);

                        List<JsonObject> pitOfHeresyActivities = dungeonActivities.stream().filter(activity -> activity.getAsJsonObject("activityDetails").get("directorActivityHash").getAsLong() == 1375089621L).collect(Collectors.toList());

                        event.getTextChannel().sendMessage(this.createEmbedBuilder(targetBungieUser, pitOfHeresyDataObject, pitOfHeresyActivities).build()).complete();
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
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Pit [@Player]").build()).queue(message -> {
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

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, JsonObject pitOfHeresyDataObject, List<JsonObject> pitOfHeresyActivities) {
        JsonObject metricsData = pitOfHeresyDataObject.getAsJsonObject("Response").getAsJsonObject("metrics").getAsJsonObject("data").getAsJsonObject("metrics");

        int completions = metricsData.getAsJsonObject("1451729471").getAsJsonObject("objectiveProgress").get("progress").getAsInt();
        boolean flawless = targetUser.ownsTriumph(245952203L);
        boolean soloFlawless = targetUser.ownsTriumph(3950599483L);

        int kills = 0, deaths = 0, assists = 0;
        long playtime = 0L;
        double killsDeathsRatio = 0.0D, killsDeathsAssistsRatio = 0.0D;

        boolean bungieError = false;

        if(pitOfHeresyActivities.size() > 0) {
            for(JsonObject activity : pitOfHeresyActivities) {
                try {
                    kills += activity.getAsJsonObject("values").getAsJsonObject("kills").getAsJsonObject("basic").get("value").getAsInt();
                    deaths += activity.getAsJsonObject("values").getAsJsonObject("deaths").getAsJsonObject("basic").get("value").getAsInt();
                    assists += activity.getAsJsonObject("values").getAsJsonObject("assists").getAsJsonObject("basic").get("value").getAsInt();
                    playtime += activity.getAsJsonObject("values").getAsJsonObject("timePlayedSeconds").getAsJsonObject("basic").get("value").getAsLong();

                    if(deaths == 0) {
                        killsDeathsRatio = kills;
                        killsDeathsAssistsRatio = (double)kills + (double)assists;
                    } else {
                        killsDeathsRatio = (((double) kills) / ((double) deaths));
                        killsDeathsAssistsRatio = (((double) kills) + ((double) assists)) / ((double) deaths);
                    }
                } catch (NullPointerException e) {
                    continue;
                }
            }
        } else {
            bungieError = true;
            Logger.warning("No 'Pit of Heresy' activities found for " + targetUser.getUser().getName() + ".", true);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Statistics > Pit of Heresy: " + targetUser.getUser().getName())
                .addField("Completions", String.valueOf(completions), true)
                .addField("Kills", bungieError ? "Bungie API Error" : this.formatInteger(kills), true)
                .addField("Deaths", bungieError ? "Bungie API Error" : this.formatInteger(deaths), true)
                .addField("Assists", bungieError ? "Bungie API Error" : this.formatInteger(assists), true)
                .addField("K/D", bungieError ? "Bungie API Error" : String.valueOf(this.round(killsDeathsRatio, 2)), true)
                .addField("(K+A)/D", bungieError ? "Bungie API Error" : String.valueOf(this.round(killsDeathsAssistsRatio, 2)), true)
                .addField("Playtime", bungieError ? "Bungie API Error" : TimeUtil.timeToString((playtime * 1000), true), true)
                .addField("Flawless",  flawless ? "Abgeschlossen" : "Nicht abgeschlossen", true)
                .addField("Solo & Flawless", soloFlawless ? "Abgeschlossen" : "Nicht abgeschlossen", true)
                .setThumbnail("https://www.bungie.net/common/destiny2_content/icons/DestinyActivityModeDefinition_f20ebb76bee675ca429e470cec58cc7b.png");

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
