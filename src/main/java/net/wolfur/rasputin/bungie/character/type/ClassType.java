package net.wolfur.rasputin.bungie.character.type;

public enum ClassType {

    TITAN("Titan", 0, 3655393761L),
    HUNTER("Hunter", 1, 671679327L),
    WARLOCK("Warlock", 2, 2271682572L),
    UNKNOWN("Unknown", 3, -1L);

    private final String betterName;
    private final int id;
    private final long classHash;

    ClassType(String betterName, int id, long classHash) {
        this.betterName = betterName;
        this.id = id;
        this.classHash = classHash;
    }

    public String getBetterName() {
        return this.betterName;
    }

    public int getId() {
        return this.id;
    }

    public long getClassHash() {
        return this.classHash;
    }

    public static ClassType getClassById(int id) {
        for(ClassType classType : values()) {
            if(classType.getId() == id) {
                return classType;
            }
        }
        return ClassType.UNKNOWN;
    }

    public static ClassType getClassByClassHash(long classHash) {
        for(ClassType classType : values()) {
            if(classType.getClassHash() == classHash) {
                return classType;
            }
        }
        return ClassType.UNKNOWN;
    }

}
