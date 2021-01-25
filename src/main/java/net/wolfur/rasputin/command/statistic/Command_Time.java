package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.character.DestinyCharacter;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.type.DestinyActivityModeType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Logger;
import net.wolfur.rasputin.util.TimeUtil;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_Time implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, JsonObject deepStoneCryptDataObject, List<JsonObject> patrolActivities) {
        return null;
    }

    private double round(double value, int decimalPoints) {
        double d = Math.pow(10, decimalPoints);
        return Math.round(value * d) / d;
    }
}
