package net.wolfur.rasputin.command.punish;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.punish.BanInformation;
import net.wolfur.rasputin.util.TimeUtil;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_BanInfo implements Command {

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
                Main.getCoreManager().getBanManager().getBanInformationAsync(target.getId(), new Callback<BanInformation>() {
                    @Override
                    public void accept(BanInformation banInformation) {
                        if (banInformation == null) {
                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User '" + target.getName() + "' ist nicht gesperrt.").build()).queue(message -> {
                                message.delete().queueAfter(15, TimeUnit.SECONDS);
                            });
                        } else {
                            long diff = banInformation.isPermanent() ? 0L : banInformation.getTimestamp() + banInformation.getTimeBanInformation().getBanTime() - System.currentTimeMillis();
                            String description = "Informationen über " + target.getAsMention() + ":" + "\n"
                                    + "  - Dauer: " + (banInformation.isPermanent() ? "Permanent" : TimeUtil.timeToString(diff, false)) + "\n"
                                    + "  - Grund: " + banInformation.getReason() + "\n"
                                    + "  - Gesperrt durch: " + User.fromId(banInformation.getBannedBy()).getAsMention();
                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.CYAN).setDescription(description).build()).queue();
                        }
                    }
                });
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User existiert nicht.").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .BanInfo <Id>").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }
    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

}
