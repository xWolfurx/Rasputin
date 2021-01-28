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
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.core.Command;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Command_Loadout implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if (bungieUser.isRegistered()) {
            if(args.length == 0) {
                bungieUser.requestCharacter(ComponentType.CHARACTER_EQUIPMENT);
                Map<DestinyCharacter, JsonObject> characterInventories = bungieUser.getCharacterData(ComponentType.CHARACTER_EQUIPMENT);
                event.getTextChannel().sendMessage(this.createLoadoutEmbedBuilder(bungieUser, characterInventories).build()).complete();
            } else if(args.length == 1) {
                User targetUser = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                if (targetUser != null) {
                    BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                    if (targetBungieUser.isRegistered()) {
                        targetBungieUser.requestCharacter(ComponentType.CHARACTER_EQUIPMENT);
                        Map<DestinyCharacter, JsonObject> characterInventories = targetBungieUser.getCharacterData(ComponentType.CHARACTER_EQUIPMENT);
                        event.getTextChannel().sendMessage(this.createLoadoutEmbedBuilder(targetBungieUser, characterInventories).build()).complete();
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
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Loadout [@Player]").build()).queue(message -> {
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

    private EmbedBuilder createLoadoutEmbedBuilder(BungieUser targetUser, Map<DestinyCharacter, JsonObject> characterInventories) {
        DestinyCharacter lastDestinyCharacter = null;

        for(DestinyCharacter destinyCharacter : targetUser.getDestinyCharacters()) {
            if(lastDestinyCharacter == null) lastDestinyCharacter = destinyCharacter;
            if(lastDestinyCharacter.getDateLastPlayed() < destinyCharacter.getDateLastPlayed()) lastDestinyCharacter = destinyCharacter;
        }

        StringBuilder weaponsBuilder = new StringBuilder();
        StringBuilder armorBuilder = new StringBuilder();
        String ghost = "";
        StringBuilder transportationBuilder = new StringBuilder();

        JsonObject lastCharacterInventory = characterInventories.get(lastDestinyCharacter);
        JsonArray itemsArray = lastCharacterInventory.getAsJsonObject("Response").getAsJsonObject("equipment").getAsJsonObject("data").getAsJsonArray("items");

        for(int i = 0; i < itemsArray.size(); i++) {
            long itemHash = itemsArray.get(i).getAsJsonObject().get("itemHash").getAsLong();
            long bucketHash = itemsArray.get(i).getAsJsonObject().get("bucketHash").getAsLong();
            String description = this.getItemType(targetUser, itemHash).replaceAll("Vehicle","Sparrow") + ": " + this.getItemName(targetUser, itemHash);

            if(bucketHash == 284967655L || bucketHash == 2025709351L) {
                transportationBuilder.append(description).append("\n");
            }
            if(bucketHash == 4023194814L) {
                ghost = this.getItemName(targetUser, itemHash);
            }
            if(bucketHash == 3448274439L || bucketHash == 3551918588L || bucketHash == 14239492L || bucketHash == 20886954L || bucketHash == 1585787867L) {
                armorBuilder.append(description).append("\n");
            }
            if(bucketHash == 1498876634L || bucketHash == 2465295065L || bucketHash == 953998645L) {
                String itemType = bucketHash == 1498876634L ? "Kinetic: " : bucketHash == 2465295065L ? "Energy: " : bucketHash == 953998645L ? "Power: " : "Unknown: ";
                weaponsBuilder.append(itemType + this.getItemName(targetUser, itemHash)).append("\n");
            }

        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Loadout Summary > " + targetUser.getUser().getName() + "´s " + ClassType.getClassById(lastDestinyCharacter.getClassType()).getBetterName())
                .addField("Weapons", weaponsBuilder.toString(), false)
                .addField("Armor", armorBuilder.toString(), false)
                .addField("Ghost", ghost, true)
                .addField("Transportation", transportationBuilder.toString(), true)
                .setThumbnail("https://www.bungie.net" + lastDestinyCharacter.getEmblemPath());

        return embedBuilder;
    }

    public String getItemName(BungieUser bungieUser, long itemHash) {
        return bungieUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION).getAsJsonObject(String.valueOf(itemHash)).getAsJsonObject("displayProperties").getAsJsonPrimitive("name").getAsString();
    }

    public String getItemType(BungieUser bungieUser, long itemHash) {
        return bungieUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION).getAsJsonObject(String.valueOf(itemHash)).get("itemTypeDisplayName").getAsString();
    }

}
