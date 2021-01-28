package net.wolfur.rasputin.permission;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.wolfur.rasputin.core.Command;

import java.util.List;

public class CommandPermission {

    private Command command;

    private boolean needPermission;

    private List<Long> whitelistedRoles;
    private List<Long> whitelistedUsers;

    public CommandPermission(Command command, boolean needPermission, List<Long> whitelistedRoles, List<Long> whitelistedUsers) {
        this.command = command;
        this.needPermission = needPermission;
        this.whitelistedRoles = whitelistedRoles;
        this.whitelistedUsers = whitelistedUsers;
    }

    public Command getCommand() {
        return this.command;
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

    public boolean hasPermission(Member member) {
        if(!this.needPermission()) return true;
        if(this.getWhitelistedUsers().contains(member.getUser().getIdLong())) return true;
        for(Role role : member.getRoles()) {
            if(this.getWhitelistedRoles().contains(role.getIdLong())) {
                return true;
            }
        }
        return false;
    }
}
