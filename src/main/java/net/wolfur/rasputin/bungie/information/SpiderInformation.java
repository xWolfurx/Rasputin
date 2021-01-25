package net.wolfur.rasputin.bungie.information;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class SpiderInformation {

    private final List<JsonObject> sales;

    public SpiderInformation(JsonObject xurObject) {
        List<JsonObject> sales = new ArrayList<>();

        JsonObject salesData = xurObject.getAsJsonObject("Response").getAsJsonObject("sales").getAsJsonObject("data");
        Iterable<String> saleNumbers = salesData.keySet();

        for(String saleNumber : saleNumbers) {
            JsonObject saleObject = salesData.getAsJsonObject(saleNumber);
            sales.add(saleObject);
        }

        this.sales = sales;
    }

    public List<JsonObject> getSales(JsonObject destinyInventoryItemDefinitionObject) {
        List<JsonObject> sales = new ArrayList<>();

        for(JsonObject sale : this.sales) {
            long itemHash = sale.get("itemHash").getAsLong();
            String itemType = destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("itemType").getAsString();
            if(itemType.equalsIgnoreCase("20")) {
                sales.add(sale);
            }
        }

        return sales;
    }


    public String getItemName(JsonObject sale, JsonObject destinyInventoryItemDefinitionObject) {
        long itemHash = sale.get("itemHash").getAsLong();
        return destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonObject("displayProperties").getAsJsonPrimitive("name").getAsString();
    }

    public int getItemQuantity(JsonObject sale) {
        return sale.get("quantity").getAsInt();
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

    @SuppressWarnings("unused")
    public String getSymbol(long itemHash) {
        return Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getMaterial(String.valueOf(itemHash))).getAsMention();
    }

}
