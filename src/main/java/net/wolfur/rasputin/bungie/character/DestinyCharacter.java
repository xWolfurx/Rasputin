package net.wolfur.rasputin.bungie.character;

public class DestinyCharacter {

    private final long characterId;

    private final long dateLastPlayed;
    private final int minutesPlayedTotal;
    private final int lightLevel;

    private final int raceType;
    private final int genderType;
    private final int classType;

    private final String emblemPath;


    public DestinyCharacter(long characterId, long dateLastPlayed, int minutesPlayedTotal, int lightLevel, int raceType, int genderType, int classType, String emblemPath) {
        this.characterId = characterId;
        this.dateLastPlayed = dateLastPlayed;
        this.minutesPlayedTotal = minutesPlayedTotal;
        this.lightLevel = lightLevel;
        this.raceType = raceType;
        this.genderType = genderType;
        this.classType = classType;
        this.emblemPath = emblemPath;
    }

    public long getCharacterId() {
        return this.characterId;
    }

    public long getDateLastPlayed() {
        return this.dateLastPlayed;
    }

    @SuppressWarnings("unused")
    public int getMinutesPlayedTotal() {
        return this.minutesPlayedTotal;
    }

    public int getLightLevel() {
        return this.lightLevel;
    }

    public int getRaceType() {
        return this.raceType;
    }

    public int getGenderType() {
        return this.genderType;
    }

    public int getClassType() {
        return this.classType;
    }

    public String getEmblemPath() {
        return this.emblemPath;
    }


}
