package net.wolfur.rasputin.punish;

import java.util.List;

public class BanInformation {

    private String userId;
    private long timestamp;
    private String bannedBy;
    private TimeBanInformation timeBanInformation;
    private String reason;

    public BanInformation(String userId, long timestamp, String bannedBy, TimeBanInformation timeBanInformation, String reason) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.bannedBy = bannedBy;
        this.timeBanInformation = timeBanInformation;
        this.reason = reason;
    }

    public BanInformation(String userId, long timestamp, String bannedBy, String reason) {
        this(userId, timestamp, bannedBy, null, reason);
    }

    public String getUserId() {
        return this.userId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getBannedBy() {
        return this.bannedBy;
    }

    public boolean isPermanent() {
        return (this.timeBanInformation == null);
    }

    public TimeBanInformation getTimeBanInformation() {
        return this.timeBanInformation;
    }

    public String getReason() {
        return this.reason;
    }

    public static class TimeBanInformation {

        private long banTime;

        public TimeBanInformation(long banTime) {
            this.banTime = banTime;
        }

        public long getBanTime() {
            return this.banTime;
        }

    }

    public static class ArchiveInformation {

        private String userId;
        private List<BanInformation> archiveBans;

        public ArchiveInformation(String userId, List<BanInformation> archiveBans) {
            this.userId = userId;
            this.archiveBans = archiveBans;
        }

        public String getUserId() {
            return this.userId;
        }

        public List<BanInformation> getArchiveBans() {
            return this.archiveBans;
        }

    }
}
