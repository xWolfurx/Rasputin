package net.wolfur.rasputin.weapon;

import net.dv8tion.jda.api.entities.Icon;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponManager {

    private Map<String, DestinyWeaponData> destinyWeaponData;

    public WeaponManager() {
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `destiny_weapons` (id INT NOT NULL AUTO_INCREMENT, name TEXT, item_hash LONG, god_roll TEXT, alternative_roll TEXT, UNIQUE KEY(id))");
        this.destinyWeaponData = new HashMap<>();
        this.loadWeapons();
    }

    private void loadWeapons() {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `destiny_weapons`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while (rs.next()) {
                String name = rs.getString("name");
                long itemHash = rs.getLong("item_hash");

                List<Long> godRoll = new ArrayList<>();
                List<Long> alternativeRoll = new ArrayList<>();

                String godRollText = rs.getString("god_roll");
                String alternativeRollText = rs.getString("alternative_roll");

                for (String godRollStr : godRollText.split(";")) {
                    try {
                        long hash = Long.parseLong(godRollStr);
                        godRoll.add(hash);
                    } catch (NumberFormatException e) {
                        Logger.error("An error occurred wile parsing trait hash to long.", true);
                        break;
                    }
                }

                for (String alternativeRollStr : alternativeRollText.split(";")) {
                    try {
                        long hash = Long.parseLong(alternativeRollStr);
                        alternativeRoll.add(hash);
                    } catch (NumberFormatException e) {
                        Logger.error("An error occurred wile parsing trait hash to long.", true);
                        break;
                    }
                }


                this.destinyWeaponData.put(name.toLowerCase(), new DestinyWeaponData(name, itemHash, godRoll, alternativeRoll));
            }
            Logger.info("Loaded " + this.destinyWeaponData.size() + " weapons from database.", false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reloadWeapons() {
        this.destinyWeaponData.clear();
        this.loadWeapons();
    }

    public Map<String, DestinyWeaponData> getDestinyWeaponData() {
        return this.destinyWeaponData;
    }

    public DestinyWeaponData getDestinyWeaponData(String name) {
        if(this.destinyWeaponData.containsKey(name)) return this.destinyWeaponData.get(name);
        return null;
    }
}
