package net.wolfur.rasputin.weapon;

import java.util.List;

public class DestinyWeaponData {

    private String name;
    private long itemHash;

    private WeaponGameType weaponGameType;
    private long masterwork;
    private long alternativeMasterwork;

    private List<Long> godRoll;
    private List<Long> alternativeRoll;

    public DestinyWeaponData(String name, long itemHash, WeaponGameType weaponGameType, long masterwork, long alternativeMasterwork, List<Long> godRoll, List<Long> alternativeRoll) {
        this.name = name;
        this.itemHash = itemHash;
        this.weaponGameType = weaponGameType;
        this.masterwork = masterwork;
        this.alternativeMasterwork = alternativeMasterwork;
        this.godRoll = godRoll;
        this.alternativeRoll = alternativeRoll;
    }

    public String getName() {
        return this.name;
    }

    public long getItemHash() {
        return this.itemHash;
    }

    public WeaponGameType getWeaponGameType() {
        return this.weaponGameType;
    }

    public long getMasterwork() {
        return this.masterwork;
    }

    public long getAlternativeMasterwork() {
        return this.alternativeMasterwork;
    }

    public List<Long> getGodRoll() {
        return this.godRoll;
    }

    public List<Long> getAlternativeRoll() {
        return this.alternativeRoll;
    }
}
