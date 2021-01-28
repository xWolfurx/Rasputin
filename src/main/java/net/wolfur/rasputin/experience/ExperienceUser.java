package net.wolfur.rasputin.experience;

import net.dv8tion.jda.api.entities.User;
import net.wolfur.rasputin.database.DatabaseUpdate;

public class ExperienceUser extends DatabaseUpdate {

    private User user;
    private int level;
    private int experience;

    public ExperienceUser(User user) {
        this.user = user;
        this.level = 0;
        this.experience = 0;

        this.loadData();
        this.setReady(true);
    }

    public User getUser() {
        return this.user;
    }

    public int getLevel() {
        return this.level;
    }

    public int getExperience() {
        return this.experience;
    }

    public int getExperienceToNextLevel() {
        return this.level * (this.level / 2) * 100;
    }

    public void setExperience(int experience) {
        if(experience < 0) experience = 0;
        this.experience = experience;
        this.setUpdate(true);
    }

    public void addExperience(int experience) {
        if(experience <= 0) return;
        this.setExperience(this.getExperience() + experience);
    }

    public void removeExperience(int experience) {
        if(experience <= 0) return;
        if(this.getExperience() - experience <= 0) this.setExperience(0);
        this.setExperience(this.getExperience() - experience);
    }

    public void setLevel(int level) {
        if(level < 0) level = 0;
        this.level = level;
        this.setUpdate(true);
    }

    public void addLevel(int level) {
        if(level <= 0) return;
        this.setLevel(this.getLevel() + level);
    }

    public void removeLevel(int level) {
        if(level <= 0) return;
        if(this.getLevel() - level <= 0) this.setLevel(0);
        this.setLevel(this.getLevel() - level);
    }

    @Override
    public void saveData() {

    }

    @Override
    public void saveDataAsync() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void loadDataAsync() {

    }
}
