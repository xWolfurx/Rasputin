package net.wolfur.rasputin.bungie.type;

public enum CurrenciesType {

    GLIMMER(3159615086L, 3664001560L),
    LEGENDARY_SHARDS(1022552290L, 2654422615L),
    BRIGHT_DUST(2817410917L, -1L);

    private final long itemHash;
    private final long purchaseItemHash;

    CurrenciesType(long itemHash, long purchaseItemHash) {
        this.itemHash = itemHash;
        this.purchaseItemHash = purchaseItemHash;
    }

    public long getItemHash() {
        return this.itemHash;
    }

    public long getPurchaseItemHash() {
        return this.purchaseItemHash;
    }

    public static CurrenciesType getFromItemHash(long itemHash) {
        for(CurrenciesType currenciesType : values()) {
            if(currenciesType.getItemHash() == itemHash) {
                return currenciesType;
            }
        }
        return null;
    }

    public static CurrenciesType getFromPurchaseItemHash(long itemHash) {
        for(CurrenciesType currenciesType : values()) {
            if(currenciesType.getPurchaseItemHash() == itemHash) {
                return currenciesType;
            }
        }
        return null;
    }

}
