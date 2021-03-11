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
import net.wolfur.rasputin.bungie.item.DestinyItem;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Command_Postmaster implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if (bungieUser.isRegistered()) {
            if(args.length == 0) {
                bungieUser.requestCharacter(ComponentType.CHARACTER_INVENTORIES);
                Map<DestinyCharacter, JsonObject> inventories = bungieUser.getCharacterData(ComponentType.CHARACTER_INVENTORIES);

                Map<DestinyCharacter, List<DestinyItem>> postmasterItems = new HashMap<>();

                for(DestinyCharacter destinyCharacter : inventories.keySet()) {
                    JsonArray inventoryData = inventories.get(destinyCharacter).getAsJsonObject("Response").getAsJsonObject("inventory").getAsJsonObject("data").getAsJsonArray("items");
                    List<DestinyItem> destinyItems = new ArrayList<>();

                    for(int i = 0; i < inventoryData.size(); i++) {
                        JsonObject item = inventoryData.get(i).getAsJsonObject();

                        long bucketHash = item.get("bucketHash").getAsLong();
                        if(bucketHash == 215593132) {
                            long itemHash = item.get("itemHash").getAsLong();
                            long itemInstanceId = item.get("itemInstanceId") != null ? item.get("itemInstanceId").getAsLong() : -1L;
                            int quantity = item.get("quantity").getAsInt();

                            destinyItems.add(new DestinyItem(itemHash, itemInstanceId, quantity, bucketHash));
                        }
                    }

                    postmasterItems.put(destinyCharacter, destinyItems);
                }

                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, postmasterItems).build()).complete();
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("collect")) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("This function is not implemented.").build()).complete();
                } else {
                    User targetUser = Main.getJDA().retrieveUserById(args[0].replaceAll("@", "").replaceAll("!", "").replaceAll("<", "").replaceAll(">", "")).complete();
                    if (targetUser != null) {
                        BungieUser targetBungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(targetUser);
                        if(targetBungieUser.isRegistered()) {
                            targetBungieUser.requestCharacter(ComponentType.CHARACTER_INVENTORIES);
                            Map<DestinyCharacter, JsonObject> inventories = targetBungieUser.getCharacterData(ComponentType.CHARACTER_INVENTORIES);

                            Map<DestinyCharacter, List<DestinyItem>> postmasterItems = new HashMap<>();

                            for(DestinyCharacter destinyCharacter : inventories.keySet()) {
                                JsonArray inventoryData = inventories.get(destinyCharacter).getAsJsonObject("Response").getAsJsonObject("inventory").getAsJsonObject("data").getAsJsonArray("items");
                                List<DestinyItem> destinyItems = new ArrayList<>();

                                for(int i = 0; i < inventoryData.size(); i++) {
                                    JsonObject item = inventoryData.get(i).getAsJsonObject();

                                    long bucketHash = item.get("bucketHash").getAsLong();
                                    if(bucketHash == 215593132) {
                                        long itemHash = item.get("itemHash").getAsLong();
                                        long itemInstanceId = item.get("itemInstanceId") != null ? item.get("itemInstanceId").getAsLong() : -1L;
                                        int quantity = item.get("quantity").getAsInt();

                                        destinyItems.add(new DestinyItem(itemHash, itemInstanceId, quantity, bucketHash));
                                    }
                                }

                                postmasterItems.put(destinyCharacter, destinyItems);
                            }

                            event.getTextChannel().sendMessage(this.createEmbedBuilder(targetBungieUser, postmasterItems).build()).complete();
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
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Postmaster [@Player | collect]").build()).queue(message -> {
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

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, Map<DestinyCharacter, List<DestinyItem>> postmasterItems) {

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Kadi 55-30 > " + targetUser.getUser().getName() + "'s Lost Items")
                .setDescription("Droiden der Poststelle kümmern sich pflichtbewusst um Lieferungen, Nachrichten und Fundsachen.")
                .setThumbnail("https://www.bungie.net/common/destiny2_content/icons/58e0868540ff4053d1a1f10f2dd959dd.png");

        int i = 1;
        for(DestinyCharacter destinyCharacter : postmasterItems.keySet()) {
            StringBuilder fieldDescription = new StringBuilder();
            String classType = ClassType.getClassById(destinyCharacter.getClassType()).getBetterName().toLowerCase();
            List<DestinyItem> lostItems = postmasterItems.get(destinyCharacter);

            if(lostItems.size() <= 0) {
                embedBuilder.addField(i + ". " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(classType)).getAsMention() + " " + ClassType.getClassById(destinyCharacter.getClassType()).getBetterName() + ":", " » Keine Items",true);
                break;
            }

            for(DestinyItem lostItem : lostItems) {
                JsonObject itemInstanceData = null;
                if(lostItem.getItemInstanceId() != -1L) {
                    itemInstanceData = targetUser.requestItem(lostItem.getItemInstanceId());
                }

                JsonObject additionalItemData = itemInstanceData != null ? itemInstanceData.getAsJsonObject("Response").getAsJsonObject("instance").getAsJsonObject("data") : null;

                String power = additionalItemData != null ? "\n" + "(Power: " + String.valueOf(additionalItemData.get("itemLevel").getAsInt()) + String.valueOf(additionalItemData.get("quality").getAsInt()) + ")" : "";
                fieldDescription.append(" » " + lostItem.getQuantity() + "x " + lostItem.getItemName(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))).append(" " + power).append("\n");
            }

            embedBuilder.addField(i + ". " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(classType)).getAsMention() + " " + ClassType.getClassById(destinyCharacter.getClassType()).getBetterName() + ":", fieldDescription.toString(),true);
            i++;
        }

        return embedBuilder;
    }
}
