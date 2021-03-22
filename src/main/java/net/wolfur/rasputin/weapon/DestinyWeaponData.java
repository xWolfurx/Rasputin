package net.wolfur.rasputin.weapon;

import java.util.List;

public class DestinyWeaponData {

    private String name;
    private long itemHash;

    private List<Long> godRoll;
    private List<Long> alternativeRoll;

    public DestinyWeaponData(String name, long itemHash, List<Long> godRoll, List<Long> alternativeRoll) {
        this.name = name;
        this.itemHash = itemHash;
        this.godRoll = godRoll;
        this.alternativeRoll = alternativeRoll;
    }

    public String getName() {
        return this.name;
    }

    public long getItemHash() {
        return this.itemHash;
    }

    public List<Long> getGodRoll() {
        return this.godRoll;
    }

    public List<Long> getAlternativeRoll() {
        return this.alternativeRoll;
    }
}
