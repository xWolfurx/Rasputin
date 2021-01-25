package net.wolfur.rasputin.bungie.type;

public enum MaterialType {

    BARYON_BOUGHS(592227263L, 778553120L),
    PHASEGLASS_NEEDLE(1305274547L, 924468777L),
    SIMULATION_SEED(49145143L, 1420498062L),
    GLACIAL_STARWORT(1485756901L, 1760701414L),
    ENHANCEMENT_CORES(3853748946L, 1812969468L),
    MICROPHASIC_DATALATTICE(3487922223L, 1845310989L),
    HELIUM_FILAMENTS(3592324052L, 1923884703L),
    SERAPHITE(31293053L, 2536947844L),
    ENHANCEMENT_PRISMS(4257549984L, 3106913645L),
    ETHERIC_SPIRAL(1177810185L, 3245502278L),
    DUSKLIGHT_SHARDS(950899352L, 3721881826L),
    SPINMETAL_LEAVES(293622383L, 4106973372L),
    ALKANE_DUST(2014411539L, 4153440841L);

    private final long itemHash;
    private final long purchaseItemHash;

    MaterialType(long itemHash, long purchaseItemHash) {
        this.itemHash = itemHash;
        this.purchaseItemHash = purchaseItemHash;
    }

    public long getItemHash() {
        return this.itemHash;
    }

    public long getPurchaseItemHash() {
        return this.purchaseItemHash;
    }

    public static MaterialType getFromItemHash(long itemHash) {
        for(MaterialType materialType : values()) {
            if(materialType.getItemHash() == itemHash) {
                return materialType;
            }
        }
        return null;
    }

    public static MaterialType getFromPurchaseItemHash(long itemHash) {
        for(MaterialType materialType : values()) {
            if(materialType.getPurchaseItemHash() == itemHash) {
                return materialType;
            }
        }
        return null;
    }

}
