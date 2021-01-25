package net.wolfur.rasputin.other;

public enum RoleType {

    VERIFIED,
    VENDOR_NOTIFY;

    public static RoleType getByName(String name) {
        for(RoleType roleType : values()) {
            if(roleType.name().equalsIgnoreCase(name)) {
                return roleType;
            }
        }
        return null;
    }

}
