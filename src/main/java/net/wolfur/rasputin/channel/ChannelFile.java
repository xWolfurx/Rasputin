package net.wolfur.rasputin.channel;

import net.wolfur.rasputin.file.builder.FileBase;
import net.wolfur.rasputin.file.builder.yaml.FileConfiguration;

import java.util.*;

public class ChannelFile extends FileBase {

    private List<Channel> channels;

    public ChannelFile() {
        super("files", "channel");
        this.channels = new ArrayList<>();

        this.writeDefaults();
        this.loadData();
    }

    private void writeDefaults() {
        FileConfiguration cfg = getConfig();

        List<String> channels = Arrays.asList(new String[] { "log", "error", "command", "ban_history", "raid_history", "news", "raid", "talk", "vendor" } );
        cfg.addDefault("channels", channels);

        for(String channelName : channels) {
            cfg.addDefault("channel." + channelName + ".id", Long.valueOf(-1L));
            cfg.addDefault("channel." + channelName + ".command", Boolean.valueOf(false));
            cfg.addDefault("channel." + channelName + ".experience", Boolean.valueOf(false));
        }

        cfg.options().copyDefaults(true);
        saveConfig();
    }

    private void loadData() {
        FileConfiguration cfg = getConfig();
        List<String> channels = cfg.getStringList("channels");

        for(String channelName : channels) {
            long channelId = cfg.getLong("channel." + channelName + ".id");
            boolean command = cfg.getBoolean("channel." + channelName + ".command");
            boolean experience = cfg.getBoolean("channel." + channelName + ".experience");

            this.channels.add(new Channel(channelId, channelName, command, experience));
        }
    }

    public Channel getChannel(String channelName) {
        for(Channel channel : this.channels) {
            if(channel.getName().equals(channelName)) {
                return channel;
            }
        }
        return null;
    }

    public Channel getChannel(long channelId) {
        for(Channel channel : this.channels) {
            if(channel.getChannelId() == channelId) {
                return channel;
            }
        }
        return null;
    }

}
