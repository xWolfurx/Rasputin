package net.wolfur.rasputin.command.administration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_SendMessage implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(args.length == 0) {
            Role memberRole = Main.getRoleManager().getRole("member");

            for(Member member : Main.getGuild().getMembers()) {
                BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(member.getUser());
                if(!bungieUser.isRegistered() && bungieUser.hasRole(memberRole)) {
                    EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.GRAY)
                            .setDescription("Willkommen Hüter, mein Name ist Rasputin und ich bin die" +
                                    "künstliche Intelligenz dieses Clans." + "\n\n" +
                                    "Meine Aufgabe besteht darin, den Clan mit Informationen rund ums" +
                                    "Destiny 2 Universum zu versorgen." + "\n\n" +
                                    "Um dir auch Zugriff auf diese Funktionen zu gewähren, brauche ich allerdings" +
                                    "zunächst deine Zustimmung. Bitte registriere dich mit meiner Datenbank, damit ich" +
                                    "dich mit meinem kompletten Wissen unterstützen kann." + "\n\n" +
                                    "Um dich zu registrieren, gehe bitte in den Channel '**" + Main.getJDA().getGuildById(Main.getFileManager().getConfigFile().getGuildId()).getTextChannelById(Main.getFileManager().getChannelFile().getChannel("talk").getChannelId()).getAsMention() + "**' und" + "\n" +
                                    "führe dort den Befehl '**.Register**' aus. Folge danach meinen weiteren Anweisungen.")
                            .setThumbnail("http://vhost106.dein-gameserver.tech/rasputin-icon.png")
                            .setTitle("Willkommen bei 'The Evil Organisation'");


                    bungieUser.getUser().openPrivateChannel().queue(channel -> channel.sendMessage(embedBuilder.build()).queue(null, Utils.ignore));
                }
            }

            event.getTextChannel().sendMessage("Complete.").complete();

        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .SendMessage").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

}
