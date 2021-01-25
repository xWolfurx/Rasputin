package net.wolfur.rasputin.command.punish;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.punish.BanInformation;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Ban implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (event.getTextChannel() != null) {
            if(Main.getFileManager().getChannelFile().isCommandChannel(event.getTextChannel().getIdLong())) {
                if (Utils.hasRole(event.getMember(), Utils.getRoleByName("Admin")) || Utils.hasRole(event.getMember(), Utils.getRoleByName("Administrator")) || Utils.hasRole(event.getMember(), Utils.getRoleByName("IT Techniker"))) {
                    if (args.length > 0) {
                        User target = User.fromId(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", ""));
                        if (target != null) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 1; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            String reason = (sb.length() == 0 ? "--" : sb.substring(0, sb.length() - 1));
                            Main.getCoreManager().getBanManager().isPlayerBannedAsync(target.getId(), new Callback<Boolean>() {
                                @Override
                                public void accept(Boolean banned) {
                                    if (banned.booleanValue()) {
                                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Dieser User ist bereits gesperrt.").build()).queue(message -> {
                                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                                        });
                                    } else {
                                        Main.getCoreManager().getBanManager().setBannedAsync(target.getId(), true, new BanInformation(target.getId(), System.currentTimeMillis(), event.getAuthor().getId(), reason), new Callback<Boolean>() {
                                            @Override
                                            public void accept(Boolean success) {
                                                if (success.booleanValue()) {
                                                    TextChannel banHistory = Main.getJDA().getTextChannelById(Main.getFileManager().getChannelFile().getChannelId("banHistory"));
                                                    if(banHistory != null) {
                                                        banHistory.sendMessage(new EmbedBuilder().setColor(Color.CYAN).setDescription("Der User '" + target.getAsMention() + "' wurde gesperrt." + "\n" + "Grund: " + reason).build()).queue();
                                                    } else {
                                                        Logger.error("Can´t find channel 'ban_history'.", true);
                                                    }
                                                } else {
                                                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User konnte nicht gesperrt werden.").build()).queue(message -> {
                                                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                                                    });
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
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Ban <Spieler> [Grund]").build()).queue(message -> {
                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                        });
                    }
                }
            }
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }
}