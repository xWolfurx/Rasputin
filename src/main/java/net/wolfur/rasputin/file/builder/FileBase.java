package net.wolfur.rasputin.file.builder;

import net.wolfur.rasputin.file.builder.yaml.FileConfiguration;
import net.wolfur.rasputin.file.builder.yaml.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FileBase {

    private String path;
    private String fileName;
    private File file;
    private FileConfiguration cfg;

    public FileBase(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
    }

    public void reloadConfig() {
        if(this.file == null) {
            this.file = new File(this.path, this.fileName + ".yml");
        }
        if(!this.file.exists()) {
            this.file.getParentFile().mkdir();
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.cfg = YamlConfiguration.loadConfiguration(this.file);
    }

    public FileConfiguration getConfig() {
        if(this.cfg == null) {
            this.reloadConfig();
        }
        return this.cfg;
    }

    public void saveConfig() {
        if((this.file == null) || (this.cfg == null)) {
            return;
        }
        try {
            this.cfg.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
