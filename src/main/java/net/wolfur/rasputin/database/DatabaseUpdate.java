package net.wolfur.rasputin.database;

import net.wolfur.rasputin.Main;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wolfur on 17.10.2017.
 */
public abstract class DatabaseUpdate {

    private boolean update;
    private boolean ready;
    private boolean forceUpdate;
    private List<ReadyExecutor> readyExecutorList;

    public DatabaseUpdate() {
        this.readyExecutorList = new ArrayList<>();
        this.forceUpdate = false;
    }

    public List<ReadyExecutor> getReadyExecutorList() {
        return this.readyExecutorList;
    }

    public void addReadyExecutor(ReadyExecutor executor) {
        if(this.ready) {
            executor.ready();
            return;
        }
        this.readyExecutorList.add(executor);
    }

    public boolean isUpdate() {
        return this.update;
    }

    public boolean isReady() {
        return this.ready;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
        if(ready) {
            for(ReadyExecutor executor : this.readyExecutorList) {
                executor.ready();
            }
            this.readyExecutorList.clear();
        }
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public boolean isForceUpdate() {
        return this.forceUpdate;
    }

    public void addToUpdater() {
        Main.getSQLManager().getUpdater().addToUpdater(this);
    }

    public void removeFromUpdater() {
        Main.getSQLManager().getUpdater().removeFromUpdater(this);
    }

    public abstract void saveData();

    public abstract void saveDataAsync();

    public abstract void loadData();

    public abstract void loadDataAsync();

    public static abstract interface ReadyExecutor {

        public abstract void ready();
    }

}
