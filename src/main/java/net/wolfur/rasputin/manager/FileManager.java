package net.wolfur.rasputin.manager;

import net.wolfur.rasputin.channel.ChannelFile;
import net.wolfur.rasputin.file.ConfigFile;
import net.wolfur.rasputin.file.EmoteDefinitionFile;
import net.wolfur.rasputin.file.RoleDefinitionFile;
import net.wolfur.rasputin.permission.PermissionFile;

public class FileManager {

    private ConfigFile configFile;
    private ChannelFile channelFile;
    private EmoteDefinitionFile emoteDefinitionFile;
    private RoleDefinitionFile roleDefinitionFile;
    private PermissionFile permissionFile;

    public FileManager() {
        this.configFile = new ConfigFile();
        this.channelFile = new ChannelFile();
        this.emoteDefinitionFile = new EmoteDefinitionFile();
        this.roleDefinitionFile = new RoleDefinitionFile();
        this.permissionFile = new PermissionFile();
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

    public PermissionFile getPermissionFile() {
        return this.permissionFile;
    }

}
