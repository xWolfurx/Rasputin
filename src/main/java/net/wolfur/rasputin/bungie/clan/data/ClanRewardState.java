package net.wolfur.rasputin.bungie.clan.data;

public class ClanRewardState {

    boolean gambit, nightfall, raid, crucible;

    public ClanRewardState(boolean gambit, boolean nightfall, boolean raid, boolean crucible) {
        this.gambit = gambit;
        this.nightfall = nightfall;
        this.raid = raid;
        this.crucible = crucible;
    }

    public boolean hasGambit() {
        return this.gambit;
    }

    public boolean hasNightfall() {
        return this.nightfall;
    }

    public boolean hasRaid() {
        return this.raid;
    }

    public boolean hasCrucible() {
        return this.crucible;
    }
}
