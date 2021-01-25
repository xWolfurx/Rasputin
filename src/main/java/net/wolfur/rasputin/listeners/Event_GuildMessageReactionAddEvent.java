package net.wolfur.rasputin.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.punish.BanInformation;
import net.wolfur.rasputin.other.Raid;
import net.wolfur.rasputin.util.TimeUtil;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Event_GuildMessageReactionAddEvent extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if(!event.getMember().getUser().isBot()) {
            Raid raid = Main.getCoreManager().getRaidManager().getRaid(event.getMessageId());
            if(raid != null) {
                event.getReaction().removeReaction(event.getUser()).queue();
                if(event.getReactionEmote().getEmote().getName().equals(Main.getFileManager().getEmoteDefinitionFile().getAddRunner())) {
                    Main.getCoreManager().getBanManager().getBanInformationAsync(event.getUser().getId(), new Callback<BanInformation>() {
                        @Override
                        public void accept(BanInformation banInformation) {
                            if(banInformation != null) {

                                if(banInformation.isPermanent()) {
                                    event.getMember().getUser().openPrivateChannel().queue(channel -> {
                                        channel.sendMessage(new EmbedBuilder().setDescription("Du kannst keinen Raid erstellen, da du gesperrt bist." + "\n" + "Grund: " + banInformation.getReason()).setColor(Color.RED).build()).queue();
                                    });
                                    return;
                                } else {
                                    long diff = banInformation.getTimestamp() + banInformation.getTimeBanInformation().getBanTime() - System.currentTimeMillis();
                                    if (diff > 0L) {
                                        event.getMember().getUser().openPrivateChannel().queue(channel -> {
                                            channel.sendMessage(new EmbedBuilder().setDescription("Du kannst keinen Raid erstellen, da du gesperrt bist." + "\n" + "Grund: " + banInformation.getReason() + "\n" + "Dauer der Sperre: " + TimeUtil.timeToString(diff, false)).setColor(Color.RED).build()).queue();
                                        });
                                        return;
                                    } else {
                                        Main.getCoreManager().getBanManager().setBanned(event.getUser().getId(), false, null);
                                        raid.addRunner(event.getUser());
                                        raid.checkStatus();
                                    }
                                }
                            } else {
                                raid.addRunner(event.getUser());
                                raid.checkStatus();
                            }
                        }
                    });
                }
                if(event.getReactionEmote().getEmote().getName().equals(Main.getFileManager().getEmoteDefinitionFile().getRemoveRunner())) {
                    if(raid.isRunner(event.getUser())) {
                        raid.removeRunner(event.getUser());
                    }
                    if(raid.isAlternative(event.getUser())) {
                        raid.removeAlternative(event.getUser());
                    }
                }
                if(event.getReactionEmote().getName().equals(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative())) {
                    Main.getCoreManager().getBanManager().getBanInformationAsync(event.getUser().getId(), new Callback<BanInformation>() {
                        @Override
                        public void accept(BanInformation banInformation) {
                            if(banInformation != null) {

                                if(banInformation.isPermanent()) {
                                    event.getMember().getUser().openPrivateChannel().queue(channel -> {
                                        channel.sendMessage(new EmbedBuilder().setDescription("Du kannst keinen Raid erstellen, da du gesperrt bist." + "\n" + "Grund: " + banInformation.getReason()).setColor(Color.RED).build()).queue();
                                    });
                                    return;
                                } else {
                                    long diff = banInformation.getTimestamp() + banInformation.getTimeBanInformation().getBanTime() - System.currentTimeMillis();
                                    if (diff > 0L) {
                                        event.getMember().getUser().openPrivateChannel().queue(channel -> {
                                            channel.sendMessage(new EmbedBuilder().setDescription("Du kannst keinen Raid erstellen, da du gesperrt bist." + "\n" + "Grund: " + banInformation.getReason() + "\n" + "Dauer der Sperre: " + TimeUtil.timeToString(diff, false)).setColor(Color.RED).build()).queue();
                                        });
                                        return;
                                    } else {
                                        Main.getCoreManager().getBanManager().setBanned(event.getUser().getId(), false, null);
                                        raid.addAlternative(event.getUser());
                                        raid.checkStatus();
                                    }
                                }
                            } else {
                                raid.addAlternative(event.getUser());
                                raid.checkStatus();
                            }
                        }
                    });
                }
                if(event.getReactionEmote().getEmote().getName().equals(Main.getFileManager().getEmoteDefinitionFile().getSetLeader())) {
                    if(raid.getLeader() == null) {
                        Main.getCoreManager().getBanManager().getBanInformationAsync(event.getUser().getId(), new Callback<BanInformation>() {
                            @Override
                            public void accept(BanInformation banInformation) {
                                if(banInformation != null) {

                                    if(banInformation.isPermanent()) {
                                        event.getMember().getUser().openPrivateChannel().queue(channel -> {
                                            channel.sendMessage(new EmbedBuilder().setDescription("Du kannst keinen Raid erstellen, da du gesperrt bist." + "\n" + "Grund: " + banInformation.getReason()).setColor(Color.RED).build()).queue();
                                        });
                                        return;
                                    } else {
                                        long diff = banInformation.getTimestamp() + banInformation.getTimeBanInformation().getBanTime() - System.currentTimeMillis();
                                        if (diff > 0L) {
                                            event.getMember().getUser().openPrivateChannel().queue(channel -> {
                                                channel.sendMessage(new EmbedBuilder().setDescription("Du kannst keinen Raid erstellen, da du gesperrt bist." + "\n" + "Grund: " + banInformation.getReason() + "\n" + "Dauer der Sperre: " + TimeUtil.timeToString(diff, false)).setColor(Color.RED).build()).queue();
                                            });
                                            return;
                                        } else {
                                            Main.getCoreManager().getBanManager().setBanned(event.getUser().getId(), false, null);
                                            raid.setLeader(event.getUser());
                                        }
                                    }
                                } else {
                                    raid.setLeader(event.getUser());
                                }
                            }
                        });
                    } else {
                        if(raid.getLeader().getId().equals(event.getUser().getId())) {
                            raid.setLeader(null);
                        }
                    }
                }
                if(event.getReactionEmote().getName().equalsIgnoreCase(Main.getFileManager().getEmoteDefinitionFile().getChangeTime())) {
                    if(raid.getLeader() != null && raid.getLeader().equals(event.getUser())) {
                        if (Main.getCoreManager().getRaidManager().getChangeTime().containsKey(event.getMember().getUser())) {
                            Main.getCoreManager().getRaidManager().getChangeTime().remove(event.getMember().getUser());
                            event.getMember().getUser().openPrivateChannel().queue(channel -> {
                                channel.sendMessage(new EmbedBuilder().setDescription("Die Bearbeitung der Uhrzeit wurde abgebrochen.").setColor(Color.RED).build()).queue();
                            });
                        } else {
                            Main.getCoreManager().getRaidManager().getChangeTime().put(event.getMember().getUser(), raid);
                            event.getMember().getUser().openPrivateChannel().queue(channel -> {
                                channel.sendMessage(new EmbedBuilder().setDescription("Die Bearbeitung der Uhrzeit wurde aktiviert." + "\n" + "\n" + "Bitte geben Sie eine neue Uhrzeit an:" + "\n" + "Format: **HH:mm**").setColor(Color.GREEN).build()).queue();
                            });
                        }
                    } else {
                        event.getMember().getUser().openPrivateChannel().queue(channel -> {
                            channel.sendMessage(new EmbedBuilder().setDescription("Die Uhrzeit kann nur vom Leader bearbeitet werden.").setColor(Color.RED).build()).queue();
                        });
                    }
                }
                if(event.getReactionEmote().getEmote().getName().equals(Main.getFileManager().getEmoteDefinitionFile().getDeleteActivity())) {
                    if(raid.getLeader().getId().equalsIgnoreCase(event.getUser().getId())) {
                        List<User> users = new ArrayList<>();
                        users.addAll(raid.getRunners());
                        users.add(raid.getLeader());
                        for(User user : users) {
                            user.openPrivateChannel().queue(channel -> {
                                channel.sendMessage(raid.getCanceledEmbed("Aktivität wurde gelöscht").build()).queue(null, Utils.ignore);
                            });
                        }

                        Main.getCoreManager().getRaidManager().deleteRaid(raid.getId(), "Manually by " + event.getUser().getName());
                    }
                }
            }
        }
    }

}
