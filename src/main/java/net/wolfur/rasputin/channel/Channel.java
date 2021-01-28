package net.wolfur.rasputin.channel;

public class Channel {

    private long channelId;
    private String name;
    private boolean command;
    private boolean experience;

    public Channel(long channelId, String name, boolean command, boolean experience) {
        this.channelId = channelId;
        this.name = name;
        this.command = command;
        this.experience = experience;
    }

    public long getChannelId() {
        return this.channelId;
    }

    public String getName() {
        return this.name;
    }

    public boolean isCommandChannel() {
        return this.command;
    }

    public boolean isExperienceChannel() {
        return this.experience;
    }
}
