package net.wolfur.rasputin.bungie.clan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.bungie.clan.data.ClanData;
import net.wolfur.rasputin.bungie.clan.data.ClanRewardState;
import net.wolfur.rasputin.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ClanUser {

    private final BungieUser bungieUser;
    private ClanData clanData;
    private ClanRewardState clanRewardState;

    public ClanUser(BungieUser bungieUser) {
        this.bungieUser = bungieUser;
        this.requestGroupV2();
    }

    public BungieUser getBungieUser() {
        return this.bungieUser;
    }

    public ClanData getClanData() {
        return this.clanData;
    }

    public ClanRewardState getClanRewardState() {
        return this.clanRewardState;
    }

    public void requestGroupV2() {
        try {
            String url = "https://www.bungie.net/platform/GroupV2/User/" + this.getBungieUser().getDestinyMembershipType().getId() + "/" + this.getBungieUser().getDestinyMembershipId() + "/0/1/";

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);

            if(responseCode != 200) {
                Logger.error("A request to Bungie.net was refused. (Response-Code: " + responseCode + ")", true);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();

            JsonArray jsonArray = jsonObject.getAsJsonObject("Response").getAsJsonArray("results");
            for(int i = 0; i < jsonArray.size(); i++) {
                JsonObject clanObject = jsonArray.get(i).getAsJsonObject();

                long groupId = clanObject.getAsJsonObject("group").get("groupId").getAsLong();
                String groupName = clanObject.getAsJsonObject("group").get("name").getAsString();
                String creationDate = clanObject.getAsJsonObject("group").get("creationDate").getAsString();
                int memberCount = clanObject.getAsJsonObject("group").get("memberCount").getAsInt();
                String clanMotto = clanObject.getAsJsonObject("group").get("motto").getAsString();
                int clanLevel = clanObject.getAsJsonObject("group").getAsJsonObject("clanInfo").getAsJsonObject("d2ClanProgressions").getAsJsonObject("584850370").get("level").getAsInt();
                String clanSign = clanObject.getAsJsonObject("group").getAsJsonObject("clanInfo").get("clanCallsign").getAsString();
                int clanProgress = clanObject.getAsJsonObject("group").getAsJsonObject("clanInfo").getAsJsonObject("d2ClanProgressions").getAsJsonObject("584850370").get("currentProgress").getAsInt();
                int weeklyProgress = clanObject.getAsJsonObject("group").getAsJsonObject("clanInfo").getAsJsonObject("d2ClanProgressions").getAsJsonObject("584850370").get("weeklyProgress").getAsInt();

                String joinDate = clanObject.getAsJsonObject("member").get("joinDate").getAsString();

                try {
                    this.clanData = new ClanData(groupId, groupName, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(creationDate).getTime(), clanMotto, memberCount, clanLevel, clanSign, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(joinDate).getTime(), clanProgress, weeklyProgress);
                } catch (ParseException e) {
                    Logger.error("An error occurred while parsing clan time.", true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestWeeklyRewardState() {
        try {
            String url = "https://www.bungie.net/platform/Destiny2/Clan/" + this.getClanData().getGroupId() + "/WeeklyRewardState/";

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);

            if(responseCode != 200) {
                Logger.error("A request to Bungie.net was refused. (Response-Code: " + responseCode + ")", true);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();

            JsonArray jsonArray = jsonObject.getAsJsonObject("Response").getAsJsonArray("rewards");
            boolean gambit = false, nightfall = false, raid = false, crucible = false;

            for(int i = 0; i < jsonArray.size(); i++) {
                JsonObject rewardObject = jsonArray.get(i).getAsJsonObject();
                long rewardCategoryHash = rewardObject.get("rewardCategoryHash").getAsLong();

                if(rewardCategoryHash == 1064137897L) {
                    JsonArray rewardEntries = rewardObject.getAsJsonArray("entries");
                    for(int j = 0; j < rewardEntries.size(); j++) {
                        JsonObject entry = rewardEntries.get(i).getAsJsonObject();
                        long rewardEntryHash = entry.get("rewardEntryHash").getAsLong();

                        if(rewardEntryHash == 248695599L) {
                            gambit = entry.get("earned").getAsBoolean();
                        }
                        if(rewardEntryHash == 964120289L) {
                            crucible = entry.get("earned").getAsBoolean();
                        }
                        if(rewardEntryHash == 2043403989L) {
                            raid = entry.get("earned").getAsBoolean();
                        }
                        if(rewardEntryHash == 3789021730L) {
                            nightfall = entry.get("earned").getAsBoolean();
                        }
                    }
                }
            }
            this.clanRewardState = new ClanRewardState(gambit, nightfall, raid, crucible);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestInvitedUsers() {
        try {
            String url = "https://www.bungie.net/platform/GroupV2/" + this.getClanData().getGroupId() + "/Members/InvitedIndividuals/";

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);

            if(responseCode != 200) {
                Logger.error("A request to Bungie.net was refused. (Response-Code: " + responseCode + ")", true);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
