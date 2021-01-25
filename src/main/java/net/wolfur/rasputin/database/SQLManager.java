package net.wolfur.rasputin.database;

import net.wolfur.rasputin.util.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SQLManager {

    private String host;
    private String port;
    private String username;
    private String password;
    private String database;
    private Connection connection;
    private final StayAliveTask stayAliveTask;
    private final AsyncHandler asyncHandler;
    private final Updater updater;

    public SQLManager(String host, String port, String username, String password, String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.stayAliveTask = new StayAliveTask(this);
        this.asyncHandler = new AsyncHandler();
        this.updater = new Updater();
    }

    public boolean openConnection() {
        try {
            if((this.connection != null) && (!this.connection.isClosed())) {
                return false;
            }
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            this.stayAliveTask.setActive(true);
            this.stayAliveTask.start();
            this.updater.setActive(true);
            this.updater.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Verbindung zum MySQL-Server konnte nicht hergestellt werden: " + e.getMessage(), true);
        }
        return false;
    }

    public Connection getConnection() {
        try {
            if((this.connection == null) || (this.connection.isClosed())) {
                this.openConnection();
            }
        } catch (Exception e) {

        }
        return this.connection;
    }

    public void closeConnection() {
        try {
            if((this.connection != null) || (!this.connection.isClosed())) {
                this.connection.close();
                this.connection = null;
                this.stayAliveTask.setActive(false);
            }
        } catch (Exception e) {
            Logger.error("Verbindung konnte nicht geschlossen werden: " + e.getMessage(), true);
        }
    }

    public void close(PreparedStatement st, ResultSet rs) {
        try {
            if(st != null) {
                st.close();
            }
            if(rs != null) {
                rs.close();
            }
        } catch (Exception e) {

        }
    }

    public void executeUpdate(String statement) {
        try {
            PreparedStatement st = this.connection.prepareStatement(statement);
            st.executeUpdate();
            close(st, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeUpdate(PreparedStatement statement) {
        try {
            statement.executeUpdate();
            close(statement, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String statement) {
        try {
            PreparedStatement st = this.connection.prepareStatement(statement);
            return st.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet executeQuery(PreparedStatement statement) {
        try {
            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AsyncHandler getAsyncHandler() {
        return this.asyncHandler;
    }

    public StayAliveTask getStayAliveTask() {
        return this.stayAliveTask;
    }

    public Updater getUpdater() {
        return this.updater;
    }
}
