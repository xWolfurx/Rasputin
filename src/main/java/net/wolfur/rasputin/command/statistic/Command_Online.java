package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.character.DestinyCharacter;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.core.Command;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Online implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if (bungieUser.isRegistered()) {
            if(args.length == 0) {
                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser).build()).complete();
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Online").build()).queue(message -> {
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

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser) {
        StringBuilder sb = new StringBuilder();

        for(BungieUser bungieUser : Main.getCoreManager().getBungieUserManager().getBungieUsers().values()) {
            if(bungieUser.isRegistered()) {

                bungieUser.requestProfile(ComponentType.CHARACTER_ACTIVITIES);
                JsonObject characterActivities = bungieUser.getProfile(ComponentType.CHARACTER_ACTIVITIES);
                DestinyCharacter lastPlayedDestinyCharacter = null;
                for (DestinyCharacter destinyCharacter : bungieUser.getDestinyCharacters()) {
                    if(lastPlayedDestinyCharacter == null) lastPlayedDestinyCharacter = destinyCharacter;
                    if(destinyCharacter.getDateLastPlayed() > lastPlayedDestinyCharacter.getDateLastPlayed())
                        lastPlayedDestinyCharacter = destinyCharacter;
                }

                JsonObject jsonObject = characterActivities.getAsJsonObject("Response").getAsJsonObject("characterActivities").getAsJsonObject("data").getAsJsonObject(String.valueOf(lastPlayedDestinyCharacter.getCharacterId()));

                long currentActivityHash = jsonObject.get("currentActivityHash").getAsLong();
                long currentActivityModeHash = jsonObject.get("currentActivityModeHash").getAsLong();

                if(currentActivityHash == 0) {
                    continue;
                }

                JsonObject destinyActivityDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_DEFINITION);
                JsonObject destinyActivityModeDefinition = targetUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_MODE_DEFINITION);

                if(currentActivityHash == 82913930L) {
                    sb.append(" » ").append("**" + bungieUser.getUser().getName() + "** is in **Orbit**.").append("\n");
                    continue;
                }

                if(currentActivityHash == 3737830648L) {
                    sb.append(" » ").append("**" + bungieUser.getUser().getName() + "** is at **Tower**.").append("\n");
                    continue;
                }

                String activityModeName = destinyActivityModeDefinition.getAsJsonObject(String.valueOf(currentActivityModeHash)).getAsJsonObject("displayProperties").get("name").getAsString();
                String activityName = destinyActivityDefinition.getAsJsonObject(String.valueOf(currentActivityHash)).getAsJsonObject("displayProperties").get("name").getAsString();

                sb.append(" » ").append("**" + bungieUser.getUser().getName() + "** is playing **" + activityModeName + "** on **" + activityName + "**.").append("\n");
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Activities > Online Clan Members:")
                .setDescription(sb.toString());

        return embedBuilder;
    }

}
