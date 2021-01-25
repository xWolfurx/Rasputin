package net.wolfur.rasputin.command.punish;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.punish.BanInformation;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_ActiveBans implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (event.getTextChannel() != null) {
            if(Main.getFileManager().getChannelFile().isCommandChannel(event.getTextChannel().getIdLong())) {
                if (Utils.hasRole(event.getMember(), Utils.getRoleByName("Admin")) || Utils.hasRole(event.getMember(), Utils.getRoleByName("Administrator")) || Utils.hasRole(event.getMember(), Utils.getRoleByName("IT Techniker"))) {
                    if (args.length == 0) {
                        Main.getCoreManager().getBanManager().getActiveBansAsync(new Callback<List<BanInformation>>() {
                            @Override
                            public void accept(List<BanInformation> activeBans) {
                                if (activeBans.isEmpty()) {
                                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Es existieren keine aktiven Sperren.").build()).queue(message -> {
                                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                                    });
                                } else {
                                    StringBuilder sb = new StringBuilder();
                                    activeBans.forEach(activeBan -> sb.append(" - ").append(User.fromId(activeBan.getUserId()).getAsMention()).append(" [" + (activeBan.isPermanent() ? "Permanent" : "Temporär") + "]").append("\n"));
                                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.CYAN).setDescription("Derzeit gesperrte User: \n" + sb.substring(0, sb.length() - 1)).build()).queue();
                                }
                            }
                        });
                    } else {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .ActiveBans").build()).queue(message -> {
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
