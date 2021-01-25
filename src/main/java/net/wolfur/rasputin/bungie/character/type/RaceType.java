package net.wolfur.rasputin.bungie.character.type;

public enum RaceType {

    HUMAN("Human", 0),
    AWOKEN("Awoken", 1),
    EXO("Exo", 2),
    UNKNOWN("Unknown", 3);

    private final String betterName;
    private final int id;

    RaceType(String betterName, int id) {
        this.betterName = betterName;
        this.id = id;
    }

    public String getBetterName() {
        return this.betterName;
    }

    public int getId() {
        return this.id;
    }

    public static RaceType getRaceById(int id) {
        for(RaceType raceType : values()) {
            if(raceType.getId() == id) {
                return raceType;
            }
        }
        return RaceType.UNKNOWN;
    }
}
