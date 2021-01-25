package net.wolfur.rasputin.manager;

import net.wolfur.rasputin.bungie.BungieUserManager;

public class CoreManager {

    private RaidManager raidManager;

    private BungieUserManager bungieUserManager;

    private BanManager banManager;
    private StatusManager statusManager;

    public CoreManager() {
        this.raidManager = new RaidManager();
        this.banManager = new BanManager();
        this.statusManager = new StatusManager();

        this.bungieUserManager = new BungieUserManager();
    }

    public RaidManager getRaidManager() {
        return this.raidManager;
    }

    public BanManager getBanManager() {
        return this.banManager;
    }

    public StatusManager getStatusManager() {
        return this.statusManager;
    }

    public BungieUserManager getBungieUserManager() {
        return this.bungieUserManager;
    }
}
