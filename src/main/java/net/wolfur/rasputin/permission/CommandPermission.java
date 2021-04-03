package net.wolfur.rasputin.permission;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;

import java.util.List;

public class CommandPermission {

    private Command command;

    private String commandName;

    private boolean needPermission;

    private List<Long> whitelistedRoles;
    private List<Long> whitelistedUsers;

    private List<Long> blacklistedUsers;
    private List<Long> blacklistedRoles;

    public CommandPermission(Command command, String commandName, boolean needPermission, List<Long> whitelistedRoles, List<Long> whitelistedUsers, List<Long> blacklistedUsers, List<Long> blacklistedRoles) {
        this.command = command;
        this.commandName = commandName;
        this.needPermission = needPermission;
        this.whitelistedRoles = whitelistedRoles;
        this.whitelistedUsers = whitelistedUsers;
        this.blacklistedUsers = blacklistedUsers;
        this.blacklistedRoles = blacklistedRoles;
    }

    public Command getCommand() {
        return this.command;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public boolean needPermission() {
        return this.needPermission;
    }

    public List<Long> getWhitelistedRoles() {
        return this.whitelistedRoles;
    }

    public List<Long> getWhitelistedUsers() {
        return this.whitelistedUsers;
    }

    public List<Long> getBlacklistedUsers() {
        return this.blacklistedUsers;
    }

    public List<Long> getBlacklistedRoles() {
        return this.blacklistedRoles;
    }

    public boolean hasPermission(Member member) {
        if(this.getBlacklistedUsers().contains(member.getUser().getIdLong())) return false;

        for(Role role : member.getRoles()) {
            if(this.getBlacklistedRoles().contains(role.getIdLong())) return false;
        }

        if(!this.needPermission()) return true;
        if(this.getWhitelistedUsers().contains(member.getUser().getIdLong())) return true;

        for(Role role : member.getRoles()) {
            if(this.getWhitelistedRoles().contains(role.getIdLong())) return true;
        }
        return false;
    }

    public void addWhitelistedUser(long id) {
        if(this.whitelistedUsers.contains(id)) return;
        this.whitelistedUsers.add(id);
        Main.getFileManager().getPermissionFile().saveCommand(this);
    }

    public void removeWhitelistedUser(long id) {
        if(!this.whitelistedUsers.contains(id)) return;
        this.whitelistedUsers.remove(id);
        Main.getFileManager().getPermissionFile().saveCommand(this);
    }

    public void addWhitelistedRole(long id) {
        if(this.whitelistedRoles.contains(id)) return;
        this.whitelistedRoles.add(id);
        Main.getFileManager().getPermissionFile().saveCommand(this);
    }

    public void removeWhitelistedRole(long id) {
        if(!this.whitelistedRoles.contains(id)) return;
        this.whitelistedRoles.remove(id);
        Main.getFileManager().getPermissionFile().saveCommand(this);
    }

    public void addBlacklistedUser(long id) {
        if(this.blacklistedUsers.contains(id)) return;
        this.blacklistedUsers.add(id);
        Main.getFileManager().getPermissionFile().saveCommand(this);
    }

    public void removeBlacklistedUser(long id) {
        if(!this.blacklistedUsers.contains(id)) return;
        this.blacklistedUsers.remove(id);
        Main.getFileManager().getPermissionFile().saveCommand(this);
    }

    public void addBlacklistedRole(long id) {
        if(this.blacklistedRoles.contains(id)) return;
        this.blacklistedRoles.add(id);
        Main.getFileManager().getPermissionFile().saveCommand(this);
    }

    public void removeBlacklistedRole(long id) {
        if(!this.blacklistedRoles.contains(id)) return;
        this.blacklistedRoles.remove(id);
        Main.getFileManager().getPermissionFile().saveCommand(this);
    }

    public void setNeedPermission(boolean value) {
        this.needPermission = value;
        Main.getFileManager().getPermissionFile().saveCommand(this);
    }
}
