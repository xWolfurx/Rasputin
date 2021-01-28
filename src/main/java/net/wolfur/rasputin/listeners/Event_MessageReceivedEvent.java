package net.wolfur.rasputin.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.CommandHandler;
import net.wolfur.rasputin.other.Raid;
import net.wolfur.rasputin.util.Variables;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Event_MessageReceivedEvent extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().startsWith(Variables.PREFIX) && event.getMessage().getAuthor().getId() != event.getJDA().getSelfUser().getId()) {
            if(Main.getFileManager().getChannelFile().getChannel(event.getTextChannel().getIdLong()) != null && Main.getFileManager().getChannelFile().getChannel(event.getTextChannel().getIdLong()).isCommandChannel())
                CommandHandler.handleCommand(CommandHandler.parser.parser(event.getMessage().getContentRaw(), event));
                return;
        }
        if(event.isFromType(ChannelType.PRIVATE)) {
            if(Main.getCoreManager().getRaidManager().getChangeTime().containsKey(event.getAuthor())) {
                Raid raid = Main.getCoreManager().getRaidManager().getChangeTime().get(event.getAuthor());
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                try {
                    Date time = sdf.parse(event.getMessage().getContentDisplay());
                    if (new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(new SimpleDateFormat("dd.MM.yyyy").format(raid.getDate()) + " " + sdf.format(time)).before(new Date())) {
                        event.getAuthor().openPrivateChannel().queue(channel -> {
                            channel.sendMessage(new EmbedBuilder().setDescription("Die angegbene Zeit liegt in der Vergangenheit.").setColor(Color.RED).build()).queue();
                        });
                        return;
                    }
                    raid.setNewTime(time);
                    Main.getCoreManager().getRaidManager().getChangeTime().remove(event.getAuthor());
                    event.getAuthor().openPrivateChannel().queue(channel -> {
                        channel.sendMessage(new EmbedBuilder().setDescription("Die Zeit wurde erfolgreich geändert.").setColor(Color.GREEN).build()).queue();
                    });
                } catch (ParseException e) {
                    event.getAuthor().openPrivateChannel().queue(channel -> {
                        channel.sendMessage(new EmbedBuilder().setDescription("Bitte geben Sie ein gültiges Format an: **HH:mm**").setColor(Color.RED).build()).queue();
                    });
                }
            }
        }
    }

}
