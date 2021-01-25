package net.wolfur.rasputin.manager;

import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.punish.BanInformation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BanManager {

    public BanManager() {
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `player_bans` (id INT NOT NULL AUTO_INCREMENT, user_id VARCHAR(255), permanent BOOLEAN, timestamp BIGINT, ban_time BIGINT NULL, banned_by VARCHAR(255), reason TEXT, UNIQUE KEY(id))");
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `player_bans_archive` (id INT NOT NULL AUTO_INCREMENT, user_id VARCHAR(255), permanent BOOLEAN, timestamp BIGINT, ban_time BIGINT NULL, banned_by VARCHAR(255), reason TEXT, UNIQUE KEY(id))");
    }

    private List<BanInformation> getActiveBans() {
        List<BanInformation> activeBans = new ArrayList<>();
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `player_bans`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {

                String userId = rs.getString("user_id");
                boolean permanent = rs.getBoolean("permanent");
                long timestamp = rs.getLong("timestamp");
                String bannedBy = rs.getString("banned_by");
                String reason = rs.getString("reason");

                BanInformation banInformation = null;
                if(permanent) {
                    banInformation = new BanInformation(userId, timestamp, bannedBy,reason);
                } else {
                    long banTime = rs.getLong("ban_time");
                    banInformation = new BanInformation(userId, timestamp, bannedBy, new BanInformation.TimeBanInformation(banTime), reason);
                }

                activeBans.add(banInformation);
            }

            Main.getSQLManager().close(st, rs);
            return activeBans;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getActiveBansAsync(Callback<List<BanInformation>> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(BanManager.this.getActiveBans());
            }
        });
    }

    public boolean isPlayerBanned(String userId) {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `player_bans` WHERE `user_id` = ?");
            st.setString(1, userId);
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            boolean banned = rs.next();

            Main.getSQLManager().close(st, rs);
            return banned;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void isPlayerBannedAsync(final String userId, final Callback<Boolean> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(BanManager.this.isPlayerBanned(userId));
            }
        });
    }

    public BanInformation getBanInformation(String userId) {
        if(!isPlayerBanned(userId)) return null;

        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `player_bans` WHERE `user_id` = ?");
            st.setString(1, userId);
            ResultSet rs = Main.getSQLManager().executeQuery(st);

            if(!rs.next()) return null;

            boolean permanent = rs.getBoolean("permanent");
            long timestamp = rs.getLong("timestamp");
            String bannedBy = rs.getString("banned_by");
            String reason = rs.getString("reason");

            BanInformation banInformation = null;
            if(permanent) {
                banInformation = new BanInformation(userId, timestamp, bannedBy,reason);
            } else {
                long banTime = rs.getLong("ban_time");
                banInformation = new BanInformation(userId, timestamp, bannedBy, new BanInformation.TimeBanInformation(banTime), reason);
            }

            Main.getSQLManager().close(st, rs);
            return banInformation;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getBanInformationAsync(final String userId, final Callback<BanInformation> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(BanManager.this.getBanInformation(userId));
            }
        });
    }

    public BanInformation.ArchiveInformation getArchiveInformation(String userId) {
        List<BanInformation> archiveBans = new ArrayList<>();
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `player_bans_archive` WHERE `user_id` = ? ");
            st.setString(1, userId);
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {
                boolean permanent = rs.getBoolean("permanent");
                long timestamp = rs.getLong("timestamp");
                String bannedBy = rs.getString("banned_by");
                String reason = rs.getString("reason");

                BanInformation banInformation = null;
                if(permanent) {
                    banInformation = new BanInformation(userId, timestamp, bannedBy, reason);
                } else {
                    long banTime = rs.getLong("ban_time");
                    banInformation = new BanInformation(userId, timestamp, bannedBy, new BanInformation.TimeBanInformation(banTime), reason);
                }
                if(banInformation != null) {
                    archiveBans.add(banInformation);
                }
            }
            Main.getSQLManager().close(st, rs);
            BanInformation.ArchiveInformation archiveInformation = new BanInformation.ArchiveInformation(userId, archiveBans);
            return archiveInformation;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getArchiveInformationAsync(final String userId, final Callback<BanInformation.ArchiveInformation> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(BanManager.this.getArchiveInformation(userId));
            }
        });
    }

    public boolean setBanned(String userId, boolean banned, BanInformation banInformation) {
        if(banned) {
            if(isPlayerBanned(userId)) return false;

            if(!banInformation.isPermanent()) {
                try {
                    PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("INSERT INTO `player_bans` (`user_id`, `permanent`, `timestamp`, `ban_time`, `banned_by`, `reason`) VALUES (?, ?, ?, ?, ?, ?)");
                    st.setString(1, userId);
                    st.setBoolean(2, false);
                    st.setLong(3, banInformation.getTimestamp());
                    st.setLong(4, banInformation.getTimeBanInformation().getBanTime());
                    st.setString(5, banInformation.getBannedBy());
                    st.setString(6, banInformation.getReason());
                    Main.getSQLManager().executeUpdate(st);
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            try {
                PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("INSERT INTO `player_bans` (`user_id`, `permanent`, `timestamp`, `banned_by`, `reason`) VALUES (?, ?, ?, ?, ?)");
                st.setString(1, userId);
                st.setBoolean(2, true);
                st.setLong(3, banInformation.getTimestamp());
                st.setString(4, banInformation.getBannedBy());
                st.setString(5, banInformation.getReason());
                Main.getSQLManager().executeUpdate(st);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        if(!isPlayerBanned(userId)) return false;

        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("DELETE FROM `player_bans` WHERE `user_id` = ?");
            st.setString(1, userId);
            Main.getSQLManager().executeUpdate(st);
            addToArchive(userId, banInformation);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setBannedAsync(final String userId, final boolean banned, final BanInformation banInformation, final Callback<Boolean> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(BanManager.this.setBanned(userId, banned, banInformation));
            }
        });
    }

    public boolean addToArchive(String userId, BanInformation banInformation) {
        try {
            if(!banInformation.isPermanent()) {
                PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("INSERT INTO `player_bans_archive` (`user_id`, `permanent`, `timestamp`, `ban_time`, `banned_by`, `reason`) VALUES (?, ?, ?, ?, ?, ?)");
                st.setString(1, userId);
                st.setBoolean(2, false);
                st.setLong(3, banInformation.getTimestamp());
                st.setLong(4, banInformation.getTimeBanInformation().getBanTime());
                st.setString(5, banInformation.getBannedBy());
                st.setString(6, banInformation.getReason());
                Main.getSQLManager().executeUpdate(st);
                return true;
            }

            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("INSERT INTO `player_bans_archive` (`user_id`, `permanent`, `timestamp`, `banned_by`, `reason`) VALUES (?, ?, ?, ?, ?)");
            st.setString(1, userId);
            st.setBoolean(2, true);
            st.setLong(3, banInformation.getTimestamp());
            st.setString(4, banInformation.getBannedBy());
            st.setString(5, banInformation.getReason());
            Main.getSQLManager().executeUpdate(st);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addToArchiveAsync(final String userId, final BanInformation banInformation, final Callback<Boolean> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(BanManager.this.addToArchive(userId, banInformation));
            }
        });
    }

    public boolean clearArchive(String userId) {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("DELETE FROM `player_bans_archive` WHERE `user_id` = ?");
            st.setString(1, userId);
            Main.getSQLManager().executeUpdate(st);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void clearArchiveAsync(final String userId, final Callback<Boolean> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(BanManager.this.clearArchive(userId));
            }
        });
    }
}
