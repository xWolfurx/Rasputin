package net.wolfur.rasputin.other;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;
import net.wolfur.rasputin.util.Utils;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Raid {

    private String id;
    private RaidType raidType;
    private User creator;
    private User leader;
    private String description;

    private Date date;
    private Date time;
    private Date completeDate;
    private long timestamp;

    private List<User> runners;
    private List<User> alternatives;

    private Message message;
    private int extension;

    private Timer alertTask;
    private Timer deleteTask;

    public Raid(RaidType raidType, Date date, Date time, User creator, String description) {
        this.raidType = raidType;
        this.date = date;
        this.time = time;
        this.creator = creator;
        this.leader = creator;
        this.description = description;
        this.timestamp = System.currentTimeMillis();

        this.runners = new ArrayList<>();
        this.alternatives = new ArrayList<>();

        this.message = null;
        this.extension = 0;

        try {
            this.completeDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(new SimpleDateFormat("dd.MM.yyyy").format(this.date) + " " + new SimpleDateFormat("HH:mm").format(this.time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.startAlertTask(((this.getCompleteDate().getTime() - TimeUtil.parseTime("10m")) - System.currentTimeMillis()));
        this.startDeleteTask(((this.getCompleteDate().getTime() + TimeUtil.parseTime("10m")) - System.currentTimeMillis()));
    }

    public Raid(String id, String channel_id, RaidType raidType, Date date, Date time, Date completeDate, User creator, User leader, List<User> runners, List<User> alternatives, String description, int extension, long timestamp) {
        this.id = id;
        this.raidType = raidType;
        this.date = date;
        this.time = time;
        this.completeDate = completeDate;
        this.creator = creator;
        this.leader = leader;
        this.description = description;
        this.timestamp = timestamp;

        this.runners = runners;
        this.alternatives = alternatives;
        this.extension = extension;

        List<Message> messages = Objects.requireNonNull(Main.getJDA().getTextChannelById(channel_id)).getHistory().retrievePast(30).complete();
        this.message = this.getMessage(messages, this.id);

        this.startAlertTask(((this.getCompleteDate().getTime() - TimeUtil.parseTime("10m")) - System.currentTimeMillis()));
        this.startDeleteTask(((this.getCompleteDate().getTime() + TimeUtil.parseTime("10m")) - System.currentTimeMillis()));

        this.updateMessage();
    }

    public String getId() {
        return this.id;
    }

    public RaidType getRaidType() {
        return this.raidType;
    }

    public User getCreator() {
        return this.creator;
    }

    public User getLeader() {
        return this.leader;
    }

    public String getDescription() {
        return this.description;
    }

    public Date getDate() {
        return this.date;
    }

    public Date getTime() {
        return this.time;
    }

    public Date getCompleteDate() {
        return this.completeDate;
    }

    public List<User> getRunners() {
        return this.runners;
    }

    public List<User> getAlternatives() {
        return this.alternatives;
    }

    public Message getMessage() {
        return this.message;
    }

    public int getExtension() {
        return this.extension;
    }

    public Timer getAlertTask() {
        return this.alertTask;
    }

    public Timer getDeleteTask() {
        return this.deleteTask;
    }

    public boolean isRunner(User user) {
        return this.runners.contains(user);
    }

    public boolean isAlternative(User user) {
        return this.alternatives.contains(user);
    }

    public boolean isCreator(User user) {
        return this.creator.equals(user);
    }

    public boolean isLeader(User user) {
        return (this.leader != null && this.leader.equals(user));
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void addRunner(User user) {
        if(this.isRunner(user)) return;
        if(this.isAlternative(user)) this.alternatives.remove(user);
        if(this.isLeader(user)) return;
        if(this.getRunners().size() >= this.getRaidType().getMaxPlayers()) return;

        this.runners.add(user);
        this.updateMessage();
    }

    public void removeRunner(User user) {
        if(!this.isRunner(user)) return;

        this.runners.remove(user);
        this.updateMessage();
    }

    public void addAlternative(User user) {
        if(this.isAlternative(user)) return;
        if(this.isRunner(user)) this.runners.remove(user);
        if(this.isLeader(user)) return;

        this.alternatives.add(user);
        this.updateMessage();
    }

    public void removeAlternative(User user) {
        if(!this.isAlternative(user)) return;

        this.alternatives.remove(user);
        this.updateMessage();
    }

    public void setLeader(User user) {
        if(user != null) {
            if (this.isLeader(user)) return;
            if (this.isRunner(user)) this.runners.remove(user);
            if (this.isAlternative(user)) this.alternatives.remove(user);
        }

        this.leader = user;
        this.updateMessage();
    }

    private void setExtension(int extension) {
        this.extension = extension;
    }

    public void setNewTime(Date time) {
        this.time = time;

        try {
            this.completeDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(new SimpleDateFormat("dd.MM.yyyy").format(this.date) + " " + new SimpleDateFormat("HH:mm").format(this.time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(this.alertTask != null) this.alertTask.cancel();
        if(this.deleteTask != null) this.deleteTask.cancel();

        this.startAlertTask(((this.getCompleteDate().getTime() - TimeUtil.parseTime("10m")) - System.currentTimeMillis()));
        this.startDeleteTask(((this.getCompleteDate().getTime() + TimeUtil.parseTime("10m")) - System.currentTimeMillis()));

        this.updateMessage();
        Logger.info("Updated time from Activity '" + this.getRaidType().getName() + "'.", true);

        List<User> users = new ArrayList<>();
        users.addAll(this.getRunners());
        users.add(this.getLeader());
        users.addAll(this.getAlternatives());

        for (User user : users) {
            user.openPrivateChannel().queue(channel -> {
                channel.sendMessage(Raid.this.getTimeChangedEmbed().build()).queue(null, Utils.ignore);
            });
        }

        Main.getCoreManager().getRaidManager().updateRaidInDatabaseAsync(this, success -> {
            if(!success) {
                Logger.error("An error occurred while updating raid into database.", true);
            }
        });
    }

    public String sendMessage(MessageChannel channel) {
        this.message = channel.sendMessage(this.getEmbedBuilder().build()).complete();
        this.id = this.message.getId();

        this.addReactions();

        return id;
    }

    public void updateMessage() {
        this.message.editMessage(this.getEmbedBuilder().build()).queue();
        Main.getCoreManager().getRaidManager().updateRaidInDatabaseAsync(this, success -> {
            if(!success) {
                Logger.error("An error occurred while updating raid in database.", true);
            }
        });
    }

    private String getRunnersAsString() {
        StringBuilder sb = new StringBuilder();
        int length = 2;

        for(User user : this.getRunners()) {
            BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(user);
            if(length > this.getRaidType().getMaxPlayers()) break;
            sb.append(length + ". - " + user.getAsMention());
            sb.append(" " + (bungieUser.isRegistered() ? this.getRaidType().getActivityHash().equalsIgnoreCase("-1") ? "" : "[Runs: " + bungieUser.getRaidCompletions(this.getRaidType().getActivityHash()) + "]" : ""));
            sb.append("\n");
            length++;
        }
        while(length <= this.getRaidType().getMaxPlayers()) {
            sb.append(length + ". - <Frei>").append("\n");
            length++;
        }

        return sb.substring(0, sb.length() - 1);
    }

    private String getAlternativesAsString() {
        StringBuilder sb = new StringBuilder();
        int i = 1;

        for(User user : this.getAlternatives()) {
            BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(user);
            sb.append(i + ". - " + user.getAsMention());
            sb.append(" " + (bungieUser.isRegistered() ? this.getRaidType().getActivityHash().equalsIgnoreCase("-1") ? "" : "[Runs: " + bungieUser.getRaidCompletions(this.getRaidType().getActivityHash()) + "]" : ""));
            sb.append("\n");
            i++;
        }

        return (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "<Keine Hüter eingetragen>");
    }

    private EmbedBuilder getEmbedBuilder() {
        BungieUser bungieLeaderUser = Main.getCoreManager().getBungieUserManager().getBungieUser(this.getLeader());

        String description = "**Leader**" + "\n" +
                "1. - " + (this.getLeader() != null ? this.getLeader().getAsMention() + " " + (bungieLeaderUser.isRegistered() ? this.getRaidType().getActivityHash().equalsIgnoreCase("-1") ? "" : "[Runs: " + bungieLeaderUser.getRaidCompletions(this.getRaidType().getActivityHash()) + "]" : "") : "<Frei>") + "\n" + "\n" +
                "**Teilnehmer:**" + "\n" +
                this.getRunnersAsString() + "\n" + "\n" +
                "**Alternativen**" + "\n" +
                this.getAlternativesAsString() + "\n" + "\n" +
                "**Beschreibung:**" + "\n" +
                this.getDescription();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(this.getRaidType().getBetterName() + " - " + Utils.convertDateToString(this.getDate().getDay()) + " (" + new SimpleDateFormat("dd.MM.yyyy").format(this.getDate()) + ") um " + new SimpleDateFormat("HH:mm").format(this.getTime()) + " Uhr")
                .setDescription(description)
                .setFooter("Erstellt von @" + this.getCreator().getName() + " - " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(this.getTimestamp())) + " Uhr", this.getCreator().getAvatarUrl())
                .setThumbnail(this.getRaidType().getIconURL());

        return embedBuilder;
    }

    private void addReactions() {
        this.message.addReaction(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddRunner())).queue();
        this.message.addReaction(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getRemoveRunner())).queue();
        this.message.addReaction(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAddAlternative())).queue();
        this.message.addReaction(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getSetLeader())).queue();
        this.message.addReaction(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getChangeTime())).queue();
        this.message.addReaction(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getDeleteActivity())).queue();
    }

    private void startAlertTask(long delay) {
        if(delay > 0L) {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    List<User> users = new ArrayList<>();
                    users.addAll(Raid.this.getRunners());
                    users.add(Raid.this.getLeader());

                    if(users.size() < Raid.this.getRaidType().getMaxPlayers()) {
                        List<User> alternatives = new ArrayList<>();
                        alternatives.addAll(Raid.this.getAlternatives());
                        while(alternatives.size() > 0) {
                            User user = alternatives.get(0);
                            users.add(user);
                            alternatives.remove(user);

                            Raid.this.runners.add(user);
                            Raid.this.alternatives.remove(user);

                            if(users.size() >= Raid.this.getRaidType().getMaxPlayers()) {
                                break;
                            }
                        }
                        Raid.this.updateMessage();
                    }

                    if(users.size() < Raid.this.getRaidType().getMaxPlayers()) {
                        if(Raid.this.extension < 2) {
                            Raid.this.time.setTime(new Date(System.currentTimeMillis() + 1800000L).getTime());
                            Raid.this.updateMessage();
                            for (User user : users) {
                                user.openPrivateChannel().queue(channel -> {
                                    channel.sendMessage(Raid.this.getWaitingEmbed().build()).queue(null, Utils.ignore);
                                });
                            }
                            this.cancel();
                            Raid.this.deleteTask.cancel();
                            Raid.this.startAlertTask(1200000L);
                            Raid.this.startDeleteTask(2400000L);
                            Raid.this.sendMessage((getRaidType().getMaxPlayers() - users.size()));

                            Raid.this.setExtension(Raid.this.getExtension() + 1);
                        } else {
                            for (User user : users) {
                                user.openPrivateChannel().queue(channel -> {
                                    channel.sendMessage(Raid.this.getCanceledEmbed("Nicht genügend Teilnehmer").build()).queue(null, Utils.ignore);
                                });
                            }
                            Main.getCoreManager().getRaidManager().deleteRaid(Raid.this.getId(), "Not enough members");
                        }
                    } else {
                        for (User user : users) {
                            user.openPrivateChannel().queue(channel -> {
                                channel.sendMessage(Raid.this.getAlertEmbed().build()).queue(null, Utils.ignore);
                            });
                        }
                    }
                }
            };

            this.alertTask = new Timer("alert-" + this.getId());
            this.alertTask.schedule(timerTask, delay);
        }
    }

    public void checkStatus() {
        if(this.getExtension() > 0) {
            if (this.getRunners().size() < this.getRaidType().getMaxPlayers()) {
                if (this.getAlternatives().size() > 0) {
                    List<User> users = new ArrayList<>();
                    users.addAll(Raid.this.getRunners());
                    users.add(Raid.this.getLeader());

                    List<User> alternatives = new ArrayList<>();
                    alternatives.addAll(Raid.this.getAlternatives());
                    while (alternatives.size() > 0) {
                        User user = alternatives.get(0);
                        users.add(user);
                        alternatives.remove(user);

                        Raid.this.runners.add(user);
                        Raid.this.alternatives.remove(user);

                        if (users.size() >= Raid.this.getRaidType().getMaxPlayers()) {
                            break;
                        }
                    }
                    this.updateMessage();
                }
            }
            if(this.getRunners().size() >= this.getRaidType().getMaxPlayers()) {
                if(this.getAlertTask() != null) {
                    this.getAlertTask().cancel();
                }
                for(User user : this.getRunners()) {
                    user.openPrivateChannel().queue(channel -> channel.sendMessage(Raid.this.getAlertNowEmbed().build()).queue());
                }
            }
        }
    }


    private void startDeleteTask(long delay) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Main.getCoreManager().getRaidManager().deleteRaid(Raid.this.getId(), "Run out");
            }
        };

        this.deleteTask = new Timer("delete-" + this.getId());
        this.deleteTask.schedule(timerTask, delay);
    }

    private boolean isDeleteTaskRunning() {
        return this.deleteTask != null;
    }

    private boolean isAlertTaskRunning() {
        return this.alertTask != null;
    }

    public void stopTasks() {
        if(isAlertTaskRunning()) {
            this.alertTask.cancel();
            this.alertTask = null;
        }
        if(isDeleteTaskRunning()) {
            this.deleteTask.cancel();
            this.deleteTask = null;
        }
    }

    private void sendMessage(int needed) {
        TextChannel textChannel = Main.getGuild().getTextChannelById(Main.getFileManager().getChannelFile().getChannel("talk").getChannelId());
        if(textChannel != null) {
            textChannel.sendMessage("Es " + (needed == 1 ? "wird" : "werden") + " noch " + needed + " Hüter für den Raid '" + this.getRaidType().getBetterName() + "' benötigt.").queue();
        }
    }

    private Message getMessage(List<Message> messages, String id) {
        for(Message message : messages) {
            if(message.getId().equalsIgnoreCase(id)) {
                return message;
            }
        }
        return null;
    }

    private EmbedBuilder getWaitingEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Activity extended: " + this.getRaidType().getBetterName())
                .setDescription("**Die Aktivität wurde um 30 Minuten verlängert.**" + "\n" + "\n" +
                        "**Grund:** Nicht genügend Teilnehmer")
                .setThumbnail(this.getRaidType().getIconURL());

        return embedBuilder;
    }

    public EmbedBuilder getCanceledEmbed(String reason) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Activity canceled: " + this.getRaidType().getBetterName())
                .setDescription("**Die Aktivität wurde abgesagt.**" + "\n" + "\n" +
                        "**Grund:** " + reason)
                .setThumbnail(this.getRaidType().getIconURL());

        return embedBuilder;
    }

    private EmbedBuilder getTimeChangedEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle("Activity updated: " + this.getRaidType().getBetterName())
                .setDescription("**Die Uhrzeit der Aktivität wurde bearbeitet.**" + "\n" + "\n" +
                        "**Neue Uhrzeit:** " + new SimpleDateFormat("HH:mm").format(this.getTime()) + " Uhr")
                .setThumbnail(this.getRaidType().getIconURL());

        return embedBuilder;
    }

    public EmbedBuilder removedFromRaid() {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Removed from activity: " + this.getRaidType().getBetterName())
                .setDescription("**Du wurdest aus der Raid-Planung '" + this.getRaidType().getBetterName() + "' am " + new SimpleDateFormat("dd.MM.yyyy").format(this.getDate()) + " entfernt.**" + "\n" + "\n" +
                        "**Grund:** Raidsperre erhalten")
                .setThumbnail(this.getRaidType().getIconURL());

        return embedBuilder;
    }

    private EmbedBuilder getAlertEmbed() {
        StringBuilder sb = new StringBuilder().append(Raid.this.getLeader().getName()).append(", ");
        for (User users : Raid.this.getRunners()) {
            sb.append(users.getName()).append(", ");
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Alert protocol initiated: " + this.getRaidType().getBetterName())
                .setDescription("Hüter: " + sb.substring(0, sb.length() - 2) + "\n" +
                        "\n" +
                        "Versammelt euch in **10:00 Minuten** im Orbit.")
                .setThumbnail(this.getRaidType().getIconURL());

        return embedBuilder;
    }

    private EmbedBuilder getAlertNowEmbed() {
        StringBuilder sb = new StringBuilder().append(Raid.this.getLeader().getName()).append(", ");
        for (User users : Raid.this.getRunners()) {
            sb.append(users.getName()).append(", ");
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.GRAY)
                .setTitle("Alert protocol initiated: " + this.getRaidType().getBetterName())
                .setDescription("Hüter: " + sb.substring(0, sb.length() - 2) + "\n" +
                        "\n" +
                        "Versammelt euch **jetzt** im Orbit.")
                .setThumbnail(this.getRaidType().getIconURL());

        return embedBuilder;
    }

    public EmbedBuilder getHistoryEmbedBuilder(String reason) {
        String description = "**Leader**" + "\n" +
                "1. - " + (this.getLeader() != null ? this.getLeader().getAsMention() : "<Frei>") + "\n" + "\n" +
                "**Teilnehmer:**" + "\n" +
                this.getRunnersAsString() + "\n" + "\n" +
                "**Alternativen**" + "\n" +
                this.getAlternativesAsString() + "\n" + "\n" +
                "**Beschreibung:**" + "\n" +
                this.getDescription() + "\n" + "\n" +
                "**Entfernt:**" + "\n" +
                reason;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(this.getRaidType().getBetterName() + " - " + Utils.convertDateToString(this.getDate().getDay()) + " (" + new SimpleDateFormat("dd.MM.yyyy").format(this.getDate()) + ") um " + new SimpleDateFormat("HH:mm").format(this.getTime()) + " Uhr")
                .setDescription(description)
                .setFooter("Erstellt von @" + this.getCreator().getName() + " - " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(this.getTimestamp())) + " Uhr", this.getCreator().getAvatarUrl())
                .setThumbnail(this.getRaidType().getIconURL());

        return embedBuilder;
    }
}
