package net.wolfur.rasputin.command.notify;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
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
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            if (args.length == 1) {
                if(args[0].equalsIgnoreCase("vendor")) {
                    Role vendorNotifyRole = Main.getRoleManager().getRole("vendor_notify");

                    if(vendorNotifyRole == null) {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Die Rolle 'Vendor Notify' konnte in der Datenbank nicht gefunden werden." + "\n\n" + "Bitte melde diesen Fehler bei Wolfur.").build()).complete();
                        return;
                    }

                    if(bungieUser.hasRole(vendorNotifyRole)) {
                        bungieUser.removeRole(vendorNotifyRole);
                        event.getTextChannel().sendMessage("Benachrichtigungen der Verkäufer für " + event.getAuthor().getAsMention() + " wurden deaktiviert.").complete();
                    } else {
                        bungieUser.addRole(vendorNotifyRole);
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

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }
}
