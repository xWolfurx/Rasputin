package net.wolfur.rasputin.manager;

import net.wolfur.rasputin.channel.ChannelFile;
import net.wolfur.rasputin.file.ConfigFile;
import net.wolfur.rasputin.file.EmoteDefinitionFile;
import net.wolfur.rasputin.file.RoleDefinitionFile;

public class FileManager {

    private ConfigFile configFile;
    private ChannelFile channelFile;
    private EmoteDefinitionFile emoteDefinitionFile;
    private RoleDefinitionFile roleDefinitionFile;

    public FileManager() {
        this.configFile = new ConfigFile();
        this.channelFile = new ChannelFile();
        this.emoteDefinitionFile = new EmoteDefinitionFile();
        this.roleDefinitionFile = new RoleDefinitionFile();
    }

    public ConfigFile getConfigFile() {
        return this.configFile;
    }

    public ChannelFile getChannelFile() {
        return this.channelFile;
    }

    public EmoteDefinitionFile getEmoteDefinitionFile() {
        return this.emoteDefinitionFile;
    }

    public RoleDefinitionFile getRoleDefinitionFile() {
        return this.roleDefinitionFile;
    }

}
