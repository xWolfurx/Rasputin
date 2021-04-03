package net.wolfur.rasputin.command.weapon;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.weapon.DestinyWeaponData;
import net.wolfur.rasputin.weapon.WeaponGameType;

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
            if(args.length >= 2) {
                WeaponGameType weaponGameType = WeaponGameType.getByName(args[0]);

                if(weaponGameType == null) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Diese Art von 'WeaponsGameType' existiert nicht.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for(int i = 1; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }

                String name = sb.substring(0, sb.length() - 1).toLowerCase();
                DestinyWeaponData destinyWeaponData = Main.getWeaponManager().getDestinyWeaponData(name, weaponGameType);

                if(destinyWeaponData == null) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Diese Waffe befindet sich derzeit nicht in der Datenbank." + "\n\n" + "Bitte melde dich bei Wolfur oder Philip, um die Waffe in die Datenbank aufzunehmen.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                    return;
                }

                event.getTextChannel().sendMessage(this.buildEmbedBuilder(bungieUser, destinyWeaponData).build()).complete();
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .GodRoll <PvE | PvP> <Name>").build()).queue(message -> {
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
        JsonObject statDefinition = targetBungieUser.getManifest(DestinyDefinitionType.DESTINY_STAT_DEFINITION);

        StringBuilder godRollPerks = new StringBuilder();
        StringBuilder alternativePerks = new StringBuilder();

        long itemHash = destinyWeaponData.getItemHash();
        JsonObject itemDefinition = inventoryItemDefinition.getAsJsonObject(String.valueOf(itemHash));

        String name = itemDefinition.getAsJsonObject("displayProperties").get("name").getAsString();
        String iconUrl = itemDefinition.getAsJsonObject("displayProperties").get("icon").getAsString();

        String itemTypeDisplayName = itemDefinition.get("itemTypeDisplayName").getAsString();
        long equipmentSlotTypeHash = itemDefinition.getAsJsonObject("equippingBlock").get("equipmentSlotTypeHash").getAsLong();
        String equipmentSlotTypeName = inventoryBucketDefinition.getAsJsonObject(String.valueOf(equipmentSlotTypeHash)).getAsJsonObject("displayProperties").get("name").getAsString();

        String masterwork = destinyWeaponData.getMasterwork() != -1L ? "Masterwork: " + statDefinition.getAsJsonObject(String.valueOf(destinyWeaponData.getMasterwork())).getAsJsonObject("displayProperties").get("name").getAsString() : "";
        String alternativeMasterwork = destinyWeaponData.getAlternativeMasterwork() != -1L ? "Masterwork: " + statDefinition.getAsJsonObject(String.valueOf(destinyWeaponData.getAlternativeMasterwork())).getAsJsonObject("displayProperties").get("name").getAsString() : "";

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
                .setTitle(name + " » " + destinyWeaponData.getWeaponGameType().getBetterName(), "https://destinytracker.com/destiny-2/db/items/" + itemHash)
                .setThumbnail("https://bungie.net" + iconUrl)
                .setDescription(itemTypeDisplayName + ", " + equipmentSlotTypeName.substring(0, equipmentSlotTypeName.length() - 1))
                .addField("God-Roll:", godRollPerks.toString() + "\n" + masterwork, true)
                .addBlankField(true)
                .addField("Alternative-Roll:", alternativePerks.toString() + "\n" + alternativeMasterwork, true);

        return embedBuilder;
    }
}
