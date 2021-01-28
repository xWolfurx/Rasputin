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

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_Trials implements Command {

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
                bungieUser.requestDestinyActivityHistory(DestinyActivityModeType.TRIALS_OF_OSIRIS, 250, true);

                JsonObject trialsDataObject = bungieUser.getProfile(ComponentType.METRICS);
                List<JsonObject> trialsActivities = bungieUser.getActivityHistory(DestinyActivityModeType.TRIALS_OF_OSIRIS);

                event.getTextChannel().sendMessage(this.createEmbedBuilder(event.getAuthor(), trialsDataObject, trialsActivities).build()).complete();
            } else if(args.length == 1) {
                User targetUser = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                if (targetUser != null) {
                    BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                    if (targetBungieUser.isRegistered()) {
                        targetBungieUser.requestProfile(ComponentType.METRICS);
                        targetBungieUser.requestDestinyActivityHistory(DestinyActivityModeType.TRIALS_OF_OSIRIS, 250, true);

                        JsonObject trialsDataObject = targetBungieUser.getProfile(ComponentType.METRICS);
                        List<JsonObject> trialsActivities = targetBungieUser.getActivityHistory(DestinyActivityModeType.TRIALS_OF_OSIRIS);

                        event.getTextChannel().sendMessage(this.createEmbedBuilder(targetUser, trialsDataObject, trialsActivities).build()).complete();
                    } else {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Dieser Spieler ist noch nicht registriert.").build()).queue(message -> {
                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                        });
                    }
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der Spieler existiert nicht.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Trials [@Player]").build()).queue(message -> {
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

    private EmbedBuilder createEmbedBuilder(User targetUser, JsonObject trialsDataObject, List<JsonObject> trialsActivities) {
        JsonObject metricsData = trialsDataObject.getAsJsonObject("Response").getAsJsonObject("metrics").getAsJsonObject("data").getAsJsonObject("metrics");

        int flawlessTickets = metricsData.getAsJsonObject("1765255052").getAsJsonObject("objectiveProgress").get("progress").getAsInt();
        int wins = metricsData.getAsJsonObject("1365664208").getAsJsonObject("objectiveProgress").get("progress").getAsInt();

        double winRate = trialsActivities.size() > 0 ? (double)wins / (double)trialsActivities.size() : 0.0D;

        int kills = 0, deaths = 0, assists = 0, mostKills = 0;
        long playtime = 0L;
        double killsDeathsRatio = 0.0D, killsDeathsAssistsRatio = 0.0D;

        if(trialsActivities.size() > 0) {
            for(JsonObject activity : trialsActivities) {
                try {
                    int newKills = activity.getAsJsonObject("values").getAsJsonObject("kills").getAsJsonObject("basic").get("value").getAsInt();

                    kills += newKills;
                    deaths += activity.getAsJsonObject("values").getAsJsonObject("deaths").getAsJsonObject("basic").get("value").getAsInt();
                    assists += activity.getAsJsonObject("values").getAsJsonObject("assists").getAsJsonObject("basic").get("value").getAsInt();
                    playtime += activity.getAsJsonObject("values").getAsJsonObject("timePlayedSeconds").getAsJsonObject("basic").get("value").getAsLong();

                    if (mostKills == 0) mostKills = newKills;
                    if (mostKills < newKills) mostKills = newKills;

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
            Logger.warning("No 'Trials of Osiris' activities found for " + targetUser.getName() + ".", true);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Statistics > Trials of Osiris: " + targetUser.getName())
                .addField("Kills", String.valueOf(kills), true)
                .addField("Deaths", String.valueOf(deaths), true)
                .addField("Assists", String.valueOf(assists), true)
                .addField("K/D", String.valueOf(this.round(killsDeathsRatio, 2)), true)
                .addField("(K+A)/D", String.valueOf(this.round(killsDeathsAssistsRatio, 2)), true)
                .addField("Most Kills in a Game", String.valueOf(mostKills), true)
                .addField("Total Flawless", flawlessTickets + "x", true)
                .addField("Win Rate", this.round(winRate * 100, 2) + "% (" + wins + "/" + trialsActivities.size() + ")", true)
                .addField("Playtime", TimeUtil.timeToString((playtime * 1000), true), true)
                .setThumbnail("https://www.bungie.net/common/destiny2_content/icons/DestinyActivityModeDefinition_e35792b49b249ca5dcdb1e7657ca42b6.png");

        return embedBuilder;
    }

    private double round(double value, int decimalPoints) {
        double d = Math.pow(10, decimalPoints);
        return Math.round(value * d) / d;
    }
}
