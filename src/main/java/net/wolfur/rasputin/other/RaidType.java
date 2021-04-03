package net.wolfur.rasputin.other;

import java.util.ArrayList;
import java.util.List;

public class RaidType {

    private String name;
    private String betterName;
    private String shortcuts;
    private int maxPlayers;
    private String iconURL;
    private String activityHash;
    private long channelId;

    public RaidType(String name, String betterName, String shortcuts, int maxPlayers, String iconURL, String activityHash, long channelId) {
        this.name = name;
        this.betterName = betterName;
        this.shortcuts = shortcuts;
        this.maxPlayers = maxPlayers;
        this.iconURL = iconURL;
        this.activityHash = activityHash;
        this.channelId = channelId;
    }

    public String getName() {
        return this.name;
    }

    public String getBetterName() {
        return this.betterName;
    }

    public String getShortcuts() {
        return this.shortcuts;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public String getIconURL() {
        return this.iconURL;
    }

    public String getActivityHash() {
        return this.activityHash;
    }

    public long getChannelId() {
        return this.channelId;
    }

    public List<String> getShortcutsAsList() {
        List<String> list = new ArrayList<>();
        if(this.shortcuts.contains(";")) {
            String[] shortcutsArray = this.shortcuts.split(";");
            for(String shortcut : shortcutsArray) {
                list.add(shortcut.toLowerCase());
            }
        } else {
            list.add(this.shortcuts.toLowerCase());
        }
        return list;
    }

}
