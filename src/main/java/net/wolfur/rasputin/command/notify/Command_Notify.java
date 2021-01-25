package net.wolfur.rasputin.command.notify;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.other.RoleType;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Notify implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(Main.getFileManager().getChannelFile().isCommandChannel(event.getTextChannel().getIdLong())) {
            BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
            if(bungieUser.isRegistered()) {
                if (args.length == 1) {
                    if(args[0].equalsIgnoreCase("vendor")) {
                        if(bungieUser.hasRole(event.getMember(), Main.getFileManager().getRoleDefinitionFile().getRoleId(RoleType.VENDOR_NOTIFY))) {
                            bungieUser.removeRole(Main.getFileManager().getRoleDefinitionFile().getRoleId(RoleType.VENDOR_NOTIFY));
                            event.getTextChannel().sendMessage("Benachrichtigungen der Verkäufer für " + event.getAuthor().getAsMention() + " wurden deaktiviert.").complete();
                        } else {
                            bungieUser.addRole(Main.getFileManager().getRoleDefinitionFile().getRoleId(RoleType.VENDOR_NOTIFY));
                            event.getTextChannel().sendMessage("Benachrichtigungen der Verkäufer für " + event.getAuthor().getAsMention() + " wurden aktiviert.").complete();
                        }
                    }
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Notify <Type>").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte registriere dich, um diesen Befehl nutzen zu können." + "\n\n" + "Registriere dich mit **.Register**.").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }
}
