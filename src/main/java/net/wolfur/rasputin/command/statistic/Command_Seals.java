package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.core.Command;

import java.util.List;

public class Command_Seals implements Command {

    private static long[] currentSealHashes = { 2364370869L, 2482004751L, 1561715947L };
    private static long[] pastSealsHashes = { 3214425110L, 1556658903L, 1343839969L, };

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
