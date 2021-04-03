package net.wolfur.rasputin.weapon;

import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WeaponManager {

    private List<DestinyWeaponData> destinyWeaponData;

    public WeaponManager() {
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `destiny_weapons` (id INT NOT NULL AUTO_INCREMENT, name TEXT, item_hash LONG, god_roll TEXT, alternative_roll TEXT, game_type VARCHAR(16), masterwork LONG, alternative_masterwork LONG, UNIQUE KEY(id))");
        this.destinyWeaponData = new ArrayList<>();
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

                String gameTypeName = rs.getString("game_type");
                long masterwork = rs.getLong("masterwork");
                long alternativeMasterwork = rs.getLong("alternative_masterwork");

                String godRollText = rs.getString("god_roll");
                String alternativeRollText = rs.getString("alternative_roll");

                for (String godRollStr : godRollText.split(";")) {
                    try {
                        long hash = Long.parseLong(godRollStr);
                        godRoll.add(hash);
                    } catch (NumberFormatException e) {
                        Logger.error("An error occurred while parsing trait hash to long.", true);
                        continue;
                    }
                }

                for (String alternativeRollStr : alternativeRollText.split(";")) {
                    try {
                        long hash = Long.parseLong(alternativeRollStr);
                        alternativeRoll.add(hash);
                    } catch (NumberFormatException e) {
                        Logger.error("An error occurred while parsing trait hash to long.", true);
                        continue;
                    }
                }

                WeaponGameType weaponGameType = WeaponGameType.getByName(gameTypeName);
                if(weaponGameType == null) {
                    Logger.error("An error occurred while parsing game type to object 'WeaponGameType'.", true);
                    continue;
                }

                this.destinyWeaponData.add(new DestinyWeaponData(name, itemHash, weaponGameType, masterwork, alternativeMasterwork, godRoll, alternativeRoll));
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

    public List<DestinyWeaponData> getDestinyWeaponData() {
        return this.destinyWeaponData;
    }

    public DestinyWeaponData getDestinyWeaponData(String name, WeaponGameType weaponGameType) {
        for(DestinyWeaponData destinyWeaponData : this.destinyWeaponData) {
            if((destinyWeaponData.getName().equalsIgnoreCase(name)) && (destinyWeaponData.getWeaponGameType().equals(weaponGameType))) {
                return destinyWeaponData;
            }
        }
        return null;
    }
}
