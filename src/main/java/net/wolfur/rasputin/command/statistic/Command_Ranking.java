package net.wolfur.rasputin.command.statistic;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.TimeUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Command_Ranking implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(Main.getFileManager().getChannelFile().isCommandChannel(event.getTextChannel().getIdLong())) {
            BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
            if (bungieUser.isRegistered()) {
                if(args.length == 1) {
                    if(args[0].equalsIgnoreCase("triumph")) {
                        event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, "Active Triumph Score", "3981543480").build()).complete();
                    } else if(args[0].equalsIgnoreCase("totalTriumph")) {
                        event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, "Total Triumph Score", "3329916678").build()).complete();
                    } else if(args[0].equalsIgnoreCase("glory")) {
                        event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, "Glory Points", "268448617").build()).complete();
                    } else if(args[0].equalsIgnoreCase("valor")) {
                        event.getTextChannel().sendMessage(this.createCrucibleEmbedBuilder(bungieUser, "Valor Points", "2872213304", "3626149776").build()).complete();
                    } else if(args[0].equalsIgnoreCase("infamy")) {
                        event.getTextChannel().sendMessage(this.createCrucibleEmbedBuilder(bungieUser, "Infamy Points", "250859887", "1963785799").build()).complete();
                    } else if(args[0].equalsIgnoreCase("tower")) {
                        event.getTextChannel().sendMessage(this.createTowerEmbedBuilder(bungieUser).build()).complete();
                    } else {
                        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Dieser Typ existiert nicht. \n\nBitte verwende **.help ranking** um dir die möglichen Typen aufzulisten.").build()).queue(message -> {
                            message.delete().queueAfter(15, TimeUnit.SECONDS);
                        });
                    }
                } else {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Ranking <Type>").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte registriere dich, um diesen Befehl nutzen zu können." + "\n\n" + "Registriere dich mit **.Register**.").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, String name, String hash) {
        Map<BungieUser, Integer> tempHashMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        for(BungieUser bungieUser : Main.getCoreManager().getBungieUserManager().getBungieUsers().values()) {
            if(bungieUser.isRegistered()) {
                int score = bungieUser.getProfile(ComponentType.METRICS).getAsJsonObject("Response").getAsJsonObject("metrics").getAsJsonObject("data").getAsJsonObject("metrics").getAsJsonObject(hash).getAsJsonObject("objectiveProgress").get("progress").getAsInt();
                tempHashMap.put(bungieUser, score);
            }
        }

        HashMap sortedHashMap = this.sortByValues(tempHashMap);
        boolean end = false;

        int i = 1;
        for(Object bungieUserObject : sortedHashMap.keySet()) {
            BungieUser bungieUser = (BungieUser)bungieUserObject;

            if(i <= 5) {
                sb.append("**" + i + ".** > " + bungieUser.getUser().getName() + ": **" + this.formatInteger((int) sortedHashMap.get(bungieUserObject)) + "**" + "\n");
            }

            String lastLine = "";
            if(bungieUser.equals(targetUser)) {
                lastLine = "\n" + "*...*" + "\n\n";
                lastLine += "**" + i + ".** > " + bungieUser.getUser().getName() + ": **" + this.formatInteger((int) sortedHashMap.get(bungieUserObject)) + "**" + "\n";
                end = true;
            }

            if(i > 5 && end) {
                sb.append(lastLine);
                break;
            }

            i++;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Ranking > " + name)
                .setDescription(sb.toString());

        return embedBuilder;
    }

    private EmbedBuilder createCrucibleEmbedBuilder(BungieUser targetUser, String name, String hash, String resetHash) {
        Map<BungieUser, Integer> tempHashMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        for(BungieUser bungieUser : Main.getCoreManager().getBungieUserManager().getBungieUsers().values()) {
            if(bungieUser.isRegistered()) {
                int resets = bungieUser.getProfile(ComponentType.METRICS).getAsJsonObject("Response").getAsJsonObject("metrics").getAsJsonObject("data").getAsJsonObject("metrics").getAsJsonObject(resetHash).getAsJsonObject("objectiveProgress").get("progress").getAsInt();
                int score = bungieUser.getProfile(ComponentType.METRICS).getAsJsonObject("Response").getAsJsonObject("metrics").getAsJsonObject("data").getAsJsonObject("metrics").getAsJsonObject(hash).getAsJsonObject("objectiveProgress").get("progress").getAsInt();


                if(name.equalsIgnoreCase("Valor Points")) {
                    score = score % 2000;
                    score = score + (resets * 2000);
                }
                if(name.equalsIgnoreCase("Infamy Points")) {
                    score = score % 15000;
                    score = score + (resets * 15000);
                }

                tempHashMap.put(bungieUser, score);
            }
        }

        HashMap sortedHashMap = this.sortByValues(tempHashMap);
        boolean end = false;

        int i = 1;
        for(Object bungieUserObject : sortedHashMap.keySet()) {
            BungieUser bungieUser = (BungieUser)bungieUserObject;
            int resets = bungieUser.getProfile(ComponentType.METRICS).getAsJsonObject("Response").getAsJsonObject("metrics").getAsJsonObject("data").getAsJsonObject("metrics").getAsJsonObject(resetHash).getAsJsonObject("objectiveProgress").get("progress").getAsInt();

            if(i <= 5) {
                sb.append("**" + i + ".** > " + bungieUser.getUser().getName() + ": **" + this.formatInteger((int) sortedHashMap.get(bungieUserObject)) + "** (" + resets + " Reset" + (resets == 1 ? "" : "s") + ")" + "\n");
            }

            String lastLine = "";
            if(bungieUser.equals(targetUser)) {
                lastLine = "\n" + "*...*" + "\n\n";
                lastLine += "**" + i + ".** > " + bungieUser.getUser().getName() + ": **" + this.formatInteger((int) sortedHashMap.get(bungieUserObject)) + "** (" + resets + " Reset" + (resets == 1 ? "" : "s") + ")" + "\n";
                end = true;
            }

            if(i > 5 && end) {
                sb.append(lastLine);
                break;
            }

            i++;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Ranking > " + name)
                .setDescription(sb.toString());

        return embedBuilder;
    }

    private EmbedBuilder createTowerEmbedBuilder(BungieUser targetUser) {
        Map<BungieUser, Long> tempHashMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        for(BungieUser bungieUser : Main.getCoreManager().getBungieUserManager().getBungieUsers().values()) {
            if(bungieUser.isRegistered()) {
                long time = 0;

                List<JsonObject> tower = bungieUser.getHistoricalStats(1502633527L);
                tower.addAll(bungieUser.getHistoricalStats(3737830648L));
                tower.addAll(bungieUser.getHistoricalStats(3903562779L));

                for(JsonObject towerObject : tower) {
                    time += towerObject.getAsJsonObject("values").getAsJsonObject("activitySecondsPlayed").getAsJsonObject("basic").get("value").getAsLong();
                }

                tempHashMap.put(bungieUser, time);
            }
        }

        HashMap sortedHashMap = this.sortByValues(tempHashMap);
        boolean end = false;

        int i = 1;
        for(Object bungieUserObject : sortedHashMap.keySet()) {
            BungieUser bungieUser = (BungieUser)bungieUserObject;

            if(i <= 5) {
                sb.append("**" + i + ".** > " + bungieUser.getUser().getName() + ": **" + TimeUtil.timeToString((long)sortedHashMap.get(bungieUserObject) * 1000, true) + "**" + "\n");
            }

            String lastLine = "";
            if(bungieUser.equals(targetUser)) {
                lastLine = "\n" + "*...*" + "\n\n";
                lastLine += "**" + i + ".** > " + bungieUser.getUser().getName() + ": **" + TimeUtil.timeToString((long)sortedHashMap.get(bungieUserObject) * 1000, true) + "**" + "\n";
                end = true;
            }

            if(i > 5 && end) {
                sb.append(lastLine);
                break;
            }

            i++;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle("Ranking > Tower Playtime")
                .setDescription(sb.toString());

        return embedBuilder;
    }

    private String formatInteger(int value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("###,###.###", symbols);

        return decimalFormat.format(value);
    }

    private HashMap sortByValues(Map hashMap) {
        List list = new LinkedList(hashMap.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                return ((Comparable)((Map.Entry)(obj2)).getValue()).compareTo(((Map.Entry)(obj1)).getValue());
            }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for(Iterator iterator = list.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry)iterator.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
}
