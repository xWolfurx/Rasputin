package net.wolfur.rasputin.bungie.type;

public enum VendorType {

    XUR(2190858386L),
    SPIDER(863940356L),
    BANSHEE_44(672118013L),
    TESS_EVERIS(3361454721L);

    private final long vendorId;

    VendorType(long vendorId) {
        this.vendorId = vendorId;
    }

    public long getVendorId() {
        return this.vendorId;
    }
}
