package net.wolfur.rasputin.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.punish.BanInformation;
import net.wolfur.rasputin.other.RaidType;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Command_Raid implements Command {

    /**.raid GARDEN_OF_SALVATION 15.01.2020 20:00**/

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (event.getTextChannel() != null) {
            if (event.getTextChannel().getIdLong() == Main.getFileManager().getChannelFile().getChannelId("raid")) {
                event.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                if (args.length >= 3) {
                    RaidType raidType = Main.getCoreManager().getRaidManager().getRaidType(args[0]);
                    if (raidType != null) {
                        Date date;
                        Date time;
                        try {
                            date = new SimpleDateFormat("dd.MM.yyyy").parse(args[1]);
                        } catch (ParseException e) {
                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte geben Sie ein gültiges Datum an.\" + \"\\n\" + \"**Format:** [dd.MM.yyyy]").build()).queue(message -> {
                                message.delete().queueAfter(15, TimeUnit.SECONDS);
                            });
                            return;
                        }
                        try {
                            time = new SimpleDateFormat("HH:mm").parse(args[2]);
                        } catch (ParseException e) {
                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte geben Sie eine gültige Zeit an.\" + \"\\n\" + \"**Format:** [HH:mm]").build()).queue(message -> {
                                message.delete().queueAfter(15, TimeUnit.SECONDS);
                            });
                            return;
                        }
                        if (date != null && time != null) {
                            try {
                                if (new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(args[1] + " " + args[2]).before(new Date())) {
                                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Die angegebene Zeit liegt in der Vergangenheit.").build()).queue(message -> {
                                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                                    });
                                    return;
                                }

                                Date startDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(args[1] + " " + args[2]);
                                long diff = startDate.getTime() - new Date().getTime();

                                if(diff < 0L) {
                                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Es trat ein Fehler beim Berechnen der Zeitdifferenz auf.").build()).queue(message -> {
                                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                                    });
                                    return;
                                }

                                if(diff < TimeUnit.MINUTES.toMillis(30)) {
                                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der Raid muss mindestens 30 Minuten im Voraus erstellt werden.").build()).queue(message -> {
                                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                                    });
                                    return;
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            StringBuilder sb = new StringBuilder();
                            for (int i = 3; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            Main.getCoreManager().getBanManager().getBanInformationAsync(event.getAuthor().getId(), new Callback<BanInformation>() {
                                @Override
                                public void accept(BanInformation banInformation) {
                                    if (banInformation != null) {

                                        if (banInformation.isPermanent()) {
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
                                                Main.getCoreManager().getBanManager().setBanned(event.getAuthor().getId(), false, null);
                                                Main.getCoreManager().getRaidManager().createRaid(event.getChannel(), raidType, date, time, event.getAuthor(), (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "<Keine Bescheibung>"));
                                            }
                                        }
                                    } else {
                                        Main.getCoreManager().getRaidManager().createRaid(event.getChannel(), raidType, date, time, event.getAuthor(), (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "<Keine Bescheibung>"));
                                    }
                                }
                            });
                        } else {
                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Es trat ein Fehler beim Erstellen des Raids auf.").build()).queue(message -> {
                                message.delete().queueAfter(15, TimeUnit.SECONDS);
                            });
                            Logger.error("An error occurred while creating raid. (Date or time is null)", true);
                        }
                    } else {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der angegebene Raid konnte nicht gefunden werden.").build()).queue(message -> {
                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                        });
                    }
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("**Verwendung:** .raid <Raidtyp> <Datum> <Uhrzeit> [Beschreibung]").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            }
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

}
