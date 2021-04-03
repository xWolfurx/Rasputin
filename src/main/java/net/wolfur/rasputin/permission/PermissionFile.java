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
            cfg.addDefault("command." + commandName + ".blacklistedUsers", Arrays.asList(new Long[] { -1L }));
            cfg.addDefault("command." + commandName + ".blacklistedRoles", Arrays.asList(new Long[] { -1L }));
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
            List<Long> blacklistedUsers = cfg.getLongList("command." + commandName + ".blacklistedUsers");
            List<Long> blacklistedRoles = cfg.getLongList("command." + commandName + ".blacklistedRoles");

            this.commandPermissions.add(new CommandPermission(CommandHandler.commands.get(commandName), commandName, needPermission, whitelistedRoles, whitelistedUsers, blacklistedUsers, blacklistedRoles));
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

    public void saveCommand(CommandPermission commandPermission) {
        FileConfiguration cfg = getConfig();

        cfg.set("command." + commandPermission.getCommandName() + ".needPermission", commandPermission.needPermission());
        cfg.set("command." + commandPermission.getCommandName() + ".whitelistedRoles", commandPermission.getWhitelistedRoles());
        cfg.set("command." + commandPermission.getCommandName() + ".whitelistedUsers", commandPermission.getWhitelistedUsers());
        cfg.set("command." + commandPermission.getCommandName() + ".blacklistedUsers", commandPermission.getBlacklistedUsers());
        cfg.set("command." + commandPermission.getCommandName() + ".blacklistedRoles", commandPermission.getBlacklistedRoles());

        saveConfig();
    }
}
