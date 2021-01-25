package net.wolfur.rasputin.file;

import net.wolfur.rasputin.file.builder.FileBase;
import net.wolfur.rasputin.file.builder.yaml.FileConfiguration;
import net.wolfur.rasputin.other.RoleType;
import net.wolfur.rasputin.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class RoleDefinitionFile extends FileBase {

    private Map<RoleType, Long> roles;

    public RoleDefinitionFile() {
        super("files", "role_definition");
        this.roles = new HashMap<>();

        this.writeDefaults();
        this.loadRoles();
    }

    private void writeDefaults() {
        FileConfiguration cfg = getConfig();

        for(RoleType roleType : RoleType.values()) {
            cfg.addDefault("roles." + roleType.name(), Long.valueOf(-1L));
        }

        cfg.options().copyDefaults(true);
        saveConfig();
    }

    private void loadRoles() {
        FileConfiguration cfg = getConfig();
        for(String roleTypeName : cfg.getConfigurationSection("roles").getKeys(false)) {
            RoleType roleType = RoleType.getByName(roleTypeName);

            if(roleType == null) {
                Logger.warning("Can´t find RoleType '" + roleTypeName + "'.", true);
                continue;
            }

            long id = cfg.getLong("roles." + roleTypeName);
            this.roles.put(roleType, id);
        }
    }

    public Long getRoleId(RoleType roleType) {
        if(!this.roles.containsKey(roleType)) return -1L;
        return this.roles.get(roleType);
    }
}
