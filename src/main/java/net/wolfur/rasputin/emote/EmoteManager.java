package net.wolfur.rasputin.emote;

import net.dv8tion.jda.api.entities.Emote;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EmoteManager {

    private Map<Long, Emote> emotes;
    private Map<String, Emote> customEmotes;

    public EmoteManager() {
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `destiny_emote_definition` (id INT NOT NULL AUTO_INCREMENT, hash_value LONG, emote_id LONG, description TEXT, UNIQUE KEY(id))");
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `custom_emote_definition` (id INT NOT NULL AUTO_INCREMENT, key_value TEXT, emote_id LONG, description TEXT, UNIQUE KEY(id))");

        this.emotes = new HashMap<>();
        this.customEmotes = new HashMap<>();

        this.loadEmotes();
        this.loadCustomEmotes();
    }

    private void loadEmotes() {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `destiny_emote_definition`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {
                long hashValue = rs.getLong("hash_value");
                long emoteId = rs.getLong("emote_id");

                Emote emote = Main.getJDA().getEmoteById(emoteId);

                if(emote == null) {
                    Logger.error("An error occurred while collecting emotes from database.", true);
                    continue;
                }

                this.emotes.put(hashValue, emote);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Logger.info("Loaded " + this.emotes.size() + " emote" + (this.emotes.size() == 1 ? "" : "s") + " from database.", true);
    }

    private void loadCustomEmotes() {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `custom_emote_definition`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {
                String key = rs.getString("key_value");
                long emoteId = rs.getLong("emote_id");

                Emote emote = Main.getJDA().getEmoteById(emoteId);

                if(emote == null) {
                    Logger.error("An error occurred while collecting emotes from database.", true);
                    continue;
                }

                this.customEmotes.put(key.toLowerCase(), emote);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Logger.info("Loaded " + this.customEmotes.size() + " custom emote" + (this.customEmotes.size() == 1 ? "" : "s") + " from database.", true);
    }

    public void reloadEmotes() {
        this.emotes.clear();
        this.customEmotes.clear();

        this.loadEmotes();
        this.loadCustomEmotes();
    }

    public Emote getEmote(long hashValue) {
        if(!this.emotes.containsKey(hashValue)) {
            return this.emotes.get(-1L);
        }
        return this.emotes.get(hashValue);
    }

    public Emote getCustomEmote(String key) {
        if(!this.customEmotes.containsKey(key.toLowerCase())) {
            return this.customEmotes.get("-1");
        }
        return this.customEmotes.get(key.toLowerCase());
    }
}
