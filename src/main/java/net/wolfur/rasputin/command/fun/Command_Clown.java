package net.wolfur.rasputin.command.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.core.Command;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Clown implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(args.length >= 0) {
            BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
            if (bungieUser.isRegistered()) {
                event.getTextChannel().sendMessage(event.getAuthor().getAsMention() + " spielt mit Clownsnase.").complete();
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte registriere dich, um diesen Befehl nutzen zu können." + "\n\n" + "Registriere dich mit **.Register**.").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .clown").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

}
