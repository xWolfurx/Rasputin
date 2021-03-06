package net.wolfur.rasputin.bungie.type;

public enum DestinyDefinitionType {

    DESTINY_INVENTORY_ITEM_DEFINITION("DestinyInventoryItemDefinition"),
    DESTINY_ACTIVITY_DEFINITION("DestinyActivityDefinition"),
    DESTINY_ACTIVITY_MODIFIER_DEFINITION("DestinyActivityModifierDefinition"),
    DESTINY_ACTIVITY_MODE_DEFINITION("DestinyActivityModeDefinition");

    private final String pathName;

    DestinyDefinitionType(String pathName) {
        this.pathName = pathName;
    }

    public String getPathName() {
        return this.pathName;
    }
}
