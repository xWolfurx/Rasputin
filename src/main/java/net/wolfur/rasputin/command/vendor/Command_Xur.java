package net.wolfur.rasputin.command.vendor;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.character.type.ClassType;
import net.wolfur.rasputin.bungie.information.XurInformation;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.bungie.type.CurrenciesType;
import net.wolfur.rasputin.bungie.type.DestinyDefinitionType;
import net.wolfur.rasputin.bungie.type.VendorType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.TimeUnit;

public class Command_Xur implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            if(args.length == 0) {
                bungieUser.requestVendor(VendorType.XUR, ComponentType.VENDOR_SALES);
                bungieUser.requestProfile(ComponentType.PROFILE_CURRENCIES);

                JsonObject xurObject = bungieUser.getVendor(VendorType.XUR, ComponentType.VENDOR_SALES);

                if(xurObject == null) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Derzeit können keine Daten über Xûr gesammelt werden.").build()).complete();
                    return;
                }

                XurInformation xurInformation = new XurInformation(xurObject);
                if(!xurInformation.isActive()) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Derzeit können keine Daten über Xûr gesammelt werden.").build()).complete();
                    return;
                }

                String xurLocation = bungieUser.requestXurLocation();
                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, xurInformation, xurLocation == null ? "Unbekannt" : xurLocation).build()).complete();
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Xur").build()).queue(message -> {
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

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, XurInformation xurInformation, String location) {
        StringBuilder description = new StringBuilder();

        String weapon = "[" + xurInformation.getItemName(xurInformation.getWeaponObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)), targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)) + "](https://destinytracker.com/destiny-2/db/items/" + xurInformation.getItemHash(xurInformation.getWeaponObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + ")" + "\n" + "**Cost: **" + xurInformation.getCostItemQuantity(xurInformation.getWeaponObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + " " + Main.getEmoteManager().getEmote(xurInformation.getCostItemHash(xurInformation.getWeaponObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)))).getAsMention();
        String hunterArmor = "[" + xurInformation.getItemName(xurInformation.getHunterArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)), targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)) + "](https://destinytracker.com/destiny-2/db/items/" + xurInformation.getItemHash(xurInformation.getHunterArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + ")" + "\n" + "**Cost: **" + xurInformation.getCostItemQuantity(xurInformation.getHunterArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + " " +  Main.getEmoteManager().getEmote(xurInformation.getCostItemHash(xurInformation.getHunterArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)))).getAsMention();
        String titanArmor = "[" + xurInformation.getItemName(xurInformation.getTitanArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)), targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)) + "](https://destinytracker.com/destiny-2/db/items/" + xurInformation.getItemHash(xurInformation.getTitanArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + ")" + "\n" + "**Cost: **" + xurInformation.getCostItemQuantity(xurInformation.getTitanArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + " "  + Main.getEmoteManager().getEmote(xurInformation.getCostItemHash(xurInformation.getTitanArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)))).getAsMention();
        String warlockArmor = "[" + xurInformation.getItemName(xurInformation.getWarlockArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)), targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)) + "](https://destinytracker.com/destiny-2/db/items/" + xurInformation.getItemHash(xurInformation.getWarlockArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + ")" + "\n" + "**Cost: **" + xurInformation.getCostItemQuantity(xurInformation.getWarlockArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + " "  + Main.getEmoteManager().getEmote(xurInformation.getCostItemHash(xurInformation.getWarlockArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)))).getAsMention();

        int legendaryShards = targetUser.getCurrenciesQuantity(CurrenciesType.LEGENDARY_SHARDS);

        description.append("Xûr handelt mit seltsamen Kuriositäten und seine Motive sind undurchsichtig. Er beugt sich seinen fernen Meistern, den Neun.");
        description.append("\n");
        description.append("\n");
        description.append("**Location:** " + location);
        description.append("\n");
        description.append("**Legendary Shards:** " + this.formatInteger(legendaryShards) + " " + Main.getEmoteManager().getEmote(CurrenciesType.LEGENDARY_SHARDS.getItemHash()).getAsMention());
        description.append("\n");
        description.append("\n");
        description.append("**Offers:**");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Vendors > Xûr, Agent of the Nine")
                .setDescription(description.toString())
                .addField(Main.getEmoteManager().getEmote(ClassType.HUNTER.getClassHash()).getAsMention() + " Hunter Armor", hunterArmor, true)
                .addField(Main.getEmoteManager().getEmote(ClassType.TITAN.getClassHash()).getAsMention() + " Titan Armor", titanArmor, true)
                .addField(Main.getEmoteManager().getEmote(ClassType.WARLOCK.getClassHash()).getAsMention() + " Warlock Armor", warlockArmor, true)
                .addField("Weapon", weapon, true)
                .setThumbnail("https://vhost106.dein-gameserver.tech/xur.png");

        return embedBuilder;
    }

    private String formatInteger(int value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("###,###.###", symbols);

        return decimalFormat.format(value);
    }

}
