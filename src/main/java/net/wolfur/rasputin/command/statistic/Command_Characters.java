package net.wolfur.rasputin.command.statistic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.character.type.GenderType;
import net.wolfur.rasputin.bungie.character.type.RaceType;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.character.type.ClassType;
import net.wolfur.rasputin.bungie.character.DestinyCharacter;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class Command_Characters implements Command {

    //TODO: Rework command

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(Main.getFileManager().getChannelFile().isCommandChannel(event.getTextChannel().getIdLong())) {
            BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
            if (bungieUser.isRegistered()) {
                if (args.length == 0) {
                    bungieUser.requestDestinyProfile();
                    EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Destiny Characters: " + event.getAuthor().getName()).setColor(Color.MAGENTA);

                    int i = 1;
                    for (DestinyCharacter destinyCharacter : bungieUser.getDestinyCharacters()) {
                        String classType = ClassType.getClassById(destinyCharacter.getClassType()).getBetterName().toLowerCase();
                        embedBuilder.addField(i + ". " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(classType)).getAsMention() + " " + ClassType.getClassById(destinyCharacter.getClassType()).getBetterName() + ": ",
                                "Zuletzt gespielt: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(destinyCharacter.getDateLastPlayed()) + "\n" +
                                        "Lichtlevel: " + destinyCharacter.getLightLevel() + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative()).getAsMention() + "\n" +
                                        "Geschlecht: " + GenderType.getGenderById(destinyCharacter.getGenderType()).getBetterName() + "\n" +
                                        "Rasse: " + RaceType.getRaceById(destinyCharacter.getRaceType()).getBetterName() + "\n", false);
                        i++;
                    }

                    event.getTextChannel().sendMessage(embedBuilder.build()).queue();
                } else if (args.length == 1) {
                    User target = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                    if (target != null) {
                        BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(target);
                        if (targetBungieUser.isRegistered()) {
                            targetBungieUser.requestDestinyProfile();
                            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Destiny Characters: " + target.getName()).setColor(Color.MAGENTA);

                            int i = 1;
                            for (DestinyCharacter destinyCharacter : targetBungieUser.getDestinyCharacters()) {
                                String classType = ClassType.getClassById(destinyCharacter.getClassType()).getBetterName().toLowerCase();
                                embedBuilder.addField(i + ". " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(classType)).getAsMention() + " " + ClassType.getClassById(destinyCharacter.getClassType()).getBetterName() + ": ",
                                        "Zuletzt gespielt: " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(destinyCharacter.getDateLastPlayed()) + "\n" +
                                                "Lichtlevel: " + destinyCharacter.getLightLevel() + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative()).getAsMention() + "\n" +
                                                "Geschlecht: " + GenderType.getGenderById(destinyCharacter.getGenderType()).getBetterName() + "\n" +
                                                "Rasse: " + RaceType.getRaceById(destinyCharacter.getRaceType()).getBetterName() + "\n", false);
                                i++;
                            }

                            event.getTextChannel().sendMessage(embedBuilder.build()).queue();
                        } else {
                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der angegebene User hat sich noch nicht registriert.").build()).queue(message -> {
                                message.delete().queueAfter(15, TimeUnit.SECONDS);
                            });
                        }
                    } else {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der User existiert nicht.").build()).queue(message -> {
                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                        });
                    }
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Characters [@Spieler]").build()).queue(message -> {
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
