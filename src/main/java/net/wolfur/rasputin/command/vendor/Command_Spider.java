package net.wolfur.rasputin.command.vendor;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.information.SpiderInformation;
import net.wolfur.rasputin.bungie.type.*;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.TimeUnit;

public class Command_Spider implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(Main.getFileManager().getChannelFile().isCommandChannel(event.getTextChannel().getIdLong())) {
            BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
            if(bungieUser.isRegistered()) {
                if(args.length == 0) {
                    bungieUser.requestVendor(VendorType.SPIDER, ComponentType.VENDOR_SALES);
                    bungieUser.requestProfile(ComponentType.PROFILE_CURRENCIES);
                    bungieUser.requestProfile(ComponentType.PROFILE_INVENTORIES);

                    JsonObject spiderObject = bungieUser.getVendor(VendorType.SPIDER, ComponentType.VENDOR_SALES);

                    if(spiderObject == null) {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Derzeit können keine Daten über Spider gesammelt werden.").build()).complete();
                        return;
                    }

                    SpiderInformation spiderInformation = new SpiderInformation(spiderObject);
                    event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, spiderInformation).build()).complete();
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Spider").build()).queue(message -> {
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

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, SpiderInformation spiderInformationInformation) {
        StringBuilder description = new StringBuilder();

        description.append("Im Gegensatz zu seinen Gefallenen-Brüdern verhandelt der clevere Spider lieber, statt zu kämpfen.");
        description.append("\n");
        description.append("\n");
        description.append("**Offers:**");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Vendors > Spider, The Shore's Only Law")
                .setDescription(description.toString())
                .setThumbnail("https://vhost106.dein-gameserver.tech/spider.png");

                for(JsonObject sale : spiderInformationInformation.getSales(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) {

                    String itemName = spiderInformationInformation.getItemName(sale, targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)).replaceAll("Purchase ", "");
                    long itemHash = spiderInformationInformation.getItemHash(sale);
                    MaterialType materialType = MaterialType.getFromPurchaseItemHash(itemHash);
                    CurrenciesType currenciesType = CurrenciesType.getFromPurchaseItemHash(itemHash);
                    int ownQuantity = materialType == null ? currenciesType == null ? -1 : targetUser.getCurrenciesQuantity(currenciesType) : targetUser.getMaterialQuantity(materialType);
                    int quantity = spiderInformationInformation.getItemQuantity(sale);

                    long costItemHash = spiderInformationInformation.getCostItemHash(sale);
                    int costItemQuantity = spiderInformationInformation.getCostItemQuantity(sale);
                    MaterialType costMaterialType = MaterialType.getFromItemHash(costItemHash);
                    CurrenciesType costCurrenciesType = CurrenciesType.getFromItemHash(costItemHash);
                    int ownCostItemQuantity = costMaterialType == null ? costCurrenciesType == null ? -1 : targetUser.getCurrenciesQuantity(costCurrenciesType) : targetUser.getMaterialQuantity(costMaterialType);

                    StringBuilder fieldDescription = new StringBuilder();

                    fieldDescription.append("**Owned:** " + this.formatInteger(ownQuantity) + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getMaterial(String.valueOf(materialType == null ? currenciesType == null ? "-1" : currenciesType.getItemHash() : materialType.getItemHash()))).getAsMention());
                    fieldDescription.append("\n");
                    fieldDescription.append("**Cost:** " + this.formatInteger(costItemQuantity) + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getMaterial(String.valueOf(costItemHash))).getAsMention());
                    fieldDescription.append("\n");
                    fieldDescription.append("**Owned:** " + this.formatInteger(ownCostItemQuantity) + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getMaterial(String.valueOf(costItemHash))).getAsMention());

                    embedBuilder.addField(this.formatInteger(quantity) + "x " + itemName, fieldDescription.toString(), true);
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
