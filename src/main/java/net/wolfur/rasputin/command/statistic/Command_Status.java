package net.wolfur.rasputin.command.statistic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.TimeUtil;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Status implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            if(args.length == 0) {
                event.getTextChannel().sendMessage(this.createEmbedBuilder().build()).complete();
            } else if(args.length == 1) {
                //TODO: Implement user status
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Status [@Player]").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte registriere dich, um diesen Befehl nutzen zu können." + "\n\n" + "Registriere dich mit **.Register**.").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    private EmbedBuilder createEmbedBuilder() {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Status > Rasputin")
                .setDescription(Main.getReloadTask().isReloading() ? "My functions are restricted, Guardian." : "I am fully operational, Guardian")
                .setThumbnail("http://vhost106.dein-gameserver.tech/rasputin-icon.png")
                .addField("Current version protocol", "Rasputin v" + Main.getVersion(), true)
                .addField("Runtime", TimeUtil.timeToString((System.currentTimeMillis() - Main.getStartTime()), false), true)
                .addField("Registered users", String.valueOf(Main.getCoreManager().getBungieUserManager().getBungieUsers().values().stream().filter(bungieUser -> bungieUser.isRegistered()).count()), true);

        return embedBuilder;
    }

}
