package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.character.type.ClassType;
import net.wolfur.rasputin.bungie.type.DestinyActivityModeType;
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_Last implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            if(args.length == 1) {
                if (args[0].equalsIgnoreCase("raid")) {
                    bungieUser.requestDestinyActivityHistory(DestinyActivityModeType.RAID, 250, true);
                    List<JsonObject> raidActivities = bungieUser.getActivityHistory(DestinyActivityModeType.RAID);
                    event.getTextChannel().sendMessage(this.createRaidEmbedBuilder(bungieUser, raidActivities).build()).complete();
                } else if (args[0].equalsIgnoreCase("patrol")) {
                    bungieUser.requestDestinyActivityHistory(DestinyActivityModeType.PATROL, 250, true);
                    List<JsonObject> patrolActivities = bungieUser.getActivityHistory(DestinyActivityModeType.PATROL);
                    event.getTextChannel().sendMessage(this.createPatrolEmbedBuilder(bungieUser, patrolActivities).build()).complete();
                } else if (args[0].equalsIgnoreCase("nightfall") || args[0].equalsIgnoreCase("nf")) {
                    bungieUser.requestDestinyActivityHistory(DestinyActivityModeType.SCORED_NIGHTFALL, 250, true);
                    List<JsonObject> nightfallActivities = bungieUser.getActivityHistory(DestinyActivityModeType.SCORED_NIGHTFALL);
                    event.getTextChannel().sendMessage(this.createNightfallEmbedBuilder(bungieUser, nightfallActivities).build()).complete();
                } else if (args[0].equalsIgnoreCase("dungeon")) {
                    bungieUser.requestDestinyActivityHistory(DestinyActivityModeType.DUNGEON, 250, true);
                    List<JsonObject> dungeonActivities = bungieUser.getActivityHistory(DestinyActivityModeType.DUNGEON);
                    event.getTextChannel().sendMessage(this.createDungeonEmbedBuilder(bungieUser, dungeonActivities).build()).complete();
                } else if (args[0].equalsIgnoreCase("gambit")) {
                    bungieUser.requestDestinyActivityHistory(DestinyActivityModeType.GAMBIT, 250, true);
                    bungieUser.requestDestinyActivityHistory(DestinyActivityModeType.GAMBIT_PRIME, 250, true);
                    List<JsonObject> gambitActivities = bungieUser.getActivityHistory(DestinyActivityModeType.GAMBIT);
                    gambitActivities.addAll(bungieUser.getActivityHistory(DestinyActivityModeType.GAMBIT_PRIME));
                    event.getTextChannel().sendMessage(this.createGambitEmbedBuilder(bungieUser, gambitActivities).build()).complete();
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Diese Aktivität existiert nicht. \n\nBitte verwende **.help last** um dir die möglichen Aktivitäten aufzulisten.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else if(args.length == 2) {
                User targetUser = Main.getJDA().retrieveUserById(args[1].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                if (targetUser != null) {
                    BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                    if (targetBungieUser.isRegistered()) {
                        if (args[0].equalsIgnoreCase("raid")) {
                            targetBungieUser.requestDestinyActivityHistory(DestinyActivityModeType.RAID, 250, true);
                            List<JsonObject> raidActivities = targetBungieUser.getActivityHistory(DestinyActivityModeType.RAID);
                            event.getTextChannel().sendMessage(this.createRaidEmbedBuilder(targetBungieUser, raidActivities).build()).complete();
                        } else if (args[0].equalsIgnoreCase("patrol")) {
                            targetBungieUser.requestDestinyActivityHistory(DestinyActivityModeType.PATROL, 250, true);
                            List<JsonObject> patrolActivities = targetBungieUser.getActivityHistory(DestinyActivityModeType.PATROL);
                            event.getTextChannel().sendMessage(this.createPatrolEmbedBuilder(targetBungieUser, patrolActivities).build()).complete();
                        } else if (args[0].equalsIgnoreCase("nightfall") || args[0].equalsIgnoreCase("nf")) {
                            targetBungieUser.requestDestinyActivityHistory(DestinyActivityModeType.SCORED_NIGHTFALL, 250, true);
                            List<JsonObject> nightfallActivities = targetBungieUser.getActivityHistory(DestinyActivityModeType.SCORED_NIGHTFALL);
                            event.getTextChannel().sendMessage(this.createNightfallEmbedBuilder(targetBungieUser, nightfallActivities).build()).complete();
                        } else if (args[0].equalsIgnoreCase("dungeon")) {
                            targetBungieUser.requestDestinyActivityHistory(DestinyActivityModeType.DUNGEON, 250, true);
                            List<JsonObject> dungeonActivities = targetBungieUser.getActivityHistory(DestinyActivityModeType.DUNGEON);
                            event.getTextChannel().sendMessage(this.createDungeonEmbedBuilder(targetBungieUser, dungeonActivities).build()).complete();
                        } else if (args[0].equalsIgnoreCase("gambit")) {
                            targetBungieUser.requestDestinyActivityHistory(DestinyActivityModeType.GAMBIT, 250, true);
                            targetBungieUser.requestDestinyActivityHistory(DestinyActivityModeType.GAMBIT_PRIME, 250, true);
                            List<JsonObject> gambitActivities = targetBungieUser.getActivityHistory(DestinyActivityModeType.GAMBIT);
                            gambitActivities.addAll(targetBungieUser.getActivityHistory(DestinyActivityModeType.GAMBIT_PRIME));
                            event.getTextChannel().sendMessage(this.createGambitEmbedBuilder(targetBungieUser, gambitActivities).build()).complete();
                        } else {
                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Diese Aktivität existiert nicht. \n\nBitte verwende **.help last** um dir die möglichen Aktivitäten aufzulisten.").build()).queue(message -> {
                                message.delete().queueAfter(15, TimeUnit.SECONDS);
                            });
                        }
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
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Last <Activity> [@Player]").build()).queue(message -> {
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

    private EmbedBuilder createRaidEmbedBuilder(BungieUser targetUser, List<JsonObject> raidActivities) {
        JsonObject lastRaid = null;
        long lastRaidTime = 0L;

        for(JsonObject raid : raidActivities) {
            String period = raid.get("period").getAsString();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = sdf.parse(period);
                long time = date.getTime();

                if(lastRaid == null) {
                    lastRaid = raid;
                    lastRaidTime = time;
                } else {
                    if(time > lastRaidTime) {
                        lastRaid = raid;
                        lastRaidTime = time;
                    }
                }
            } catch (ParseException e) {
                Logger.error("An error occurred while parsing activity period.", true);
                return new EmbedBuilder().setColor(Color.RED).setDescription("An error occurred while parsing activity period.");
            }
        }

        if(lastRaid == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity found.");
        }

        long instanceId = lastRaid.getAsJsonObject("activityDetails").get("instanceId").getAsLong();
        long duration = lastRaid.getAsJsonObject("values").getAsJsonObject("timePlayedSeconds").getAsJsonObject("basic").get("value").getAsLong();

        JsonObject postGameCarnageReport = targetUser.requestPostGameCarnageReport(instanceId);
        if(postGameCarnageReport == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity data received.");
        }

        long activityHash = postGameCarnageReport.getAsJsonObject("Response").getAsJsonObject("activityDetails").get("directorActivityHash").getAsLong();
        JsonObject destinyActivityDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_DEFINITION);

        String activityName = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).getAsJsonObject("displayProperties").get("name").getAsString();
        String iconUrl = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).get("pgcrImage").getAsString();

        JsonArray entries = postGameCarnageReport.getAsJsonObject("Response").getAsJsonArray("entries");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Activities > Last Raid: " + targetUser.getUser().getName())
                .setDescription("**" + activityName + " - " + TimeUtil.timeToString(duration * 1000, true) + "**")
                .setThumbnail("https://www.bungie.net" + iconUrl)
                .setFooter("Raid on " + activityName + " | " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(lastRaidTime)), Main.getJDA().getSelfUser().getAvatarUrl());

        for(int i = 0; i < entries.size(); i++) {
            JsonObject entry = entries.get(i).getAsJsonObject();
            StringBuilder fieldDescription = new StringBuilder();

            String playerName = entry.getAsJsonObject("player").getAsJsonObject("destinyUserInfo").get("displayName").getAsString();
            int lightLevel = entry.getAsJsonObject("player").get("lightLevel").getAsInt();
            long classHash = entry.getAsJsonObject("player").get("classHash").getAsLong();

            int kills = entry.getAsJsonObject("values").getAsJsonObject("kills").getAsJsonObject("basic").get("value").getAsInt();
            int deaths = entry.getAsJsonObject("values").getAsJsonObject("deaths").getAsJsonObject("basic").get("value").getAsInt();
            int assists = entry.getAsJsonObject("values").getAsJsonObject("assists").getAsJsonObject("basic").get("value").getAsInt();
            double killsDeathsRatio = entry.getAsJsonObject("values").getAsJsonObject("killsDeathsRatio").getAsJsonObject("basic").get("value").getAsDouble();

            fieldDescription.append("K: **" + kills + "** D: **" + deaths + "** A: **" + assists + "**");
            fieldDescription.append("\n");
            fieldDescription.append("K/D: **" + this.round(killsDeathsRatio, 2) + "**");

            embedBuilder.addField(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(ClassType.getClassByClassHash(classHash).getBetterName().toLowerCase())).getAsMention() + " " + playerName + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative()).getAsMention() + " " + this.formatInteger(lightLevel), fieldDescription.toString(), true);
        }

        return embedBuilder;
    }

    private EmbedBuilder createPatrolEmbedBuilder(BungieUser targetUser, List<JsonObject> patrolActivities) {
        JsonObject lastPatrol = null;
        long lastPatrolTime = 0L;

        for(JsonObject raid : patrolActivities) {
            String period = raid.get("period").getAsString();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = sdf.parse(period);
                long time = date.getTime();

                if(lastPatrol == null) {
                    lastPatrol = raid;
                    lastPatrolTime = time;
                } else {
                    if(time > lastPatrolTime) {
                        lastPatrol = raid;
                        lastPatrolTime = time;
                    }
                }
            } catch (ParseException e) {
                Logger.error("An error occurred while parsing activity period.", true);
                return new EmbedBuilder().setColor(Color.RED).setDescription("An error occurred while parsing activity period.");
            }
        }

        if(lastPatrol == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity found.");
        }

        long instanceId = lastPatrol.getAsJsonObject("activityDetails").get("instanceId").getAsLong();
        long duration = lastPatrol.getAsJsonObject("values").getAsJsonObject("timePlayedSeconds").getAsJsonObject("basic").get("value").getAsLong();

        JsonObject postGameCarnageReport = targetUser.requestPostGameCarnageReport(instanceId);
        if(postGameCarnageReport == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity data received.");
        }

        long activityHash = postGameCarnageReport.getAsJsonObject("Response").getAsJsonObject("activityDetails").get("directorActivityHash").getAsLong();
        JsonObject destinyActivityDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_DEFINITION);

        String activityName = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).getAsJsonObject("displayProperties").get("name").getAsString();
        String iconUrl = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).get("pgcrImage").getAsString();

        JsonArray entries = postGameCarnageReport.getAsJsonObject("Response").getAsJsonArray("entries");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Activities > Last Patrol: " + targetUser.getUser().getName())
                .setDescription("**" + activityName + " - " + TimeUtil.timeToString(duration * 1000, true) + "**")
                .setThumbnail("https://www.bungie.net" + iconUrl)
                .setFooter("Patrol on " + activityName + " | " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(lastPatrolTime)), Main.getJDA().getSelfUser().getAvatarUrl());

        for(int i = 0; i < entries.size(); i++) {
            JsonObject entry = entries.get(i).getAsJsonObject();
            StringBuilder fieldDescription = new StringBuilder();

            String playerName = entry.getAsJsonObject("player").getAsJsonObject("destinyUserInfo").get("displayName").getAsString();
            int lightLevel = entry.getAsJsonObject("player").get("lightLevel").getAsInt();
            long classHash = entry.getAsJsonObject("player").get("classHash").getAsLong();

            int kills = entry.getAsJsonObject("values").getAsJsonObject("kills").getAsJsonObject("basic").get("value").getAsInt();
            int deaths = entry.getAsJsonObject("values").getAsJsonObject("deaths").getAsJsonObject("basic").get("value").getAsInt();
            int assists = entry.getAsJsonObject("values").getAsJsonObject("assists").getAsJsonObject("basic").get("value").getAsInt();
            double killsDeathsRatio = entry.getAsJsonObject("values").getAsJsonObject("killsDeathsRatio").getAsJsonObject("basic").get("value").getAsDouble();

            fieldDescription.append("K: **" + kills + "** D: **" + deaths + "** A: **" + assists + "**");
            fieldDescription.append("\n");
            fieldDescription.append("K/D: **" + this.round(killsDeathsRatio, 2) + "**");

            embedBuilder.addField(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(ClassType.getClassByClassHash(classHash).getBetterName().toLowerCase())).getAsMention() + " " + playerName + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative()).getAsMention() + " " + this.formatInteger(lightLevel), fieldDescription.toString(), true);
        }

        return embedBuilder;
    }

    private EmbedBuilder createNightfallEmbedBuilder(BungieUser targetUser, List<JsonObject> nightfallActivities) {
        JsonObject lastNightfall = null;
        long lastNightfallTime = 0L;

        for(JsonObject nightfall : nightfallActivities) {
            String period = nightfall.get("period").getAsString();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = sdf.parse(period);
                long time = date.getTime();

                if(lastNightfall == null) {
                    lastNightfall = nightfall;
                    lastNightfallTime = time;
                } else {
                    if(time > lastNightfallTime) {
                        lastNightfall = nightfall;
                        lastNightfallTime = time;
                    }
                }
            } catch (ParseException e) {
                Logger.error("An error occurred while parsing activity period.", true);
                return new EmbedBuilder().setColor(Color.RED).setDescription("An error occurred while parsing activity period.");
            }
        }

        if(lastNightfall == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity found.");
        }

        long instanceId = lastNightfall.getAsJsonObject("activityDetails").get("instanceId").getAsLong();
        long duration = lastNightfall.getAsJsonObject("values").getAsJsonObject("timePlayedSeconds").getAsJsonObject("basic").get("value").getAsLong();
        int score = lastNightfall.getAsJsonObject("values").getAsJsonObject("teamScore").getAsJsonObject("basic").get("value").getAsInt();

        JsonObject postGameCarnageReport = targetUser.requestPostGameCarnageReport(instanceId);
        if(postGameCarnageReport == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity data received.");
        }

        long activityHash = postGameCarnageReport.getAsJsonObject("Response").getAsJsonObject("activityDetails").get("directorActivityHash").getAsLong();
        JsonObject destinyActivityDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_DEFINITION);

        String activityName = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).getAsJsonObject("displayProperties").get("name").getAsString();
        String iconUrl = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).get("pgcrImage").getAsString();

        JsonArray entries = postGameCarnageReport.getAsJsonObject("Response").getAsJsonArray("entries");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Activities > Last Nightfall: " + targetUser.getUser().getName())
                .setDescription("**" + activityName + " - " + TimeUtil.timeToString(duration * 1000, true) + "**" + "\n" + "**Total Score: " + this.formatInteger(score) + "**")
                .setThumbnail("https://www.bungie.net" + iconUrl)
                .setFooter(activityName + " | " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(lastNightfallTime)), Main.getJDA().getSelfUser().getAvatarUrl());

        for(int i = 0; i < entries.size(); i++) {
            JsonObject entry = entries.get(i).getAsJsonObject();
            StringBuilder fieldDescription = new StringBuilder();

            String playerName = entry.getAsJsonObject("player").getAsJsonObject("destinyUserInfo").get("displayName").getAsString();
            int lightLevel = entry.getAsJsonObject("player").get("lightLevel").getAsInt();
            long classHash = entry.getAsJsonObject("player").get("classHash").getAsLong();

            int kills = entry.getAsJsonObject("values").getAsJsonObject("kills").getAsJsonObject("basic").get("value").getAsInt();
            int deaths = entry.getAsJsonObject("values").getAsJsonObject("deaths").getAsJsonObject("basic").get("value").getAsInt();
            int assists = entry.getAsJsonObject("values").getAsJsonObject("assists").getAsJsonObject("basic").get("value").getAsInt();
            double killsDeathsRatio = entry.getAsJsonObject("values").getAsJsonObject("killsDeathsRatio").getAsJsonObject("basic").get("value").getAsDouble();

            fieldDescription.append("K: **" + kills + "** D: **" + deaths + "** A: **" + assists + "**");
            fieldDescription.append("\n");
            fieldDescription.append("K/D: **" + this.round(killsDeathsRatio, 2) + "**");

            embedBuilder.addField(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(ClassType.getClassByClassHash(classHash).getBetterName().toLowerCase())).getAsMention() + " " + playerName + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative()).getAsMention() + " " + this.formatInteger(lightLevel), fieldDescription.toString(), true);
        }

        return embedBuilder;
    }

    private EmbedBuilder createDungeonEmbedBuilder(BungieUser targetUser, List<JsonObject> dungeonActivities) {
        JsonObject lastDungeon = null;
        long lastDungeonTime = 0L;

        for(JsonObject dungeon : dungeonActivities) {
            String period = dungeon.get("period").getAsString();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = sdf.parse(period);
                long time = date.getTime();

                if(lastDungeon == null) {
                    lastDungeon = dungeon;
                    lastDungeonTime = time;
                } else {
                    if(time > lastDungeonTime) {
                        lastDungeon = dungeon;
                        lastDungeonTime = time;
                    }
                }
            } catch (ParseException e) {
                Logger.error("An error occurred while parsing activity period.", true);
                return new EmbedBuilder().setColor(Color.RED).setDescription("An error occurred while parsing activity period.");
            }
        }

        if(lastDungeon == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity found.");
        }

        long instanceId = lastDungeon.getAsJsonObject("activityDetails").get("instanceId").getAsLong();
        long duration = lastDungeon.getAsJsonObject("values").getAsJsonObject("timePlayedSeconds").getAsJsonObject("basic").get("value").getAsLong();

        JsonObject postGameCarnageReport = targetUser.requestPostGameCarnageReport(instanceId);
        if(postGameCarnageReport == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity data received.");
        }

        long activityHash = postGameCarnageReport.getAsJsonObject("Response").getAsJsonObject("activityDetails").get("directorActivityHash").getAsLong();
        JsonObject destinyActivityDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_DEFINITION);

        String activityName = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).getAsJsonObject("displayProperties").get("name").getAsString();
        String iconUrl = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).get("pgcrImage").getAsString();

        JsonArray entries = postGameCarnageReport.getAsJsonObject("Response").getAsJsonArray("entries");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Activities > Last Dungeon: " + targetUser.getUser().getName())
                .setDescription("**" + activityName + " - " + TimeUtil.timeToString(duration * 1000, true) + "**")
                .setThumbnail("https://www.bungie.net" + iconUrl)
                .setFooter("Dungeon on " + activityName + " | " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(lastDungeonTime)), Main.getJDA().getSelfUser().getAvatarUrl());

        for(int i = 0; i < entries.size(); i++) {
            JsonObject entry = entries.get(i).getAsJsonObject();
            StringBuilder fieldDescription = new StringBuilder();

            String playerName = entry.getAsJsonObject("player").getAsJsonObject("destinyUserInfo").get("displayName").getAsString();
            int lightLevel = entry.getAsJsonObject("player").get("lightLevel").getAsInt();
            long classHash = entry.getAsJsonObject("player").get("classHash").getAsLong();

            int kills = entry.getAsJsonObject("values").getAsJsonObject("kills").getAsJsonObject("basic").get("value").getAsInt();
            int deaths = entry.getAsJsonObject("values").getAsJsonObject("deaths").getAsJsonObject("basic").get("value").getAsInt();
            int assists = entry.getAsJsonObject("values").getAsJsonObject("assists").getAsJsonObject("basic").get("value").getAsInt();
            double killsDeathsRatio = entry.getAsJsonObject("values").getAsJsonObject("killsDeathsRatio").getAsJsonObject("basic").get("value").getAsDouble();

            fieldDescription.append("K: **" + kills + "** D: **" + deaths + "** A: **" + assists + "**");
            fieldDescription.append("\n");
            fieldDescription.append("K/D: **" + this.round(killsDeathsRatio, 2) + "**");

            embedBuilder.addField(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(ClassType.getClassByClassHash(classHash).getBetterName().toLowerCase())).getAsMention() + " " + playerName + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative()).getAsMention() + " " + this.formatInteger(lightLevel), fieldDescription.toString(), true);
        }

        return embedBuilder;
    }

    private EmbedBuilder createTrialsEmbedBuilder(BungieUser targetUser, List<JsonObject> dungeonActivities) {
        JsonObject lastTrials = null;
        long lastTrialsTime = 0L;

        for(JsonObject trials : dungeonActivities) {
            String period = trials.get("period").getAsString();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = sdf.parse(period);
                long time = date.getTime();

                if(lastTrials == null) {
                    lastTrials = trials;
                    lastTrialsTime = time;
                } else {
                    if(time > lastTrialsTime) {
                        lastTrials = trials;
                        lastTrialsTime = time;
                    }
                }
            } catch (ParseException e) {
                Logger.error("An error occurred while parsing activity period.", true);
                return new EmbedBuilder().setColor(Color.RED).setDescription("An error occurred while parsing activity period.");
            }
        }

        if(lastTrials == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity found.");
        }

        long instanceId = lastTrials.getAsJsonObject("activityDetails").get("instanceId").getAsLong();
        long duration = lastTrials.getAsJsonObject("values").getAsJsonObject("timePlayedSeconds").getAsJsonObject("basic").get("value").getAsLong();

        JsonObject postGameCarnageReport = targetUser.requestPostGameCarnageReport(instanceId);
        if(postGameCarnageReport == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity data received.");
        }

        long activityHash = postGameCarnageReport.getAsJsonObject("Response").getAsJsonObject("activityDetails").get("directorActivityHash").getAsLong();
        JsonObject destinyActivityDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_DEFINITION);

        String activityName = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).getAsJsonObject("displayProperties").get("name").getAsString();
        String iconUrl = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).get("pgcrImage").getAsString();

        JsonArray entries = postGameCarnageReport.getAsJsonObject("Response").getAsJsonArray("entries");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Activities > Last Trials of Osiris: " + targetUser.getUser().getName())
                .setDescription("**" + activityName + " - " + TimeUtil.timeToString(duration * 1000, true) + "**")
                .setThumbnail("https://www.bungie.net" + iconUrl)
                .setFooter("Trials of Osiris on " + activityName + " | " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(lastTrialsTime)), Main.getJDA().getSelfUser().getAvatarUrl());

        for(int i = 0; i < entries.size(); i++) {
            JsonObject entry = entries.get(i).getAsJsonObject();
            StringBuilder fieldDescription = new StringBuilder();

            String playerName = entry.getAsJsonObject("player").getAsJsonObject("destinyUserInfo").get("displayName").getAsString();
            int lightLevel = entry.getAsJsonObject("player").get("lightLevel").getAsInt();
            long classHash = entry.getAsJsonObject("player").get("classHash").getAsLong();

            int kills = entry.getAsJsonObject("values").getAsJsonObject("kills").getAsJsonObject("basic").get("value").getAsInt();
            int deaths = entry.getAsJsonObject("values").getAsJsonObject("deaths").getAsJsonObject("basic").get("value").getAsInt();
            int assists = entry.getAsJsonObject("values").getAsJsonObject("assists").getAsJsonObject("basic").get("value").getAsInt();
            double killsDeathsRatio = entry.getAsJsonObject("values").getAsJsonObject("killsDeathsRatio").getAsJsonObject("basic").get("value").getAsDouble();

            fieldDescription.append("K: **" + kills + "** D: **" + deaths + "** A: **" + assists + "**");
            fieldDescription.append("\n");
            fieldDescription.append("K/D: **" + this.round(killsDeathsRatio, 2) + "**");

            embedBuilder.addField(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(ClassType.getClassByClassHash(classHash).getBetterName().toLowerCase())).getAsMention() + " " + playerName + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative()).getAsMention() + " " + this.formatInteger(lightLevel), fieldDescription.toString(), true);
        }

        return embedBuilder;
    }

    private EmbedBuilder createGambitEmbedBuilder(BungieUser targetUser, List<JsonObject> gambitActivities) {
        JsonObject lastGambit = null;
        long lastGambitTime = 0L;

        for(JsonObject gambit : gambitActivities) {
            String period = gambit.get("period").getAsString();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = sdf.parse(period);
                long time = date.getTime();

                if(lastGambit == null) {
                    lastGambit = gambit;
                    lastGambitTime = time;
                } else {
                    if(time > lastGambitTime) {
                        lastGambit = gambit;
                        lastGambitTime = time;
                    }
                }
            } catch (ParseException e) {
                Logger.error("An error occurred while parsing activity period.", true);
                return new EmbedBuilder().setColor(Color.RED).setDescription("An error occurred while parsing activity period.");
            }
        }

        if(lastGambit == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity found.");
        }

        long instanceId = lastGambit.getAsJsonObject("activityDetails").get("instanceId").getAsLong();
        long duration = lastGambit.getAsJsonObject("values").getAsJsonObject("timePlayedSeconds").getAsJsonObject("basic").get("value").getAsLong();

        JsonObject postGameCarnageReport = targetUser.requestPostGameCarnageReport(instanceId);
        if(postGameCarnageReport == null) {
            return new EmbedBuilder().setColor(Color.RED).setDescription("Error: No activity data received.");
        }

        long activityHash = postGameCarnageReport.getAsJsonObject("Response").getAsJsonObject("activityDetails").get("directorActivityHash").getAsLong();
        JsonObject destinyActivityDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_DEFINITION);

        String activityName = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).getAsJsonObject("displayProperties").get("name").getAsString();
        String iconUrl = destinyActivityDefinition.getAsJsonObject(String.valueOf(activityHash)).get("pgcrImage").getAsString();

        JsonArray entries = postGameCarnageReport.getAsJsonObject("Response").getAsJsonArray("entries");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Activities > Last Gambit: " + targetUser.getUser().getName())
                .setDescription("**" + activityName + " - " + TimeUtil.timeToString(duration * 1000, true) + "**")
                .setThumbnail("https://www.bungie.net" + iconUrl)
                .setFooter(activityName + " | " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(lastGambitTime)), Main.getJDA().getSelfUser().getAvatarUrl());

        StringBuilder alphaTeam = new StringBuilder();
        StringBuilder bravoTeam = new StringBuilder();

        int teamWon = -1;

        for(int i = 0; i < entries.size(); i++) {
            JsonObject entry = entries.get(i).getAsJsonObject();

            String playerName = entry.getAsJsonObject("player").getAsJsonObject("destinyUserInfo").get("displayName").getAsString();
            long classHash = entry.getAsJsonObject("player").get("classHash").getAsLong();

            int team = entry.getAsJsonObject("values").getAsJsonObject("team").getAsJsonObject("basic").get("value").getAsInt();
            double standing = entry.getAsJsonObject("values").getAsJsonObject("standing").getAsJsonObject("basic").get("value").getAsDouble();

            int banked = entry.getAsJsonObject("extended").getAsJsonObject("values").getAsJsonObject("motesDeposited").getAsJsonObject("basic").get("value").getAsInt();
            int denied = entry.getAsJsonObject("extended").getAsJsonObject("values").getAsJsonObject("motesDenied").getAsJsonObject("basic").get("value").getAsInt();

            int smallBlockers = entry.getAsJsonObject("extended").getAsJsonObject("values").getAsJsonObject("smallBlockersSent").getAsJsonObject("basic").get("value").getAsInt();
            int mediumBlockers = entry.getAsJsonObject("extended").getAsJsonObject("values").getAsJsonObject("mediumBlockersSent").getAsJsonObject("basic").get("value").getAsInt();
            int largeBlockers = entry.getAsJsonObject("extended").getAsJsonObject("values").getAsJsonObject("largeBlockersSent").getAsJsonObject("basic").get("value").getAsInt();

            String healing = entry.getAsJsonObject("extended").getAsJsonObject("values").getAsJsonObject("primevalHealing").getAsJsonObject("basic").get("displayValue").getAsString();
            double damage = entry.getAsJsonObject("extended").getAsJsonObject("values").getAsJsonObject("primevalDamage").getAsJsonObject("basic").get("value").getAsDouble();

            int kills = entry.getAsJsonObject("extended").getAsJsonObject("values").getAsJsonObject("invaderKills").getAsJsonObject("basic").get("value").getAsInt();


            if(teamWon == -1) {
                if(standing == 0.0) {
                    teamWon = team;
                }
            }

            if(team == 17) {
                alphaTeam.append(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(ClassType.getClassByClassHash(classHash).getBetterName().toLowerCase())).getAsMention()).append(" " + playerName);
                alphaTeam.append("\n");
                alphaTeam.append("Banked: **" + banked + "** Blockers: **" + (smallBlockers + mediumBlockers + largeBlockers) + "**");
                alphaTeam.append("\n");
                alphaTeam.append("Denied: **" + denied + "** Invade Kills: **" + kills + "**");
                alphaTeam.append("\n");
                alphaTeam.append("Damage: **" + this.formatDouble(this.round(damage, 2)));
                alphaTeam.append("\n");
                alphaTeam.append("Healing: **" + healing + "**");
                alphaTeam.append("\n");
            } else if(team == 18) {
                bravoTeam.append(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(ClassType.getClassByClassHash(classHash).getBetterName().toLowerCase())).getAsMention()).append(" " + playerName);
                bravoTeam.append("\n");
                bravoTeam.append("Banked: **" + banked + "** Blockers: **" + (smallBlockers + mediumBlockers + largeBlockers) + "**");
                bravoTeam.append("\n");
                bravoTeam.append("Denied: **" + denied + "** Invade Kills: **" + kills + "**");
                bravoTeam.append("\n");
                bravoTeam.append("Damage: **" + this.formatDouble(this.round(damage, 2)));
                bravoTeam.append("\n");
                bravoTeam.append("Healing: **" + healing + "**");
                bravoTeam.append("\n");
            } else {
                Logger.warning("No team found for gambit activity.", true);
            }
        }

        embedBuilder.addField("Alpha: " + (teamWon == 17 ? "1 (Victory)" : "0 (Defeat)"), alphaTeam.toString(), true);
        embedBuilder.addField("Bravo: " + (teamWon == 18 ? "1 (Victory)" : "0 (Defeat)"), alphaTeam.toString(), true);

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

    private String formatDouble(double value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("###,###.###", symbols);

        return decimalFormat.format(value);
    }

}
