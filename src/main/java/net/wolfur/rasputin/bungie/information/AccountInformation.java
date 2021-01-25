package net.wolfur.rasputin.bungie.information;

import net.wolfur.rasputin.bungie.type.MembershipType;

public class AccountInformation {

    private final String displayName;
    private final MembershipType membershipType;
    private final long membershipId;

    public AccountInformation(String displayName, MembershipType membershipType, long membershipId) {
        this.displayName = displayName;
        this.membershipType = membershipType;
        this.membershipId = membershipId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public MembershipType getMembershipType() {
        return this.membershipType;
    }

    @SuppressWarnings("unused")
    public long getMembershipId() {
        return this.membershipId;
    }
}
