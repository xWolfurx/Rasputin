package net.wolfur.rasputin.bungie;

import net.dv8tion.jda.api.entities.User;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BungieUserManager {

    private Map<User, BungieUser> bungieUsers;

    public BungieUserManager() {
        this.bungieUsers = new HashMap<>();
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `bungie_users` (id INT NOT NULL AUTO_INCREMENT, discord_name VARCHAR(100), discord_id LONG, bungie_membership_id LONG, destiny_membership_id LONG, destiny_membership_type TEXT, access_token TEXT, expires LONG, refresh_token LONG, UNIQUE KEY(id))");
        this.initialize();
    }

    private void initialize() {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `bungie_users`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {
                Main.getJDA().retrieveUserById(rs.getLong("discord_id")).queue(user -> {
                    this.addUserToCache(user);
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUserToCache(User user) {
        if(this.bungieUsers.containsKey(user)) return;
        BungieUser bungieUser = new BungieUser(user);
        this.bungieUsers.put(user, bungieUser);
    }

    public void removeUserFromCache(User user) {
        if(!this.bungieUsers.containsKey(user)) return;
        BungieUser bungieUser = this.bungieUsers.get(user);
        bungieUser.saveDataAsync();
        bungieUser.removeFromUpdater();
        this.bungieUsers.remove(user);
    }

    public BungieUser getBungieUser(User user) {
        if(!this.bungieUsers.containsKey(user)) this.addUserToCache(user);
        return this.bungieUsers.get(user);
    }

    public Map<User, BungieUser> getBungieUsers() {
        return this.bungieUsers;
    }

    public BungieUser getBungieUserFromSecurityToken(String securityToken) {
        for(BungieUser bungieUser : this.bungieUsers.values()) {
            if(bungieUser.getSecurityToken() == null) continue;
            if(bungieUser.getSecurityToken().equalsIgnoreCase(securityToken)) {
                return bungieUser;
            }
        }
        return null;
    }

    public void checkUsers() {
        for(BungieUser bungieUsers : this.bungieUsers.values()) {
            bungieUsers.startRefreshTimer();
        }
    }

}
