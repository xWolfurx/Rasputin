package net.wolfur.rasputin.permission;

import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.core.CommandHandler;
import net.wolfur.rasputin.file.builder.FileBase;
import net.wolfur.rasputin.file.builder.yaml.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionFile extends FileBase {

    private List<CommandPermission> commandPermissions;

    public PermissionFile() {
        super("files", "permissions");
        this.commandPermissions = new ArrayList<>();

        this.writeDefaults();
        this.loadData();
    }

    private void writeDefaults() {
        FileConfiguration cfg = getConfig();

        for(String commandName : CommandHandler.commands.keySet()) {
            cfg.addDefault("command." + commandName + ".needPermission", Boolean.valueOf(true));
            cfg.addDefault("command." + commandName + ".whitelistedRoles", Arrays.asList(new Long[] { -1L }));
            cfg.addDefault("command." + commandName + ".whitelistedUsers", Arrays.asList(new Long[] { -1L }));
        }

        cfg.options().copyDefaults(true);
        saveConfig();
    }

    private void loadData() {
        FileConfiguration cfg = getConfig();

        for(String commandName : CommandHandler.commands.keySet()) {
            boolean needPermission = cfg.getBoolean("command." + commandName + ".needPermission");
            List<Long> whitelistedRoles = cfg.getLongList("command." + commandName + ".whitelistedRoles");
            List<Long> whitelistedUsers = cfg.getLongList("command." + commandName + ".whitelistedUsers");

            this.commandPermissions.add(new CommandPermission(CommandHandler.commands.get(commandName), needPermission, whitelistedRoles, whitelistedUsers));
        }
    }

    public CommandPermission getCommandPermission(Command command) {
        for(CommandPermission commandPermission : this.commandPermissions) {
            if(commandPermission.getCommand().equals(command)) {
                return commandPermission;
            }
        }
        return null;
    }
}
