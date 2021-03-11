package net.wolfur.rasputin.bungie.item;

import com.google.gson.JsonObject;

public class DestinyItem {

    private long itemHash;
    private long itemInstanceId;
    private int quantity;
    private long bucketHash;

    public DestinyItem(long itemHash, long itemInstanceId, int quantity, long bucketHash) {
        this.itemHash = itemHash;
        this.itemInstanceId = itemInstanceId;
        this.quantity = quantity;
        this.bucketHash = bucketHash;
    }

    public long getItemHash() {
        return this.itemHash;
    }

    public long getItemInstanceId() {
        return this.itemInstanceId;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public long getBucketHash() {
        return this.bucketHash;
    }

    public String getItemName(JsonObject destinyInventoryItemDefinitionObject) {
        return destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonObject("displayProperties").getAsJsonPrimitive("name").getAsString();
    }

    public String getItemType(JsonObject destinyInventoryItemDefinitionObject) {
        return destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("itemType").getAsString();
    }

    public String getItemTypeDisplayName(JsonObject destinyInventoryItemDefinitionObject) {
        return destinyInventoryItemDefinitionObject.getAsJsonObject(String.valueOf(itemHash)).getAsJsonPrimitive("itemTypeDisplayName").getAsString();
    }
}
