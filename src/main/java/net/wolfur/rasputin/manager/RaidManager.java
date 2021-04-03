package net.wolfur.rasputin.manager;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.other.Raid;
import net.wolfur.rasputin.other.RaidType;
import net.wolfur.rasputin.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RaidManager {

    private List<RaidType> raidTypes;
    private Map<String, Raid> raids;

    private Map<User, Raid> changeTime;

    public RaidManager() {
        this.raidTypes = new ArrayList<>();
        this.raids = new HashMap<>();
        this.changeTime = new HashMap<>();

        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `raid_types` (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100), better_name VARCHAR(255), shortcuts VARCHAR(10), max_players INT, icon_url TEXT, active BOOLEAN, activity_hash TEXT, UNIQUE KEY(id))");
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `raids` (id VARCHAR(255), channel_id VARCHAR(255), type VARCHAR(100), date VARCHAR(255), time VARCHAR(255), leader TEXT, creator TEXT, runners TEXT, alternatives TEXT, description TEXT, extension INT, timestamp LONG, UNIQUE KEY(id))");

        this.loadRaidTypes();
        new Timer("loading-raids").schedule(new TimerTask() {
            @Override
            public void run() {
                RaidManager.this.loadRaids();
            }
        }, 25000L);
    }

    public void resetRaidManager() {
        this.raidTypes.clear();

        this.raids.values().forEach(raid -> raid.stopTasks());
        this.raids.clear();

        this.loadRaidTypes();
        this.loadRaids();
    }

    public void loadRaidTypes() {
        this.raidTypes.clear();
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `raid_types`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {
                if(rs.getBoolean("active")) {
                    this.raidTypes.add(new RaidType(rs.getString("name"), rs.getString("better_name"), rs.getString("shortcuts"), rs.getInt("max_players"), rs.getString("icon_url"), rs.getString("activity_hash"), rs.getLong("channel_id")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Logger.info("Loaded " + this.raidTypes.size() + " raid types.", true);
    }

    public void loadRaids() {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `raids`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {
                String id = rs.getString("id");
                String channelId = rs.getString("channel_id");
                if(Main.getJDA().getTextChannelById(channelId) != null) {
                    List<Message> messages = Main.getJDA().getTextChannelById(channelId).getHistory().retrievePast(30).complete();
                        if(this.getMessage(messages, id) != null) {
                        try {
                            Date date = new SimpleDateFormat("dd.MM.yyyy").parse(rs.getString("date"));
                            Date time = new SimpleDateFormat("HH:mm").parse(rs.getString("time"));
                            Date completeDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(new SimpleDateFormat("dd.MM.yyyy").format(date) + " " + new SimpleDateFormat("HH:mm").format(time));
                            if (completeDate.after(new Date())) {
                                List<User> runners = new ArrayList<>();
                                List<User> alternatives = new ArrayList<>();

                                String runnersArray = rs.getString("runners");
                                String alternativesArray = rs.getString("alternatives");

                                if (!runnersArray.equalsIgnoreCase("none")) {
                                    for (String user : runnersArray.split(";")) {
                                        runners.add(Main.getJDA().retrieveUserById(user).complete());
                                    }
                                }
                                if (!alternativesArray.equalsIgnoreCase("none")) {
                                    for (String user : alternativesArray.split(";")) {
                                        alternatives.add(Main.getJDA().retrieveUserById(user).complete());
                                    }
                                }

                                String strLeader = rs.getString("leader");
                                User leader;

                                if(!strLeader.equalsIgnoreCase("none")) {
                                    leader = Main.getJDA().retrieveUserById(strLeader).complete();
                                } else {
                                    leader = null;
                                }

                                String raidType = rs.getString("type");

                                Raid raid = new Raid(id, channelId, Main.getCoreManager().getRaidManager().getRaidType(raidType), date, time, completeDate, Main.getJDA().retrieveUserById(rs.getString("creator")).complete(), leader, runners, alternatives, rs.getString("description"), rs.getInt("extension"), rs.getLong("timestamp"));
                                this.raids.put(id, raid);

                                Logger.info("Loaded raid '" + raidType + "' from database.", true);
                            } else {
                                getMessage(messages, id).delete().queue();
                                this.deleteRaidFromDatabase(id);
                                Logger.warning("Deleted raid '" + id + "' -> Raid is in the past.", true);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        this.deleteRaidFromDatabase(id);
                        Logger.warning("Deleted raid '" + id + "' -> Can´t find message", true);
                    }
                } else {
                    this.deleteRaidFromDatabase(id);
                    Logger.warning("Deleted raid '" + id + "' -> Can´t find channel", true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void createRaid(MessageChannel channel, RaidType raidType, Date date, Date time, User creator, String description) {
        Raid raid = new Raid(raidType, date, time, creator, description);
        String id = raid.sendMessage(channel);
        this.raids.put(id, raid);
        Logger.info("Created new activity: " + raidType.getBetterName(), true);

        this.addRaidToDatabaseAsync(raid, channel, new Callback<Boolean>() {
            @Override
            public void accept(Boolean success) {
                if(!success) {
                    Logger.error("An error occurred while uploading raid to database.", true);
                }
            }
        });
    }

    public void deleteRaid(String id, String reason) {
        if(!this.raids.containsKey(id)) return;

        Raid raid = this.raids.get(id);

        if(raid.getMessage() != null) raid.getMessage().delete().queue();
        raid.stopTasks();

        this.raids.remove(id);
        Logger.info("Activity '" + raid.getRaidType().getName() + "' deleted. (" + reason + ")", true);

        if(Main.getFileManager().getChannelFile().getChannel("raid_history") != null) {
            TextChannel raidHistoryChannel = Main.getJDA().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("raid_history").getChannelId());
            raidHistoryChannel.sendMessage(raid.getHistoryEmbedBuilder(reason).build()).queue();
        } else {
            Logger.warning("Raid history channel is not configured yet.", true);
        }

        this.deleteRaidFromDatabaseAsync(id, new Callback<Boolean>() {
            @Override
            public void accept(Boolean success) {
                if(!success) {
                    Logger.error("An error occurred while removing raid from database.", true);
                }
            }
        });
    }

    public Raid getRaid(String id) {
        if(this.raids.containsKey(id)) {
            return this.raids.get(id);
        }
        return null;
    }

    public RaidType getRaidType(String name) {
        for(RaidType types : this.raidTypes) {
            List<String> shortcuts = types.getShortcutsAsList();
            if((types.getName().equalsIgnoreCase(name)) || (shortcuts.contains(name.toLowerCase()))) {
                return types;
            }
        }
        return null;
    }

    //id VARCHAR(255), channel_id VARCHAR(255), type VARCHAR(100), date VARCHAR(255), time VARCHAR(255), leader TEXT, creator TEXT, runners TEXT, alternatives TEXT, description TEXT, extension INT
    public boolean addRaidToDatabase(Raid raid, MessageChannel messageChannel) {
        StringBuilder runners = new StringBuilder();
        StringBuilder alternatives = new StringBuilder();
        for(User user : raid.getRunners()) {
            runners.append(user.getId()).append(";");
        }
        for(User user : raid.getAlternatives()) {
            alternatives.append(user.getId()).append(";");
        }

        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("INSERT INTO `raids` (id, channel_id, type, date, time, leader, creator, runners, alternatives, description, extension, `timestamp`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            st.setString(1, raid.getMessage().getId());
            st.setString(2, messageChannel.getId());
            st.setString(3, raid.getRaidType().getName());
            st.setString(4, new SimpleDateFormat("dd.MM.yyyy").format(raid.getDate()));
            st.setString(5, new SimpleDateFormat("HH:mm").format(raid.getTime()));
            st.setString(6, raid.getCreator().getId());
            st.setString(7, raid.getLeader().getId());
            st.setString(8, runners.length() > 0 ? runners.substring(0, runners.length() - 1) : "none");
            st.setString(9, alternatives.length() > 0 ? alternatives.substring(0, alternatives.length() - 1) : "none");
            st.setString(10, raid.getDescription());
            st.setInt(11, raid.getExtension());
            st.setLong(12, raid.getTimestamp());
            Main.getSQLManager().executeUpdate(st);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addRaidToDatabaseAsync(final Raid raid, final MessageChannel messageChannel, final Callback<Boolean> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(RaidManager.this.addRaidToDatabase(raid, messageChannel));
            }
        });
    }

    public boolean updateRaidInDatabase(Raid raid) {
        StringBuilder runners = new StringBuilder();
        StringBuilder alternatives = new StringBuilder();
        for(User user : raid.getRunners()) {
            runners.append(user.getId()).append(";");
        }
        for(User user : raid.getAlternatives()) {
            alternatives.append(user.getId()).append(";");
        }

        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("UPDATE `raids` SET `runners` = ?, `alternatives` = ?, `leader` = ?, `extension` = ?, `time` = ? WHERE `id` = ?");
            st.setString(1, runners.length() > 0 ? runners.substring(0, runners.length() - 1) : "none");
            st.setString(2, alternatives.length() > 0 ? alternatives.substring(0, alternatives.length() - 1) : "none");
            st.setString(3, (raid.getLeader() != null ? raid.getLeader().getId() : "none"));
            st.setInt(4, raid.getExtension());
            st.setString(5, new SimpleDateFormat("HH:mm").format(raid.getTime()));
            st.setString(6, raid.getId());
            Main.getSQLManager().executeUpdate(st);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateRaidInDatabaseAsync(final Raid raid, final Callback<Boolean> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(RaidManager.this.updateRaidInDatabase(raid));
            }
        });
    }

    public boolean deleteRaidFromDatabase(String id) {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("DELETE FROM `raids` WHERE `id` = ?");
            st.setString(1, id);
            Main.getSQLManager().executeUpdate(st);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteRaidFromDatabaseAsync(final String id, final Callback<Boolean> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(RaidManager.this.deleteRaidFromDatabase(id));
            }
        });
    }

    private Message getMessage(List<Message> messages, String id) {
        for(Message message : messages) {
            if(message.getId().equalsIgnoreCase(id)) {
                return message;
            }
        }
        return null;
    }

    public Map<User, Raid> getChangeTime() {
        return this.changeTime;
    }

    public Map<String, Raid> getRaids() {
        return this.raids;
    }
}
