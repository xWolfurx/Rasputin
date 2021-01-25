package net.wolfur.rasputin.bungie.type;

public enum MembershipType {

    NONE(0),
    TIGER_XBOX(1),
    TIGER_PSN(2),
    TIGER_STEAM(3),
    TIGER_BLIZZARD(4),
    TIGER_STADIA(5),
    TIGER_DEMON(10),
    BUNGIE_NEXT(254),
    ALL(-1);

    private final int id;

    MembershipType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static MembershipType getByName(String name) {
        for(MembershipType membershipType : values()) {
            if(membershipType.name().equalsIgnoreCase(name)) {
                return membershipType;
            }
        }
        return null;
    }

    public static MembershipType getById(int id) {
        for(MembershipType membershipType : values()) {
            if(membershipType.getId() == id) {
                return membershipType;
            }
        }
        return null;
    }

}
