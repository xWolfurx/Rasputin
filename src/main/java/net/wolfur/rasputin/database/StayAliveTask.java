package net.wolfur.rasputin.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StayAliveTask extends Thread {

    private SQLManager sqlManager;
    private boolean active;

    public StayAliveTask(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
        this.active = true;
    }

    public SQLManager getSQLManager() {
        return this.sqlManager;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void run() {
        while(this.active) {
            try {
                Connection connection = this.sqlManager.getConnection();
                PreparedStatement st = connection.prepareStatement("/* ping */ SELECT 1");
                st.executeQuery();
                Thread.sleep(300000L);
            } catch (InterruptedException | SQLException e) {
                e.printStackTrace();
                setActive(false);
            }
        }
    }
}
