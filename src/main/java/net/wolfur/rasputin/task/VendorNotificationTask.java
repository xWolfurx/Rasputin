package net.wolfur.rasputin.task;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.information.GunsmithInformation;
import net.wolfur.rasputin.bungie.information.SpiderInformation;
import net.wolfur.rasputin.bungie.information.XurInformation;
import net.wolfur.rasputin.bungie.type.*;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VendorNotificationTask implements Runnable {

    private ScheduledExecutorService service;

    public VendorNotificationTask() {
        this.service = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
        ZonedDateTime nextRun = now.withHour(Main.getFileManager().getConfigFile().getResetHour()).withMinute(1).withSecond(0);

        if(now.compareTo(nextRun) > 0) nextRun = nextRun.plusDays(1);

        Duration duration = Duration.between(now, nextRun);
        long initialDelay = duration.getSeconds();

        this.service.scheduleAtFixedRate(this, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        Logger.info("Next vendor notification in " + TimeUtil.timeToString(duration.toMillis(), true) + ".", true);
    }

    public void stop() {
        if(!isCurrentlyRunning()) return;
        this.service.shutdown();
        this.service = null;
    }

    public boolean isCurrentlyRunning() {
        return this.service != null;
    }

    @Override
    public void run() {
        if(Main.getFileManager().getConfigFile().getDefaultBungieUser() == -1L) {
            Logger.warning("No default bungie user has been set.", true);
            return;
        }
        if(Main.getFileManager().getChannelFile().getChannel("vendor") == null) {
            Logger.warning("No channel id for vendor notification has been found.", true);
            return;
        }

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("vendor").getChannelId());
        MessageHistory history = new MessageHistory(textChannel);
        List<Message> messages;

        try {
            while (true) {
                messages = history.retrievePast(1).complete();
                messages.get(0).delete().queue();
            }
        } catch (Exception e) {
        }

        User defaultUser = Main.getJDA().retrieveUserById(Main.getFileManager().getConfigFile().getDefaultBungieUser()).complete();
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(defaultUser);

        if(!bungieUser.isRegistered()) {
            Logger.warning("The default bungie user is not registered.", true);
            return;
        }

        bungieUser.requestVendor(VendorType.BANSHEE_44, ComponentType.VENDOR_SALES);
        bungieUser.requestVendor(VendorType.SPIDER, ComponentType.VENDOR_SALES);
        /**bungieUser.requestVendor(VendorType.XUR, ComponentType.VENDOR_SALES);

        String xurLocation = bungieUser.requestXurLocation();
        JsonObject xurObject = bungieUser.getVendor(VendorType.XUR, ComponentType.VENDOR_SALES);

        if(xurObject != null) {
            XurInformation xurInformation = new XurInformation(xurObject);
            if(!xurInformation.isActive()){
                textChannel.sendMessage(this.createXurEmbedBuilder(bungieUser, xurInformation, xurLocation).build()).complete();
            }
        }**/

        JsonObject spiderObject = bungieUser.getVendor(VendorType.SPIDER, ComponentType.VENDOR_SALES);

        if(spiderObject != null) {
            SpiderInformation spiderInformation = new SpiderInformation(spiderObject);
            textChannel.sendMessage(this.createSpiderEmbedBuilder(bungieUser, spiderInformation).build()).complete();
        }

        JsonObject gunsmithObject = bungieUser.getVendor(VendorType.BANSHEE_44, ComponentType.VENDOR_SALES);

        if(gunsmithObject != null) {
            GunsmithInformation gunsmithInformation = new GunsmithInformation(gunsmithObject);
            textChannel.sendMessage(this.createGunsmithEmbedBuilder(bungieUser, gunsmithInformation).build()).complete();
        }

    }

    private EmbedBuilder createGunsmithEmbedBuilder(BungieUser targetUser, GunsmithInformation gunsmithInformation) {
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
                    fieldDescription.append(this.formatInteger(costItem.getQuantity()) + " ");
                    fieldDescription.append(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getMaterial(String.valueOf(costItem.getItemHash()))).getAsMention());
                    fieldDescription.append("\n");
                }
                embedBuilder.addField(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getMaterial(String.valueOf(itemHash))).getAsMention() + itemName, fieldDescription.toString(), true);
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

    private EmbedBuilder createXurEmbedBuilder(BungieUser targetUser, XurInformation xurInformation, String location) {
        StringBuilder description = new StringBuilder();

        String weapon = "[" + xurInformation.getItemName(xurInformation.getWeaponObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)), targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)) + "](https://destinytracker.com/destiny-2/db/items/" + xurInformation.getItemHash(xurInformation.getWeaponObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + ")" + "\n" + "**Cost: **" + xurInformation.getCostItemQuantity(xurInformation.getWeaponObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + " " + xurInformation.getSymbol(xurInformation.getCostItemHash(xurInformation.getWeaponObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))));
        String hunterArmor = "[" + xurInformation.getItemName(xurInformation.getHunterArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)), targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)) + "](https://destinytracker.com/destiny-2/db/items/" + xurInformation.getItemHash(xurInformation.getHunterArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + ")" + "\n" + "**Cost: **" + xurInformation.getCostItemQuantity(xurInformation.getHunterArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + " " + xurInformation.getSymbol(xurInformation.getCostItemHash(xurInformation.getHunterArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))));
        String titanArmor = "[" + xurInformation.getItemName(xurInformation.getTitanArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)), targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)) + "](https://destinytracker.com/destiny-2/db/items/" + xurInformation.getItemHash(xurInformation.getTitanArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + ")" + "\n" + "**Cost: **" + xurInformation.getCostItemQuantity(xurInformation.getTitanArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + " " + xurInformation.getSymbol(xurInformation.getCostItemHash(xurInformation.getTitanArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))));
        String warlockArmor = "[" + xurInformation.getItemName(xurInformation.getWarlockArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)), targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION)) + "](https://destinytracker.com/destiny-2/db/items/" + xurInformation.getItemHash(xurInformation.getWarlockArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + ")" + "\n" + "**Cost: **" + xurInformation.getCostItemQuantity(xurInformation.getWarlockArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))) + " " + xurInformation.getSymbol(xurInformation.getCostItemHash(xurInformation.getWarlockArmorObject(targetUser.getManifest(DestinyDefinitionType.DESTINY_INVENTORY_ITEM_DEFINITION))));

        description.append("Xûr handelt mit seltsamen Kuriositäten und seine Motive sind undurchsichtig. Er beugt sich seinen fernen Meistern, den Neun.");
        description.append("\n");
        description.append("\n");
        description.append("**Location:** " + location);
        description.append("\n");
        description.append("\n");
        description.append("**Offers:**");

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Vendors > Xûr, Agent of the Nine")
                .setDescription(description.toString())
                .addField(Utils.getEmote("hunter").getAsMention() + "Hunter Armor", hunterArmor, true)
                .addField(Utils.getEmote("titan").getAsMention() + "Titan Armor", titanArmor, true)
                .addField(Utils.getEmote("warlock").getAsMention() + "Warlock Armor", warlockArmor, true)
                .addField("Weapon", weapon, true)
                .setThumbnail("https://vhost106.dein-gameserver.tech/xur.png");

        return embedBuilder;
    }

    private EmbedBuilder createSpiderEmbedBuilder(BungieUser targetUser, SpiderInformation spiderInformationInformation) {
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
            int quantity = spiderInformationInformation.getItemQuantity(sale);

            long costItemHash = spiderInformationInformation.getCostItemHash(sale);
            int costItemQuantity = spiderInformationInformation.getCostItemQuantity(sale);

            StringBuilder fieldDescription = new StringBuilder();

            fieldDescription.append("\n");
            fieldDescription.append("**Cost:** " + this.formatInteger(costItemQuantity) + " " + Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getMaterial(String.valueOf(costItemHash))).getAsMention());

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
