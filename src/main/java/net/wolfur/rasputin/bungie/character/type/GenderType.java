package net.wolfur.rasputin.bungie.character.type;

public enum GenderType {

    MALE("Male", 0),
    FEMALE("Female", 1),
    UNKNOWN("Unknown", 2);

    private final String betterName;
    private final int id;

    GenderType(String betterName, int id) {
        this.betterName = betterName;
        this.id = id;
    }

    public String getBetterName() {
        return this.betterName;
    }

    public int getId() {
        return this.id;
    }

    public static GenderType getGenderById(int id) {
        for(GenderType genderType : values()) {
            if(genderType.getId() == id) {
                return genderType;
            }
        }
        return GenderType.UNKNOWN;
    }
}
