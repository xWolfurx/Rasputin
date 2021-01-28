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
            User target = User.fromId(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", ""));
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
                                            TextChannel banHistory = Main.getJDA().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("banHistory").getChannelId());
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
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Unban <Spieler>").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

}
