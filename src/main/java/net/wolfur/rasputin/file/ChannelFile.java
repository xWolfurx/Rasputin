package net.wolfur.rasputin.file;

import net.wolfur.rasputin.file.builder.FileBase;
import net.wolfur.rasputin.file.builder.yaml.FileConfiguration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChannelFile extends FileBase {

    private Map<String, Long> channels;

    public ChannelFile() {
        super("files", "channel");
        this.channels = new HashMap<>();

        this.writeDefaults();

        this.loadChannels();
    }

    private void writeDefaults() {
        FileConfiguration cfg = getConfig();

        cfg.addDefault("channel.log.id", Long.valueOf(-1L));
        cfg.addDefault("channel.error.id", Long.valueOf(-1L));
        cfg.addDefault("channel.command.id", Long.valueOf(-1L));
        cfg.addDefault("channel.banHistory.id", Long.valueOf(-1L));
        cfg.addDefault("channel.raidHistory.id", Long.valueOf(-1L));
        cfg.addDefault("channel.news.id", Long.valueOf(-1L));
        cfg.addDefault("channel.raid.id", Long.valueOf(-1L));
        cfg.addDefault("channel.talk.id", Long.valueOf(-1));
        cfg.addDefault("channel.vendor.id", Long.valueOf(-1));

        cfg.options().copyDefaults(true);
        saveConfig();
    }

    private void loadChannels() {
        FileConfiguration cfg = getConfig();
        for(String channelName : cfg.getConfigurationSection("channel").getKeys(false)) {
            long channelId = cfg.getLong("channel." + channelName + ".id");
            this.channels.put(channelName.toLowerCase(), channelId);
        }
    }

    public Long getChannelId(String channelName) {
        if(this.channels.containsKey(channelName.toLowerCase())) return this.channels.get(channelName.toLowerCase());
        return -1L;
    }

    public boolean isCommandChannel(long channelId) {
        if((channelId == this.getChannelId("command")) || (channelId == this.getChannelId("talk"))) return true;
        return false;
    }

}
