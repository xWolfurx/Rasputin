package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.character.DestinyCharacter;
import net.wolfur.rasputin.bungie.character.type.ClassType;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Command_Fireteam implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if (bungieUser.isRegistered()) {
            if(args.length == 0) {
                JsonObject fireteam = bungieUser.getProfile(ComponentType.TRANSITORY);

                if(fireteam.getAsJsonObject("Response").getAsJsonObject("profileTransitoryData").getAsJsonObject("data") == null) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("**" + event.getAuthor().getName() + "** is not currently in a Fireteam.").setColor(Color.RED).build()).complete();
                    return;
                }

                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, fireteam).build()).complete();
            } else if(args.length == 1) {
                User targetUser = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                if (targetUser != null) {
                    BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                    if(targetBungieUser.isRegistered()) {
                        JsonObject fireteam = targetBungieUser.getProfile(ComponentType.TRANSITORY);

                        if(fireteam.getAsJsonObject("Response").getAsJsonObject("profileTransitoryData").getAsJsonObject("data") == null) {
                            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("**" + targetUser.getName() + "** is not currently in a Fireteam.").setColor(Color.RED).build()).complete();
                            return;
                        }

                        event.getTextChannel().sendMessage(this.createEmbedBuilder(targetBungieUser, fireteam).build()).complete();
                    } else {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Dieser Spieler ist noch nicht registriert.").build()).queue(message -> {
                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                        });
                    }
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Der Spieler existiert nicht.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Fireteam [@Player]").build()).queue(message -> {
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

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, JsonObject fireteam) {
        JsonArray jsonArray = fireteam.getAsJsonObject("Response").getAsJsonObject("profileTransitoryData").getAsJsonObject("data").getAsJsonArray("partyMembers");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Fireteam > Current Fireteam: " + targetUser.getUser().getName())
                .setDescription("**" + targetUser.getUser().getName() + "** is in a Fireteam with:")
                .setFooter("Fireteam" + " | " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), Main.getJDA().getSelfUser().getAvatarUrl());

        for(int i = 0; i < jsonArray.size(); i++) {
            JsonObject partyMember = jsonArray.get(i).getAsJsonObject();

            long membershipId = partyMember.get("membershipId").getAsLong();
            String displayName = partyMember.get("displayName").getAsString();

            ClassType classType = null;
            int lightLevel = 0;

            if(targetUser.getBungieMembershipId() == membershipId) continue;

            for(BungieUser bungieUser : Main.getCoreManager().getBungieUserManager().getBungieUsers().values()) {
                if(bungieUser.getDestinyMembershipId() == membershipId) {
                    DestinyCharacter lastPlayedDestinyCharacter = null;
                    for(DestinyCharacter destinyCharacter : bungieUser.getDestinyCharacters()) {
                        if(lastPlayedDestinyCharacter == null) lastPlayedDestinyCharacter = destinyCharacter;
                        if(destinyCharacter.getDateLastPlayed() > lastPlayedDestinyCharacter.getDateLastPlayed()) lastPlayedDestinyCharacter = destinyCharacter;
                    }

                    lightLevel = lastPlayedDestinyCharacter.getLightLevel();
                    classType = ClassType.getClassById(lastPlayedDestinyCharacter.getClassType());
                }
            }

            embedBuilder.addField(classType == null ? displayName : Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(classType.getBetterName().toLowerCase())).getAsMention() + " " + displayName, lightLevel == 0 ? "" : Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative()).getAsMention() + " " + this.formatInteger(lightLevel), true);
        }

        return embedBuilder;
    }

    private String formatInteger(int value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("###,###.###", symbols);

        return decimalFormat.format(value);
    }

}
