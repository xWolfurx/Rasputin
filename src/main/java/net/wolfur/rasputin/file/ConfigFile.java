package net.wolfur.rasputin.file;

import net.wolfur.rasputin.file.builder.FileBase;
import net.wolfur.rasputin.file.builder.yaml.FileConfiguration;

public class ConfigFile extends FileBase {

    private String token;
    private String apiKey;
    private long guildId;

    private String clientId;
    private String clientSecret;

    private long defaultBungieUser;

    private int reloadHour;
    private int resetHour;

    public ConfigFile() {
        super("files", "config");
        this.writeDefaults();
        this.loadToken();
        this.loadAPIKey();
        this.loadGuildId();

        this.loadClientId();
        this.loadClientSecret();

        this.loadDefaultBungieUser();

        this.loadReloadHour();
        this.loadResetHour();
    }

    private void writeDefaults() {
        FileConfiguration cfg = getConfig();

        cfg.addDefault("MySQL.Host", "localhost");
        cfg.addDefault("MySQL.Port", "3306");
        cfg.addDefault("MySQL.Username", "username");
        cfg.addDefault("MySQL.Password", "password");
        cfg.addDefault("MySQL.Database", "database");

        cfg.addDefault("Discord.Token", "token");
        cfg.addDefault("Discord.GuildId", Long.valueOf(-1L));

        cfg.addDefault("Bungie.APIKey", "none");
        cfg.addDefault("Bungie.ClientId", "none");
        cfg.addDefault("Bungie.ClientSecret", "none");
        cfg.addDefault("Bungie.DefaultBungieUser", Long.valueOf(-1L));
        cfg.addDefault("Bungie.ReloadHour", Integer.valueOf(2));
        cfg.addDefault("Bungie.ResetHour", Integer.valueOf(1));

        cfg.options().copyDefaults(true);
        saveConfig();
    }

    private void loadToken() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("Discord.Token") == null) return;
        this.token = cfg.getString("Discord.Token");
    }

    private void loadGuildId() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("Discord.GuildId") == null) return;
        this.guildId = cfg.getLong("Discord.GuildId");
    }

    private void loadClientId() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("Bungie.ClientId") == null) return;
        this.clientId = cfg.getString("Bungie.ClientId");
    }

    private void loadClientSecret() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("Bungie.ClientSecret") == null) return;
        this.clientSecret = cfg.getString("Bungie.ClientSecret");
    }

    private void loadAPIKey() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("Bungie.APIKey") == null) return;
        this.apiKey = cfg.getString("Bungie.APIKey");
    }

    private void loadDefaultBungieUser() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("Bungie.DefaultBungieUser") == null) return;
        this.defaultBungieUser = cfg.getLong("Bungie.DefaultBungieUser");
    }

    private void loadReloadHour() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("Bungie.ReloadHour") == null) return;
        this.reloadHour = cfg.getInt("Bungie.ReloadHour");
    }

    private void loadResetHour() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("Bungie.ResetHour") == null) return;
        this.resetHour = cfg.getInt("Bungie.ResetHour");
    }

    public String getToken() {
        return this.token;
    }

    public long getGuildId() {
        return this.guildId;
    }

    public String getAPIKey() {
        return this.apiKey;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public long getDefaultBungieUser() {
        return this.defaultBungieUser;
    }

    public int getReloadHour() {
        return this.reloadHour;
    }

    public int getResetHour() {
        return this.resetHour;
    }
}
