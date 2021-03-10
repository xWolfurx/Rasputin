package net.wolfur.rasputin.command.punish;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Unban implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (args.length == 1) {

            long id = -1;
            try {
                id = Long.parseLong(args[0]);
                if(id <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte gebe eine gültige Id ein.").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
                return;
            }

            User target = Main.getJDA().retrieveUserById(id).complete();
            if (target != null) {
                Main.getCoreManager().getBanManager().isPlayerBannedAsync(target.getId(), new Callback<Boolean>() {
                        @Override
                        public void accept(Boolean banned) {
                            if (!banned.booleanValue()) {
                                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User ist nicht gesperrt.").build()).queue(message -> {
                                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                                });
                            } else {
                                Main.getCoreManager().getBanManager().setBannedAsync(target.getId(), false, Main.getCoreManager().getBanManager().getBanInformation(target.getId()), new Callback<Boolean>() {
                                    @Override
                                    public void accept(Boolean success) {
                                        if (success.booleanValue()) {
                                            TextChannel banHistory = Main.getJDA().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("ban_history").getChannelId());
                                            if(banHistory != null) {
                                                banHistory.sendMessage(new EmbedBuilder().setColor(Color.CYAN).setDescription("Der User '" + target.getAsMention() + "' wurde entsperrt.").build()).queue();
                                            } else {
                                                Logger.error("Can´t find channel 'ban_history'.", true);
                                            }
                                        } else {
                                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User '" + target.getAsMention() + "' konnte nicht entsperrt werden.").build()).queue();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User existiert nicht.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Unban <Id>").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

}
