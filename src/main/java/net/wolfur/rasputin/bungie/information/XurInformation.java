package net.wolfur.rasputin.bungie.information;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class XurInformation {

    private final List<JsonObject> sales;
    boolean active = true;

    public XurInformation(JsonObject xurObject) {
        List<JsonObject> sales = new ArrayList<>();

        JsonObject salesData = xurObject.getAsJsonObject("Response").getAsJsonObject("sales").getAsJsonObject("data");
        Iterable<String> saleNumbers = salesData.keySet();

        for(String saleNumber : saleNumbers) {
            JsonObject saleObject = salesData.getAsJsonObject(saleNumber);
            sales.add(saleObject);
        }

        this.sales = sales;

        if(sales.size() == 1) {
            active = false;
        }
    }

    public JsonObject getWeaponObject(JsonObject destinyInventoryItemDefinitionObject) {
        for(JsonObject sale : this.sales) {
            long itemHash = sale.get("itemHash").getAsLong();
            String itemType = destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("itemType").getAsString();

            if(itemType.equalsIgnoreCase("3")) {
                return sale;
            }
        }
        return null;
    }

    public JsonObject getTitanArmorObject(JsonObject destinyInventoryItemDefinitionObject) {
        for(JsonObject sale : this.sales) {
            long itemHash = sale.get("itemHash").getAsLong();
            String itemType = destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("itemType").getAsString();
            String classType = destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("classType").getAsString();

            if(itemType.equalsIgnoreCase("2") && classType.equalsIgnoreCase("0")) {
                return sale;
            }
        }
        return null;
    }

    public JsonObject getHunterArmorObject(JsonObject destinyInventoryItemDefinitionObject) {
        for(JsonObject sale : this.sales) {
            long itemHash = sale.get("itemHash").getAsLong();
            String itemType = destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("itemType").getAsString();
            String classType = destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("classType").getAsString();

            if(itemType.equalsIgnoreCase("2") && classType.equalsIgnoreCase("1")) {
                return sale;
            }
        }
        return null;
    }

    public JsonObject getWarlockArmorObject(JsonObject destinyInventoryItemDefinitionObject) {
        for(JsonObject sale : this.sales) {
            long itemHash = sale.get("itemHash").getAsLong();
            String itemType = destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("itemType").getAsString();
            String classType = destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("classType").getAsString();

            if(itemType.equalsIgnoreCase("2") && classType.equalsIgnoreCase("2")) {
                return sale;
            }
        }
        return null;
    }

    public String getItemName(JsonObject sale, JsonObject destinyInventoryItemDefinitionObject) {
        long itemHash = sale.get("itemHash").getAsLong();
        return destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonObject("displayProperties").getAsJsonPrimitive("name").getAsString();
    }

    public long getItemHash(JsonObject sale) {
        return sale.get("itemHash").getAsLong();
    }

    public long getCostItemHash(JsonObject sale) {
        JsonArray costsArray = sale.getAsJsonArray("costs");
        for(int i = 0; i < costsArray.size(); i++) {
            return costsArray.get(i).getAsJsonObject().get("itemHash").getAsLong();
        }
        return -1L;
    }

    public int getCostItemQuantity(JsonObject sale) {
        JsonArray costsArray = sale.getAsJsonArray("costs");
        for(int i = 0; i < costsArray.size(); i++) {
            return costsArray.get(i).getAsJsonObject().get("quantity").getAsInt();
        }
        return -1;
    }

    public String getSymbol(long itemHash) {
        return Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getMaterial(String.valueOf(itemHash))).getAsMention();
    }

    public boolean isActive() {
        return this.active;
    }

}
