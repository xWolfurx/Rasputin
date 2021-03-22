package net.wolfur.rasputin.command.weapon;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.information.XurInformation;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.bungie.type.VendorType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.weapon.DestinyWeaponData;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_GodRoll implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            if(args.length >= 1) {
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String name = sb.substring(0, sb.length() - 1).toLowerCase();
                DestinyWeaponData destinyWeaponData = Main.getWeaponManager().getDestinyWeaponData(name);

                if(destinyWeaponData == null) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Diese Waffe befindet sich derzeit nicht in der Datenbank." + "\n\n" + "Bitte melde dich bei Wolfur, um die Waffe in die Datenbank aufzunehmen.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                    return;
                }

                event.getTextChannel().sendMessage(this.buildEmbedBuilder(bungieUser, destinyWeaponData).build()).complete();
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .GodRoll <Name>").build()).queue(message -> {
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

    private EmbedBuilder buildEmbedBuilder(BungieUser targetBungieUser, DestinyWeaponData destinyWeaponData) {
        JsonObject inventoryItemDefinition = targetBungieUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_LITE_DEFINITION);
        JsonObject inventoryBucketDefinition = targetBungieUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_BUCKET_DEFINITION);

        StringBuilder godRollPerks = new StringBuilder();
        StringBuilder alternativePerks = new StringBuilder();

        long itemHash = destinyWeaponData.getItemHash();
        JsonObject itemDefinition = inventoryItemDefinition.getAsJsonObject(String.valueOf(itemHash));

        String name = itemDefinition.getAsJsonObject("displayProperties").get("name").getAsString();
        String iconUrl = itemDefinition.getAsJsonObject("displayProperties").get("icon").getAsString();

        String itemTypeDisplayName = itemDefinition.get("itemTypeDisplayName").getAsString();
        long equipmentSlotTypeHash = itemDefinition.getAsJsonObject("equippingBlock").get("equipmentSlotTypeHash").getAsLong();
        String equipmentSlotTypeName = inventoryBucketDefinition.getAsJsonObject(String.valueOf(equipmentSlotTypeHash)).getAsJsonObject("displayProperties").get("name").getAsString();

        for(long godRollPerkHash : destinyWeaponData.getGodRoll()) {
            String perkName = inventoryItemDefinition.getAsJsonObject(String.valueOf(godRollPerkHash)).getAsJsonObject("displayProperties").get("name").getAsString();
            String perkTypeDisplayName = inventoryItemDefinition.getAsJsonObject(String.valueOf(godRollPerkHash)).get("itemTypeDisplayName").getAsString();
            String perkIconUrl = inventoryItemDefinition.getAsJsonObject(String.valueOf(godRollPerkHash)).getAsJsonObject("displayProperties").get("icon").getAsString();

            godRollPerks.append(" » " + perkTypeDisplayName.replaceAll("Trait", "Perk") + ": " + perkName).append("\n");
        }

        for(long alternativePerkHash : destinyWeaponData.getAlternativeRoll()) {
            String perkName = inventoryItemDefinition.getAsJsonObject(String.valueOf(alternativePerkHash)).getAsJsonObject("displayProperties").get("name").getAsString();
            String perkTypeDisplayName = inventoryItemDefinition.getAsJsonObject(String.valueOf(alternativePerkHash)).get("itemTypeDisplayName").getAsString();
            String perkIconUrl = inventoryItemDefinition.getAsJsonObject(String.valueOf(alternativePerkHash)).getAsJsonObject("displayProperties").get("icon").getAsString();

            alternativePerks.append(" » " + perkTypeDisplayName.replaceAll("Trait", "Perk") + ": " + perkName).append("\n");
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle(name, "https://destinytracker.com/destiny-2/db/items/" + itemHash)
                .setThumbnail("https://bungie.net" + iconUrl)
                .setDescription(itemTypeDisplayName + ", " + equipmentSlotTypeName)
                .addField("God-Roll:", godRollPerks.toString(), true)
                .addBlankField(true)
                .addField("Alternative-Roll:", alternativePerks.toString(), true);

        return embedBuilder;
    }
}
