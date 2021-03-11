package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.character.DestinyCharacter;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.core.Command;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Command_Current implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if (bungieUser.isRegistered()) {
            if (args.length == 0) {
                bungieUser.requestProfile(ComponentType.CHARACTER_ACTIVITIES);
                JsonObject characterActivities = bungieUser.getProfile(ComponentType.CHARACTER_ACTIVITIES);
                DestinyCharacter lastPlayedDestinyCharacter = null;
                for(DestinyCharacter destinyCharacter : bungieUser.getDestinyCharacters()) {
                    if(lastPlayedDestinyCharacter == null) lastPlayedDestinyCharacter = destinyCharacter;
                    if(destinyCharacter.getDateLastPlayed() > lastPlayedDestinyCharacter.getDateLastPlayed()) lastPlayedDestinyCharacter = destinyCharacter;
                }

                JsonObject jsonObject = characterActivities.getAsJsonObject("Response").getAsJsonObject("characterActivities").getAsJsonObject("data").getAsJsonObject(String.valueOf(lastPlayedDestinyCharacter.getCharacterId()));

                long currentActivityHash = jsonObject.get("currentActivityHash").getAsLong();
                long currentActivityModeHash = jsonObject.get("currentActivityModeHash").getAsLong();

                if(currentActivityHash == 0) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("**" + event.getAuthor().getName() + "** is not currently playing.").setColor(Color.RED).build()).complete();
                    return;
                }

                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, currentActivityHash, currentActivityModeHash).build()).complete();
            } else if(args.length == 1) {
                User targetUser = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                if (targetUser != null) {
                    BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                    if(targetBungieUser.isRegistered()) {
                        targetBungieUser.requestProfile(ComponentType.CHARACTER_ACTIVITIES);
                        JsonObject characterActivities = targetBungieUser.getProfile(ComponentType.CHARACTER_ACTIVITIES);
                        DestinyCharacter lastPlayedDestinyCharacter = null;
                        for(DestinyCharacter destinyCharacter : targetBungieUser.getDestinyCharacters()) {
                            if(lastPlayedDestinyCharacter == null) lastPlayedDestinyCharacter = destinyCharacter;
                            if(destinyCharacter.getDateLastPlayed() > lastPlayedDestinyCharacter.getDateLastPlayed()) lastPlayedDestinyCharacter = destinyCharacter;
                        }

                        JsonObject jsonObject = characterActivities.getAsJsonObject("Response").getAsJsonObject("characterActivities").getAsJsonObject("data").getAsJsonObject(String.valueOf(lastPlayedDestinyCharacter.getCharacterId()));

                        long currentActivityHash = jsonObject.get("currentActivityHash").getAsLong();
                        long currentActivityModeHash = jsonObject.get("currentActivityModeHash").getAsLong();

                        if(currentActivityHash == 0) {
                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("**" + targetUser.getName() + "** is not currently playing.").setColor(Color.RED).build()).complete();
                            return;
                        }

                        event.getTextChannel().sendMessage(this.createEmbedBuilder(targetBungieUser, currentActivityHash, currentActivityModeHash).build()).complete();
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
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Current [@Player]").build()).queue(message -> {
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

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, long currentActivityHash, long currentActivityModeHash) {
        JsonObject destinyActivityDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_DEFINITION);
        JsonObject destinyActivityModeDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_MODE_DEFINITION);

        if(currentActivityHash == 82913930L) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(Color.MAGENTA)
                    .setTitle("Activities > Current Activity: " + targetUser.getUser().getName())
                    .setDescription("**" + targetUser.getUser().getName() + "** is in **Orbit**.")
                    .setFooter( "In Orbit | " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), Main.getJDA().getSelfUser().getAvatarUrl());

            return embedBuilder;
        }

        String activityModeName = destinyActivityModeDefinition.getAsJsonObject(String.valueOf(currentActivityModeHash)).getAsJsonObject("displayProperties").get("name").getAsString();
        String activityName = destinyActivityDefinition.getAsJsonObject(String.valueOf(currentActivityHash)).getAsJsonObject("displayProperties").get("name").getAsString();
        String iconUrl = destinyActivityDefinition.getAsJsonObject(String.valueOf(currentActivityHash)).get("pgcrImage").getAsString();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Activities > Current Activity: " + targetUser.getUser().getName())
                .setDescription("**" + targetUser.getUser().getName() + "** is playing **" + activityModeName + "** on **" + activityName + "**.")
                .setThumbnail("https://www.bungie.net" + iconUrl)
                .setFooter(activityModeName + " | " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), Main.getJDA().getSelfUser().getAvatarUrl());

        return embedBuilder;
    }

}
