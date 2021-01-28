package net.wolfur.rasputin.command.reset;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.core.Command;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Weekly implements Command {

    //541780856: Deep Stone Crypt
    //3312774044: Crucible Playlist

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

            //vent.getTextChannel().sendMessage(this.createEmbedBuilder(milestonesObject, activityModifierDefinition).build()).complete();
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte registriere dich, um diesen Befehl nutzen zu können." + "\n\n" + "Registriere dich mit **.Register**.").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }
}
