package net.wolfur.rasputin.survey;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.util.TimeUtil;

import java.awt.*;
import java.util.Map;

public class Survey {

    private long id;

    private long messageId;
    private long channelId;

    private String name;

    private long startDate;
    private long endDate;

    private User creator;

    private String description;

    private Map<Integer, String> answers;
    private Map<User, Integer> participators;

    public Survey(long id, long messageId, long channelId, String name, long startDate, long endDate, User creator, String description, Map<Integer, String> answers, Map<User, Integer> participators) {
        this.id = id;

        this.messageId = messageId;
        this.channelId = channelId;

        this.name = name;

        this.startDate = startDate;
        this.endDate = endDate;

        this.creator = creator;

        this.description = description;

        this.answers = answers;
        this.participators = participators;
    }

    public long getId() {
        return this.id;
    }

    public long getMessageId() {
        return this.messageId;
    }

    public long getChannelId() {
        return this.channelId;
    }

    public String getName() {
        return this.name;
    }

    public long getStartDate() {
        return this.startDate;
    }

    public long getEndDate() {
        return this.endDate;
    }

    public User getCreator() {
        return this.creator;
    }

    public String getDescription() {
        return this.description;
    }

    public Map<Integer, String> getAnswers() {
        return this.answers;
    }

    public Map<User, Integer> getParticipators() {
        return this.participators;
    }

    private EmbedBuilder createEmbedBuilder() {
        EmbedBuilder embedBuilder = new EmbedBuilder()

                .setTitle("Survey » #" + this.getId())
                .setColor(Color.CYAN)
                .setDescription(this.getDescription())
                .setThumbnail("")
                .setFooter("Survey ends in " + TimeUtil.timeToString((this.getEndDate() - this.getStartDate()), true), Main.getJDA().getSelfUser().getAvatarUrl());

        for(Map.Entry<Integer, String> answersEntry : this.getAnswers().entrySet()) {
            embedBuilder.addField("Answer » " + Main.getEmoteManager().getCustomEmote("number_" + answersEntry.getKey()).getAsMention(), answersEntry.getValue(), true);
        }

        return embedBuilder;
    }

    private void addReactions() {
        
    }

    public void sendMessage() {

    }
}
