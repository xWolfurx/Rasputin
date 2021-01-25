package net.wolfur.rasputin.task;

import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReloadTask implements Runnable {

    private ScheduledExecutorService service;
    private boolean reloading;

    public ReloadTask() {
        this.service = Executors.newScheduledThreadPool(1);
        this.reloading = false;
    }

    public void start() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
        ZonedDateTime nextRun = now.withHour(Main.getFileManager().getConfigFile().getReloadHour()).withMinute(0).withSecond(0);

        if(now.compareTo(nextRun) > 0) nextRun = nextRun.plusDays(1);

        Duration duration = Duration.between(now, nextRun);
        long initialDelay = duration.getSeconds();

        this.service.scheduleAtFixedRate(this, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        Logger.info("Next reloading in " + TimeUtil.timeToString(duration.toMillis(), true) + ".", true);
    }

    public void stop() {
        if(!isCurrentlyRunning()) return;
        this.service.shutdown();
        this.service = null;
    }

    public boolean isCurrentlyRunning() {
        return this.service != null;
    }

    public boolean isReloading() {
        return this.reloading;
    }

    @Override
    public void run() {
        this.reloading = true;
        Logger.warning("Reloading configuration... Please wait...", true);
        Logger.warning("Some functions are restricted.", true);

        Main.getCoreManager().getRaidManager().resetRaidManager();

        Logger.info("Reloading completed.", true);
        Logger.info("All functions reactivated.", true);
        this.reloading = false;
    }
}
