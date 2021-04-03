package net.wolfur.rasputin.command.vendor;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.character.DestinyCharacter;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.information.GunsmithInformation;
import net.wolfur.rasputin.bungie.information.SpiderInformation;
import net.wolfur.rasputin.bungie.type.*;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.TimeUtil;
import net.wolfur.rasputin.util.Utils;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_Gunsmith implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            if(args.length == 0) {
                bungieUser.requestVendor(VendorType.BANSHEE_44, ComponentType.VENDOR_SALES);
                bungieUser.requestProfile(ComponentType.PROFILE_CURRENCIES);
                bungieUser.requestProfile(ComponentType.PROFILE_INVENTORIES);

                JsonObject gunsmithObject = bungieUser.getVendor(VendorType.BANSHEE_44, ComponentType.VENDOR_SALES);

                if(gunsmithObject == null) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Derzeit können keine Daten über Banshee-44 gesammelt werden.").build()).complete();
                    return;
                }

                GunsmithInformation gunsmithInformation = new GunsmithInformation(gunsmithObject);
                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, gunsmithInformation).build()).complete();

            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Gunsmith").build()).queue(message -> {
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


    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, GunsmithInformation gunsmithInformation) {
        StringBuilder description = new StringBuilder();
        List<Long> listedItems = new ArrayList<>();

        description.append("Banshee-44 hat viele Leben hinter sich. Als Meisterwaffenschmied des Turms versorgt er die Hüter mit bester Ware.");
        description.append("\n");
        description.append("\n");
        description.append("**Offers:**");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Vendors > Banshee-44, The Tower Gunsmith")
                .setDescription(description.toString())
                .setThumbnail("https://vhost106.dein-gameserver.tech/banshee.png");

        for(JsonObject sale : gunsmithInformation.getSales()) {
            long itemHash = gunsmithInformation.getItemHash(sale);
            if((itemHash == 2979281381L || itemHash == 4257549984L || itemHash == 4257549985L)) {
                String itemName = gunsmithInformation.getItemName(sale, targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)).replaceAll("Purchase ", "");
                StringBuilder fieldDescription = new StringBuilder();

                for(GunsmithInformation.CostItem costItem : gunsmithInformation.getCostItems(sale)) {
                    MaterialType costMaterialType = MaterialType.getFromItemHash(costItem.getItemHash());
                    CurrenciesType costCurrenciesType = CurrenciesType.getFromItemHash(costItem.getItemHash());
                    int ownCostItemQuantity = costMaterialType == null ? costCurrenciesType == null ? -1 : targetUser.getCurrenciesQuantity(costCurrenciesType) : targetUser.getMaterialQuantity(costMaterialType);

                    if(costItem.getItemHash() == 3159615086L) continue;

                    fieldDescription.append(this.formatInteger(costItem.getQuantity()) + " ");
                    fieldDescription.append(Main.getEmoteManager().getEmote(costItem.getItemHash()).getAsMention());
                    fieldDescription.append(" (" + this.formatInteger(ownCostItemQuantity) + " owned)");
                    fieldDescription.append("\n");
                }
                embedBuilder.addField(Main.getEmoteManager().getEmote(itemHash).getAsMention() + " " + itemName, fieldDescription.toString(), true);
            }

        }

        StringBuilder fieldDescription = new StringBuilder();
        for(JsonObject sale : gunsmithInformation.getSales()) {
            long itemHash = gunsmithInformation.getItemHash(sale);

            if(gunsmithInformation.getItemType(sale, targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)).equalsIgnoreCase("19") && !listedItems.contains(itemHash)) {
                String itemName = gunsmithInformation.getItemName(sale, targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)).replaceAll("Purchase ", "");
                String itemTypeDisplayName = gunsmithInformation.getItemTypeDisplayName(sale, targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION));

                fieldDescription.append(itemName);
                fieldDescription.append(", ");
                fieldDescription.append(itemTypeDisplayName);
                fieldDescription.append("\n");
            }

            listedItems.add(itemHash);
        }
        embedBuilder.addField("Mods", fieldDescription.toString(), false);

        return embedBuilder;
    }

    private String formatInteger(int value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("###,###.###", symbols);

        return decimalFormat.format(value);
    }

}
