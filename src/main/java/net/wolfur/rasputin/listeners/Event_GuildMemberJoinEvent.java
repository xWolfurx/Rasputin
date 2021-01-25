package net.wolfur.rasputin.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.Utils;
import java.awt.*;

public class Event_GuildMemberJoinEvent extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if(event.getMember().getUser().isBot()) return;

        User user = event.getMember().getUser();
        Logger.info(user.getName() + " joined the discord.", true);

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.GRAY)
                                        .setDescription("Willkommen Hüter, mein Name ist Rasputin und ich bin die" + "\n" +
                                                        "künstliche Intelligenz dieses Clans." + "\n\n" +
                                                        "Meine Aufgabe besteht darin, den Clan mit Informationen rund ums" + "\n" +
                                                        "Destiny 2 Universum zu versorgen." + "\n\n" +
                                                        "Um dir auch Zugriff auf diese Funktionen zu gewähren, brauche ich allerdings" + "\n" +
                                                        "zunächst deine Zustimmung. Bitte registriere dich mit meiner Datenbank, damit ich" + "\n" +
                                                        "dich mit meinem kompletten Wissen unterstützen kann." + "\n\n" +
                                                        "Um dich zu registrieren, gehe bitte in den Channel '**\uD83D\uDCACtalk**' und" + "\n" +
                                                        "führe dort den Befehl '**.Register**' aus. Folge danach meinen weiteren Anweisungen.")
                                        .setThumbnail("http://vhost106.dein-gameserver.tech/rasputin-icon.png")
                                        .setTitle("Willkommen bei 'The Evil Organisation'");

        user.openPrivateChannel().queue(channel -> channel.sendMessage(embedBuilder.build()).queue(null, Utils.ignore));
    }
}
