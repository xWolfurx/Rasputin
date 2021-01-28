package net.wolfur.rasputin.command.clan;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.character.DestinyCharacter;
import net.wolfur.rasputin.bungie.character.type.ClassType;
import net.wolfur.rasputin.bungie.clan.data.ClanData;
import net.wolfur.rasputin.bungie.clan.data.ClanRewardState;
import net.wolfur.rasputin.bungie.type.ComponentType;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.util.Utils;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Command_Clan implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUser(event.getAuthor());
        if(bungieUser.isRegistered()) {
            if(args.length == 0) {
                bungieUser.getClanUser().requestGroupV2();
                bungieUser.getClanUser().requestWeeklyRewardState();
                bungieUser.requestCharacter(ComponentType.CHARACTER_PROGRESSIONS);

                ClanData clanData = bungieUser.getClanUser().getClanData();
                ClanRewardState clanRewardState = bungieUser.getClanUser().getClanRewardState();
                Map<DestinyCharacter, JsonObject> characterData = bungieUser.getCharacterData(ComponentType.CHARACTER_PROGRESSIONS);

                event.getTextChannel().sendMessage(this.createEmbedBuilder(bungieUser, clanData, clanRewardState, characterData).build()).complete();
            } else {
                event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Verwendung: .Clan").build()).queue(message -> {
                    message.delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        } else {
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte registriere dich, um diesen Befehl nutzen zu können." + "\n\n" + "Registriere dich mit **.Register**.").build()).queue(message -> {
                message.delete().queueAfter(15, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }


    private EmbedBuilder createEmbedBuilder(BungieUser targetUser, ClanData clanData, ClanRewardState clanRewardState, Map<DestinyCharacter, JsonObject> characterData) {
        StringBuilder description = new StringBuilder();

        description.append(clanData.getMotto());
        description.append("\n");
        description.append("Members: " + clanData.getMemberCount());
        description.append("\n");
        description.append("Joined: " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(clanData.getJoinDate())));

        StringBuilder characterProgression = new StringBuilder();
        for(DestinyCharacter destinyCharacter : characterData.keySet()) {
            JsonObject data = characterData.get(destinyCharacter);

            JsonObject progression = data.getAsJsonObject("Response").getAsJsonObject("progressions").getAsJsonObject("data").getAsJsonObject("progressions").getAsJsonObject("540048094");
            characterProgression.append(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getClassType(ClassType.getClassById(destinyCharacter.getClassType()).getBetterName().toLowerCase())).getAsMention());
            characterProgression.append("Weekly XP: **" + this.formatInteger(progression.get("weeklyProgress").getAsInt()) + "**");
            characterProgression.append("\n");
        }


        int cap = 0;
        for(int i = 0; i < clanData.getClanLevel() + 1; i++) {
            if(i == 1) {
                cap += 100000;
            } else if(i != 0) {
                cap += 125000;
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setTitle(clanData.getGroupName(), "https://www.bungie.net/en/ClanV2/?groupId=" + clanData.getGroupId())
                .setDescription(description.toString())
                .addField(clanData.getClanSign() + " Weekly XP", clanData.getClanLevel() == 6 ? "Season 12 complete" : this.formatInteger(clanData.getWeeklyProgress()), true)
                .addField(clanData.getClanSign() + " Level Progress", clanData.getClanProgress() == 600000.0D ? "Max" : this.formatInteger(clanData.getClanProgress()) + "/" + this.formatInteger(cap), true)
                .addField(clanData.getClanSign() + " Level", String.valueOf(clanData.getClanLevel()), true)
                .addField("Weekly Clangrams", "Nightfall: " + this.getEngramEmote(clanRewardState.hasNightfall()) + "\n" + "Gambit: " + this.getEngramEmote(clanRewardState.hasGambit()) + "\n" + "Raid: " + this.getEngramEmote(clanRewardState.hasRaid()) + "\n" + "Crucible: " + this.getEngramEmote(clanRewardState.hasCrucible()), true)
                .addField(targetUser.getUser().getName() + "'s Personal Contribution", characterProgression.toString(), true)
                .setFooter(clanData.getClanSign() + " created " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(clanData.getTimeCreated())));
        return embedBuilder;
    }

    private String getEngramEmote(boolean value) {
        if(!value) return Utils.getEmote("engram_unlocked").getAsMention();
        return Utils.getEmote("engram_locked").getAsMention();
    }

    private String formatInteger(int value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("###,###.###", symbols);

        return decimalFormat.format(value);
    }
}
