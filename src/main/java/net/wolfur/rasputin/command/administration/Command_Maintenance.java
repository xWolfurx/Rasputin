package net.wolfur.rasputin.command.administration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Maintenance implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("Enable")) {
                if(!Main.isMaintenance()) {
                    Utils.handleMaintenance(true);
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Maintenance is already active.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else if(args[0].equalsIgnoreCase("Disable")) {
                if(Main.isMaintenance()) {
                    Utils.handleMaintenance(false);
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Maintenance is already inactive.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            }
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Maintenance <Enable | Disable>").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }
}
