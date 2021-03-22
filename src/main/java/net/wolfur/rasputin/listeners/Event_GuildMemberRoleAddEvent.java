package net.wolfur.rasputin.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.other.RoleType;
import net.wolfur.rasputin.util.Utils;

import java.awt.Color;
import java.util.List;

public class Event_GuildMemberRoleAddEvent extends ListenerAdapter {

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if(event.getMember().getUser().isBot()) return;

        User user = event.getMember().getUser();
        List<Role> addedRoles = event.getRoles();

        if(!addedRoles.contains(Main.getGuild().getRoleById(Main.getFileManager().getRoleDefinitionFile().getRoleId(RoleType.MEMBER)))) {
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.GRAY)
                .setDescription("Willkommen Hüter, mein Name ist Rasputin und ich bin die" + "\n" +
                        "künstliche Intelligenz dieses Clans." + "\n\n" +
                        "Meine Aufgabe besteht darin, den Clan mit Informationen rund ums" + "\n" +
                        "Destiny 2 Universum zu versorgen." + "\n\n" +
                        "Um dir auch Zugriff auf diese Funktionen zu gewähren, brauche ich allerdings" + "\n" +
                        "zunächst deine Zustimmung. Bitte registriere dich mit meiner Datenbank, damit ich" + "\n" +
                        "dich mit meinem kompletten Wissen unterstützen kann." + "\n\n" +
                        "Um dich zu registrieren, gehe bitte in den Channel '**" + Main.getJDA().getGuildById(Main.getFileManager().getConfigFile().getGuildId()).getTextChannelById(Main.getFileManager().getChannelFile().getChannel("talk").getChannelId()).getAsMention() + "**' und" + "\n" +
                        "führe dort den Befehl '**.Register**' aus. Folge danach meinen weiteren Anweisungen.")
                .setThumbnail("http://vhost106.dein-gameserver.tech/rasputin-icon.png")
                .setTitle("Willkommen bei 'The Evil Organisation'");


        user.openPrivateChannel().queue(channel -> channel.sendMessage(embedBuilder.build()).queue(null, Utils.ignore));
    }

}
