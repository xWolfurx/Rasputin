package net.wolfur.rasputin.listeners;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.wolfur.rasputin.util.Logger;

public class Event_GuildMemberRemoveEvent extends ListenerAdapter {

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Logger.info(event.getUser().getName() + " left the server.", true);
    }
}
