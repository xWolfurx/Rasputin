package net.wolfur.rasputin.bungie.type;

public enum ComponentType {

    NONE(0, "None"),
    PROFILES(100, "Profiles"),
    VENDOR_RECEIPTS(101, "Vendor Receipts"),
    PROFILE_INVENTORIES(102, "Profile Inventories"),
    PROFILE_CURRENCIES(103, "Profile Currencies"),
    PROFILE_PROGRESSION(104, "Profile Progression"),
    PLATFORM_SILVER(105, "Platform Silver"),
    CHARACTERS(200, "Characters"),
    CHARACTER_INVENTORIES(201, "Character Inventories"),
    CHARACTER_PROGRESSIONS(202, "Character Progressions"),
    CHARACTER_RENDER_DATA(203, "Character Render Data"),
    CHARACTER_ACTIVITIES(204, "Character Activities"),
    CHARACTER_EQUIPMENT(205, "Character Equipment"),
    ITEM_INSTANCES(300, "Item Instances"),
    ITEM_OBJECTIVES(301, "Item Objectives"),
    ITEM_PERKS(302, "Item Perks"),
    ITEM_RENDER_DATA(303, "Item Render Data"),
    ITEM_STATS(304, "Item Stats"),
    ITEM_SOCKETS(305, "Item Sockets"),
    ITEM_TALENT_GRIDS(306, "Item Talent Grids"),
    ITEM_COMMON_DATA(307, "Item Common Data"),
    ITEM_PLUG_STATES(308, "Item Plug States"),
    ITEM_PLUG_OBJECTIVES(309, "Item Plug Objectives"),
    ITEM_REUSABLE_PLUGS(310, "Item Reusable Plugs"),
    VENDORS(400, "Vendors"),
    VENDOR_CATEGORIES(401, "Vendor Categories"),
    VENDOR_SALES(402, "Vendor Sales"),
    KIOSKS(500, "Kiosks"),
    CURRENCY_LOOKUPS(600, "Currency Lookups"),
    PRESENTATION_NODES(700, "Presentation Nodes"),
    COLLECTIBLES(800, "Collectibles"),
    RECORDS(900, "Records"),
    TRANSITORY(1000, "Transitory"),
    METRICS(1100, "Metrics");

    private final int componentId;
    private final String betterName;

    ComponentType(int componentId, String betterName) {
        this.componentId = componentId;
        this.betterName = betterName;
    }

    public int getComponentId() {
        return this.componentId;
    }

    public String getBetterName() {
        return this.betterName;
    }

}
