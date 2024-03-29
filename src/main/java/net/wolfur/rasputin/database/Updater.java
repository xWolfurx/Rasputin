package net.wolfur.rasputin.database;

import java.util.ArrayList;
import java.util.List;

public class Updater extends Thread {

    private boolean active;
    private List<DatabaseUpdate> updateList = new ArrayList<>();

    public void addToUpdater(DatabaseUpdate databaseUpdate) {
        if(this.updateList.contains(databaseUpdate)) {
            return;
        }
        this.updateList.add(databaseUpdate);
    }

    public void removeFromUpdater(DatabaseUpdate databaseUpdate) {
        if(!this.updateList.contains(databaseUpdate)) {
            return;
        }
        this.updateList.remove(databaseUpdate);
    }

    public boolean containsUpdate(DatabaseUpdate databaseUpdate) {
        return this.updateList.contains(databaseUpdate);
    }

    public List<DatabaseUpdate> getUpdateList() {
        return this.updateList;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void saveAll() {
        for(DatabaseUpdate databaseUpdate : this.updateList) {
            if((databaseUpdate.isForceUpdate()) || (databaseUpdate.isUpdate())) {
                databaseUpdate.saveData();
                databaseUpdate.setUpdate(false);
            }
        }
    }

    public void run() {
        while(this.active) {
            this.saveAll();
            try {
                Thread.sleep(30000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
