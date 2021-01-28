package net.wolfur.rasputin.experience;

import net.dv8tion.jda.api.entities.User;
import net.wolfur.rasputin.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ExperienceManager {

    private Map<User, ExperienceUser> experienceUsers;

    public ExperienceManager() {
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `player_experience` (id INT NOT NULL AUTO_INCREMENT, user_id LONG, level INT, experience BIGINT, UNIQUE KEY(id))");
        this.experienceUsers = new HashMap<>();
        this.initialize();
    }

    private void initialize() {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `player_experience`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {
                Main.getJDA().retrieveUserById(rs.getLong("user_id")).queue(user -> {
                    this.addUserToCache(user);
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUserToCache(User user) {
        if(this.experienceUsers.containsKey(user)) return;
        ExperienceUser experienceUser = new ExperienceUser(user);
        this.experienceUsers.put(user, experienceUser);
    }

    public void removeUserFromCache(User user) {
        if(!this.experienceUsers.containsKey(user)) return;
        ExperienceUser experienceUser = this.experienceUsers.get(user);
        experienceUser.saveDataAsync();
        experienceUser.removeFromUpdater();
        this.experienceUsers.remove(user);
    }

    public ExperienceUser getExperienceUser(User user) {
        if(!this.experienceUsers.containsKey(user)) this.addUserToCache(user);
        return this.experienceUsers.get(user);
    }

    public Map<User, ExperienceUser> getExperienceUsers() {
        return this.experienceUsers;
    }


}
