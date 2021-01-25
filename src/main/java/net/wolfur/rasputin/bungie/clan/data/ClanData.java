package net.wolfur.rasputin.bungie.clan.data;

public class ClanData {

    private final long groupId;
    private final String groupName;
    private final long timeCreated;
    private final String motto;
    private final int memberCount;
    private final int clanLevel;
    private final String clanSign;
    private final int clanProgress;
    private final int weeklyProgress;

    private final long joinDate;

    public ClanData(long groupId, String groupName, long timeCreated, String motto, int memberCount, int clanLevel, String clanSign, long joinDate, int clanProgress, int weeklyProgress) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.timeCreated = timeCreated;
        this.motto = motto;
        this.memberCount = memberCount;
        this.clanLevel = clanLevel;
        this.clanSign = clanSign;
        this.joinDate = joinDate;
        this.clanProgress = clanProgress;
        this.weeklyProgress = weeklyProgress;
    }

    public long getGroupId() {
        return this.groupId;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public long getTimeCreated() {
        return this.timeCreated;
    }

    public String getMotto() {
        return this.motto;
    }

    public int getMemberCount() {
        return this.memberCount;
    }

    public int getClanLevel() {
        return this.clanLevel;
    }

    public String getClanSign() {
        return this.clanSign;
    }

    public long getJoinDate() {
        return this.joinDate;
    }

    public int getClanProgress() {
        return this.clanProgress;
    }

    public int getWeeklyProgress() {
        return this.weeklyProgress;
    }

}
