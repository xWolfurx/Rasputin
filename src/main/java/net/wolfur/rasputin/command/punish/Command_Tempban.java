package net.wolfur.rasputin.command.punish;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.other.Raid;
import net.wolfur.rasputin.punish.BanInformation;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Tempban implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (args.length >= 2) {
            long id = -1;
            try {
                id = Long.parseLong(args[2]);
                if(id <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte gebe eine gültige Id ein.").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
                return;
            }
            User target = Main.getJDA().retrieveUserById(id).complete();
            if (target != null) {
                long banTime = TimeUtil.parseTime(args[1]);
                if (banTime != -1L) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
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
                                Main.getCoreManager().getBanManager().setBannedAsync(target.getId(), true, new BanInformation(target.getId(), System.currentTimeMillis(), event.getAuthor().getId(), new BanInformation.TimeBanInformation(banTime), reason), new Callback<Boolean>() {
                                    @Override
                                    public void accept(Boolean success) {
                                        long endTime = System.currentTimeMillis() + banTime;
                                        if (success.booleanValue()) {
                                            for(Raid raid : Main.getCoreManager().getRaidManager().getRaids().values()) {
                                                if(raid.isRunner(target) || raid.isAlternative(target) || raid.isLeader(target)) {
                                                    long startTime = raid.getCompleteDate().getTime();

                                                    if(endTime > startTime) {
                                                        if(raid.isRunner(target)) raid.removeRunner(target);
                                                        if(raid.isAlternative(target)) raid.removeAlternative(target);
                                                        if(raid.isLeader(target)) raid.setLeader(null);

                                                        raid.updateMessage();

                                                        target.openPrivateChannel().queue(channel -> {
                                                            channel.sendMessage(raid.removedFromRaid().build()).queue();
                                                        });
                                                    }
                                                }
                                            }

                                            TextChannel banHistory = Main.getJDA().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("banHistory").getChannelId());
                                            if (banHistory != null) {
                                                banHistory.sendMessage(new EmbedBuilder().setColor(Color.CYAN).setDescription("Der User '" + target.getAsMention() + "' wurde gesperrt." + "\n" + "Grund: " + reason + "\n" + "Dauer: " + TimeUtil.timeToString(banTime, true)).build()).queue();
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
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Falsches Zeitformat.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User existiert nicht.").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Tempban <Id> <Zeit> [Grund]").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }
}
