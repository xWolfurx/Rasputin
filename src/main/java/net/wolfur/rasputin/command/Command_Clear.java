package net.wolfur.rasputin.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_Clear implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (event.getTextChannel() != null) {
            if (Utils.hasRole(event.getMember(), Utils.getRoleByName("Admin")) || Utils.hasRole(event.getMember(), Utils.getRoleByName("Administrator")) || Utils.hasRole(event.getMember(), Utils.getRoleByName("IT Techniker"))) {
                try {
                    MessageHistory history = new MessageHistory(event.getTextChannel());
                    List<Message> messages;
                    if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
                        try {
                            while (true) {
                                messages = history.retrievePast(1).complete();
                                messages.get(0).delete().queue();
                            }
                        } catch (Exception e) {
                        }
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.GREEN).setDescription("Successfully deleted all messages.").build()).queue(message -> {
                            message.delete().queueAfter(30, TimeUnit.SECONDS);
                        });
                    } else {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Clear All").build()).queue(message -> {
                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                        });
                    }
                } catch (Exception e) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setDescription("An error occurred while performing the command.").addField("Error Type", e.getLocalizedMessage(), false).addField("Message", e.getMessage(), false).build()).queue();
                }
            }
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

}
