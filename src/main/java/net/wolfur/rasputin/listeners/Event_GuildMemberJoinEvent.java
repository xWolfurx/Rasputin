package net.wolfur.rasputin.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.wolfur.rasputin.util.Logger;

public class Event_GuildMemberJoinEvent extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        Logger.info(member.getUser().getAsMention() + " joined the server.", true);
    }

}
