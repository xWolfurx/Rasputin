package net.wolfur.rasputin.manager;

import net.dv8tion.jda.api.entities.Activity;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.database.Callback;
import net.wolfur.rasputin.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StatusManager {

    private List<String> statusMessages;

    private Timer timer;
    private Random random;

    public StatusManager() {
        Main.getSQLManager().executeUpdate("CREATE TABLE IF NOT EXISTS `rasputin_status` (id INT NOT NULL AUTO_INCREMENT, message TEXT, UNIQUE KEY(id))");
        this.statusMessages = new ArrayList<>();
        this.random = new Random();

        this.loadStatusMessagesAsync(new Callback<Boolean>() {
            @Override
            public void accept(Boolean success) {
                if(success.booleanValue()) {
                    StatusManager.this.startTimer();
                }
            }
        });
    }

    private boolean loadStatusMessages() {
        this.statusMessages.clear();
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `rasputin_status`");
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            while(rs.next()) {
                String message = rs.getString("message");
                this.statusMessages.add(message);
            }
            Logger.info("Loaded " + this.statusMessages.size() + " status messages.", true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void loadStatusMessagesAsync(Callback<Boolean> callback) {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.accept(StatusManager.this.loadStatusMessages());
            }
        });
    }

    public List<String> getStatusMessages() {
        return this.statusMessages;
    }

    public boolean isCurrentlyRunning() {
        return this.timer != null;
    }

    public void stopTimer() {
        if(!isCurrentlyRunning()) return;
        this.timer.cancel();
        this.timer = null;
    }

    public void startTimer() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(StatusManager.this.statusMessages.isEmpty()) return;

                String message = StatusManager.this.statusMessages.get(StatusManager.this.random.nextInt(StatusManager.this.statusMessages.size()));
                Main.getJDA().getPresence().setActivity(Activity.playing(message));
            }
        };

        this.timer = new Timer("status-task");
        this.timer.schedule(timerTask, 10000L, 60000L);
    }
}
