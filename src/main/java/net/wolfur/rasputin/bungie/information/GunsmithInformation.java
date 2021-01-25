package net.wolfur.rasputin.bungie.information;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
public class GunsmithInformation {

    private final List<JsonObject> sales;

    public GunsmithInformation(JsonObject gunsmithObject) {
        List<JsonObject> sales = new ArrayList<>();

        JsonObject salesData = gunsmithObject.getAsJsonObject("Response").getAsJsonObject("sales").getAsJsonObject("data");
        Iterable<String> saleNumbers = salesData.keySet();

        for(String saleNumber : saleNumbers) {
            JsonObject saleObject = salesData.getAsJsonObject(saleNumber);
            sales.add(saleObject);
        }

        this.sales = sales;
    }

    public List<JsonObject> getSales() {
        return this.sales;
    }


    public String getItemName(JsonObject sale, JsonObject destinyInventoryItemDefinitionObject) {
        long itemHash = sale.get("itemHash").getAsLong();
        return destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonObject("displayProperties").getAsJsonPrimitive("name").getAsString();
    }

    public long getItemHash(JsonObject sale) {
        return sale.get("itemHash").getAsLong();
    }

    public List<CostItem> getCostItems(JsonObject sale) {
        List<CostItem> costItems = new ArrayList<>();
        JsonArray costsArray = sale.getAsJsonArray("costs");
        for(int i = 0; i < costsArray.size(); i++) {
            long itemHash = costsArray.get(i).getAsJsonObject().get("itemHash").getAsLong();
            int quantity = costsArray.get(i).getAsJsonObject().get("quantity").getAsInt();
            costItems.add(new CostItem(itemHash, quantity));
        }

        return costItems;
    }

    public String getItemType(JsonObject sale, JsonObject destinyInventoryItemDefinitionObject) {
        long itemHash = sale.get("itemHash").getAsLong();
        return destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("itemType").getAsString();
    }

    public String getItemTypeDisplayName(JsonObject sale, JsonObject destinyInventoryItemDefinitionObject) {
        long itemHash = sale.get("itemHash").getAsLong();
        return destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("itemTypeDisplayName").getAsString();
    }


    public static class CostItem {

        private final long itemHash;
        private final int quantity;

        public CostItem(long itemHash, int quantity) {
            this.itemHash = itemHash;
            this.quantity = quantity;
        }

        public long getItemHash() {
            return this.itemHash;
        }

        public int getQuantity() {
            return this.quantity;
        }

    }

}
