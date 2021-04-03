package net.wolfur.rasputin.role;

import net.dv8tion.jda.api.entities.Role;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RoleManager {

    private Map<String, Role> roles;

    public RoleManager() {
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `discord_roles` (id INT NOT NULL AUTO_INCREMENT, role_name VARCHAR(256), role_id LONG, UNIQUE KEY(id))");
        this.roles = new HashMap<>();
        this.loadRoles();
    }

    private void loadRoles() {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `discord_roles`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {
                String roleName = rs.getString("role_name");
                long roleId = rs.getLong("role_id");

                Role role = Main.getJDA().getRoleById(roleId);

                if(role == null) {
                    Logger.error("An error occurred while loading roles: Role does not exists.", true);
                    continue;
                }

                this.roles.put(roleName, role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Logger.info("Loaded " + this.roles.size() + " roles from database.", true);
    }

    public void reloadRoles() {
        this.roles.clear();
        this.loadRoles();
    }

    public Role getRole(String name) {
        if(!this.roles.containsKey(name)) return null;
        return this.roles.get(name);
    }

}
