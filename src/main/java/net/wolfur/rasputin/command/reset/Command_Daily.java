package net.wolfur.rasputin.command.reset;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_Daily implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            bungieUser.requestMilestones();

            JsonObject milestonesObject = bungieUser.getMilestonesObject();
            JsonObject activityModifierDefinition = bungieUser.getManifest(DestinyDefinitionType.DESTINY_ACTIVITY_MODIFIER_DEFINITION);

            event.getTextChannel().sendMessage(this.createEmbedBuilder(milestonesObject, activityModifierDefinition).build()).complete();
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte registriere dich, um diesen Befehl nutzen zu können." + "\n\n" + "Registriere dich mit **.Register**.").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

    private EmbedBuilder createEmbedBuilder(JsonObject milestonesObject, JsonObject activityModifierDefinition) {
        JsonArray activitiesArray = milestonesObject.getAsJsonObject("Response").getAsJsonObject("1437935813").getAsJsonArray("activities");
        List<String> modifiers = new ArrayList<>();

        for(int i = 0; i < activitiesArray.size(); i++) {
            JsonObject activity = activitiesArray.get(i).getAsJsonObject();
            JsonArray modifierArray = activity.getAsJsonArray("modifierHashes");
            for(int j = 0; j < modifierArray.size(); j++) {
                modifiers.add(modifierArray.get(j).getAsString());
            }
        }

        StringBuilder modifierStringBuilder = new StringBuilder();
        for(String modifierHash : modifiers) {
            modifierStringBuilder.append(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getModifier(modifierHash)).getAsMention());
            modifierStringBuilder.append(" ").append(activityModifierDefinition.getAsJsonObject(modifierHash).getAsJsonObject("displayProperties").get("name").getAsString());
            modifierStringBuilder.append("\n");
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Daily Rotations")
                .addField("Strikes", modifierStringBuilder.toString(), true)
                .addField("Altar Reward", "Not implemented", true);

        return embedBuilder;
    }

}
