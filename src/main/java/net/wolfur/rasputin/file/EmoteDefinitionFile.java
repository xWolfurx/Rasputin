package net.wolfur.rasputin.file;

import net.wolfur.rasputin.file.builder.FileBase;
import net.wolfur.rasputin.file.builder.yaml.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class EmoteDefinitionFile extends FileBase {

    //Activities
    private String addRunner, removeRunner, addAlternative, setLeader, changeTime, deleteActivity;

    //Materials
    private Map<String, String> materials;

    //Modifiers
    private Map<String, String> modifiers;

    //AccountTypes
    private Map<Integer, String> accountTypes;

    //ClassTypes
    private Map<String, String> classTypes;

    //Numbers
    private Map<Integer, String> numbers;

    public EmoteDefinitionFile() {
        super("files", "emote_definition");
        this.materials = new HashMap<>();
        this.accountTypes = new HashMap<>();
        this.classTypes = new HashMap<>();
        this.numbers = new HashMap<>();
        this.modifiers = new HashMap<>();
        this.writeDefaults();

        this.loadAddRunner();
        this.loadRemoveRunner();
        this.loadAddAlternative();
        this.loadSetLeader();
        this.loadChangeTime();
        this.loadDeleteActivity();

        this.loadMaterials();
        this.loadAccountTypes();
        this.loadClassTypes();
        this.loadNumbers();
        this.loadModifiers();

    }

    private void writeDefaults() {
        FileConfiguration cfg = getConfig();

        cfg.addDefault("activities.add_runner", "database_error");
        cfg.addDefault("activities.remove_runner", "database_error");
        cfg.addDefault("activities.add_alternative", "database_error");
        cfg.addDefault("activities.set_leader", "database_error");
        cfg.addDefault("activities.change_time", "database_error");
        cfg.addDefault("activities.delete_activity", "database_error");

        cfg.addDefault("materials.1022552290", "database_error");
        cfg.addDefault("materials.3159615086", "database_error");
        cfg.addDefault("materials.1305274547", "database_error");
        cfg.addDefault("materials.49145143", "database_error");
        cfg.addDefault("materials.3487922223", "database_error");
        cfg.addDefault("materials.1177810185", "database_error");
        cfg.addDefault("materials.592227263", "database_error");
        cfg.addDefault("materials.3592324052", "database_error");
        cfg.addDefault("materials.31293053", "database_error");
        cfg.addDefault("materials.950899352", "database_error");
        cfg.addDefault("materials.2014411539", "database_error");
        cfg.addDefault("materials.1485756901", "database_error");
        cfg.addDefault("materials.293622383", "database_error");

        cfg.addDefault("modifier.644078431", "stasis_singe");
        cfg.addDefault("modifier.2558957669", "solar_singe");
        cfg.addDefault("modifier.3215384520", "arc_singe");
        cfg.addDefault("modifier.3362074814", "void_singe");
        cfg.addDefault("modifier.1206783463", "grenadier");
        cfg.addDefault("modifier.4221013735", "blackout");

        cfg.addDefault("accounts.1", "database_error");
        cfg.addDefault("accounts.2", "database_error");
        cfg.addDefault("accounts.3", "database_error");
        cfg.addDefault("accounts.4", "database_error");
        cfg.addDefault("accounts.5", "database_error");
        cfg.addDefault("accounts.10", "database_error");

        cfg.addDefault("class.warlock", "database_error");
        cfg.addDefault("class.titan", "database_error");
        cfg.addDefault("class.hunter", "database_error");

        cfg.addDefault("numbers.1", "one");
        cfg.addDefault("numbers.2", "two");
        cfg.addDefault("numbers.3", "three");
        cfg.addDefault("numbers.4", "four");
        cfg.addDefault("numbers.5", "five");
        cfg.addDefault("numbers.6", "six");
        cfg.addDefault("numbers.7", "seven");
        cfg.addDefault("numbers.8", "eight");
        cfg.addDefault("numbers.9", "nine");
        cfg.addDefault("numbers.0", "zero");

        cfg.options().copyDefaults(true);
        saveConfig();
    }

    private void loadAddRunner() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("activities.add_runner") == null) {
            this.addRunner = "none";
            return;
        }
        this.addRunner = cfg.getString("activities.add_runner");
    }

    private void loadRemoveRunner() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("activities.remove_runner") == null) {
            this.removeRunner = "none";
            return;
        }
        this.removeRunner = cfg.getString("activities.remove_runner");
    }

    private void loadAddAlternative() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("activities.add_alternative") == null) {
            this.addAlternative = "none";
            return;
        }
        this.addAlternative = cfg.getString("activities.add_alternative");
    }

    private void loadSetLeader() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("activities.set_leader") == null) {
            this.setLeader = "none";
            return;
        }
        this.setLeader = cfg.getString("activities.set_leader");
    }

    private void loadChangeTime() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("activities.change_time") == null) {
            this.changeTime = "none";
            return;
        }
        this.changeTime = cfg.getString("activities.change_time");
    }

    private void loadDeleteActivity() {
        FileConfiguration cfg = getConfig();
        if(cfg.get("activities.delete_activity") == null) {
            this.deleteActivity = "none";
            return;
        }
        this.deleteActivity = cfg.getString("activities.delete_activity");
    }

    private void loadMaterials() {
        FileConfiguration cfg = getConfig();
        for(String hash : cfg.getConfigurationSection("materials").getKeys(false)) {
            String labelName = cfg.getString("materials." + hash);
            this.materials.put(hash, labelName);
        }
    }

    private void loadAccountTypes() {
        FileConfiguration cfg = getConfig();
        for(String hash : cfg.getConfigurationSection("accounts").getKeys(false)) {
            String labelName = cfg.getString("accounts." + hash);
            this.accountTypes.put(Integer.valueOf(hash), labelName);
        }
    }

    private void loadClassTypes() {
        FileConfiguration cfg = getConfig();
        for(String hash : cfg.getConfigurationSection("class").getKeys(false)) {
            String labelName = cfg.getString("class." + hash);
            this.classTypes.put(hash, labelName);
        }

    }

    private void loadNumbers() {
        FileConfiguration cfg = getConfig();
        for(String hash : cfg.getConfigurationSection("numbers").getKeys(false)) {
            String labelName = cfg.getString("numbers." + hash);
            this.numbers.put(Integer.valueOf(hash), labelName);
        }
    }

    private void loadModifiers() {
        FileConfiguration cfg = getConfig();
        for(String hash : cfg.getConfigurationSection("modifier").getKeys(false)) {
            String labelName = cfg.getString("modifier." + hash);
            this.modifiers.put(hash, labelName);
        }
    }

    public String getMaterial(String hash) {
        return this.materials.getOrDefault(hash, "database_error");
    }

    public String getAccountType(int hash) {
        return this.accountTypes.getOrDefault(hash, "database_error");
    }

    public String getClassType(String hash) {
        return this.classTypes.getOrDefault(hash, "database_error");
    }

    public String getNumber(int hash) {
        return this.numbers.getOrDefault(hash, "database_error");
    }

    public String getModifier(String hash) {
        return this.modifiers.getOrDefault(hash, "database_error");
    }

    public String getAddRunner() {
        return this.addRunner;
    }

    public String getRemoveRunner() {
        return this.removeRunner;
    }

    public String getAddAlternative() {
        return this.addAlternative;
    }

    public String getSetLeader() {
        return this.setLeader;
    }

    public String getChangeTime() {
        return this.changeTime;
    }

    public String getDeleteActivity() {
        return this.deleteActivity;
    }

}
