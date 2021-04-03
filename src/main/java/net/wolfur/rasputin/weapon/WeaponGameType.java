package net.wolfur.rasputin.weapon;

public enum WeaponGameType {

    PVE("PvE"),
    PVP("PvP");

    private String betterName;

    WeaponGameType(String betterName) {
        this.betterName = betterName;
    }

    public String getBetterName() {
        return this.betterName;
    }

    public static WeaponGameType getByName(String name) {
        for(WeaponGameType weaponGameType : values()) {
            if(weaponGameType.name().equalsIgnoreCase(name) || weaponGameType.getBetterName().equalsIgnoreCase(name)) {
                return weaponGameType;
            }
        }
        return null;
    }
}
