package net.wolfur.rasputin.bungie;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.character.type.ClassType;
import net.wolfur.rasputin.bungie.character.DestinyCharacter;
import net.wolfur.rasputin.bungie.clan.ClanUser;
import net.wolfur.rasputin.bungie.information.AccountInformation;
import net.wolfur.rasputin.bungie.type.*;
import net.wolfur.rasputin.database.DatabaseUpdate;
import net.wolfur.rasputin.util.*;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BungieUser extends DatabaseUpdate {

    private final User user;

    private String securityToken;
    private String authorisationCode;

    private String accessToken;
    private long expires;
    private String refreshToken;

    private long bungieMembershipId;

    private long destinyMembershipId;
    private MembershipType destinyMembershipType;

    private ClanUser clanUser;

    private JsonObject milestonesObject;

    private List<DestinyCharacter> destinyCharacters;
    private List<JsonObject> patrolHistory;
    private List<JsonObject> raidHistory;
    private List<JsonObject> trialsHistory;

    private List<JsonObject> historicalStats;

    private Map<Long, AccountInformation> accounts;
    private Map<DestinyActivityModeType, List<JsonObject>> activityHistory;
    private Map<ComponentType, JsonObject> profileData;
    private Map<ComponentType, Map<DestinyCharacter, JsonObject>> characterData;
    private Map<VendorType, Map<ComponentType, JsonObject>> vendorData;
    private Map<DestinyDefinitionType, JsonObject> manifests;

    private Map<Long, JsonObject> triumphs;

    private Timer refreshTimer;

    public BungieUser(User user) {
        this(user, true);
    }

    public BungieUser(User user, boolean addUpdater) {
        this.user = user;

        this.accessToken = "none";
        this.expires = -1L;
        this.refreshToken = "none";
        this.bungieMembershipId = -1L;

        this.destinyMembershipId = -1L;
        this.destinyMembershipType = MembershipType.ALL;

        this.destinyCharacters = new ArrayList<>();
        this.patrolHistory = new ArrayList<>();
        this.raidHistory = new ArrayList<>();
        this.trialsHistory = new ArrayList<>();

        this.historicalStats = new ArrayList<>();

        this.accounts = new HashMap<>();
        this.activityHistory = new HashMap<>();
        this.profileData = new HashMap<>();
        this.characterData = new HashMap<>();
        this.vendorData = new HashMap<>();
        this.manifests = new HashMap<>();

        this.triumphs = new HashMap<>();
        this.loadDataAsync();

        if(addUpdater) {
            this.addToUpdater();
        }

        addReadyExecutor(() -> {
            if(isRegistered()) {
                BungieUser.this.clanUser = new ClanUser(BungieUser.this);
                BungieUser.this.startRefreshTimer();
                BungieUser.this.requestDestinyProfile();
                BungieUser.this.initializeBungieUser();
            }
        });
    }

    private void initializeBungieUser() {
        this.requestProfile(ComponentType.CHARACTERS);
        this.requestProfile(ComponentType.PROFILE_INVENTORIES);
        this.requestProfile(ComponentType.PROFILE_CURRENCIES);
        this.requestProfile(ComponentType.RECORDS);
        this.requestProfile(ComponentType.COLLECTIBLES);
        this.requestProfile(ComponentType.METRICS);
        this.requestHistoricalStats();
    }

    public void startRegistrationProcess() {
        this.sendRegistrationMessage();
    }

    public void handleAuthorisationCode(String code) {
        this.authorisationCode = code;
        this.requestAccessToken(false);
        this.requestBungieProfile();
    }

    private String createLink() {
        this.securityToken = new RandomStringGenerator(30).nextString();
        return "https://www.bungie.net/en/oauth/authorize?client_id=" + Main.getFileManager().getConfigFile().getClientId() + "&response_type=code&state=" + this.securityToken;
    }

    public JsonObject getProfile(ComponentType componentType) {
        if(!this.profileData.containsKey(componentType)) this.requestProfile(componentType);
        return this.profileData.get(componentType);
    }

    public List<JsonObject> getActivityHistory(DestinyActivityModeType destinyActivityModeType) {
        if(this.activityHistory.containsKey(destinyActivityModeType)) return this.activityHistory.get(destinyActivityModeType);
        return new ArrayList<>();
    }

    public Map<DestinyCharacter, JsonObject> getCharacterData(ComponentType componentType) {
        if(!this.characterData.containsKey(componentType)) this.requestCharacter(componentType);
        return this.characterData.get(componentType);
    }

    public boolean ownsTriumph(long triumphHashValue) {
        if(!this.profileData.containsKey(ComponentType.RECORDS)) this.requestProfile(ComponentType.RECORDS);
        JsonObject triumphData = this.profileData.get(ComponentType.RECORDS).getAsJsonObject("Response").getAsJsonObject("profileRecords").getAsJsonObject("data").getAsJsonObject("records");
        Iterable<String> triumphHashValues = triumphData.keySet();

        AtomicBoolean ownsTriumph = new AtomicBoolean(false);

        triumphHashValues.forEach(triumphValue -> {
            if(triumphValue.equalsIgnoreCase(String.valueOf(triumphHashValue))) {
                JsonArray triumphJsonArray = triumphData.getAsJsonObject(triumphValue).getAsJsonArray("objectives");
                for(int i = 0; i < triumphJsonArray.size(); i++) {
                    ownsTriumph.set(triumphJsonArray.get(i).getAsJsonObject().get("complete").getAsBoolean());
                }
            }
        });

        return ownsTriumph.get();
    }

    public boolean ownsCollectible(long collectibleHashValue) {
        if(!this.profileData.containsKey(ComponentType.COLLECTIBLES)) this.requestProfile(ComponentType.COLLECTIBLES);
        JsonObject collectibleData = this.profileData.get(ComponentType.COLLECTIBLES).getAsJsonObject("Response").getAsJsonObject("profileCollectibles").getAsJsonObject("data").getAsJsonObject("collectibles");

        try {
            int state = collectibleData.getAsJsonObject(String.valueOf(collectibleHashValue)).get("state").getAsInt();
            return ((state & 1) != 1);
        } catch (NullPointerException e) {
            Logger.error("Can´t find collectible '" + collectibleHashValue + "' for " + this.getUser().getName() + ".", true);
            return false;
        }
    }

    public List<JsonObject> getHistoricalStats(long activityHash) {
        List<JsonObject> historicalStats = new ArrayList<>();
        for(JsonObject activityObject : this.historicalStats) {
            long hashValue = activityObject.get("activityHash").getAsLong();
            if(hashValue == activityHash) {
                historicalStats.add(activityObject);
            }
        }
        return historicalStats;
    }

    public JsonObject getVendor(VendorType vendorType, ComponentType componentType) {
        if(!this.vendorData.containsKey(vendorType)) this.requestVendor(vendorType, componentType);
        Map<ComponentType, JsonObject> vendorComponentData = this.vendorData.get(vendorType);
        if(!vendorComponentData.containsKey(componentType)) {
            this.requestVendor(vendorType, componentType);
            vendorComponentData = this.vendorData.get(vendorType);
        }
        return vendorComponentData.get(componentType);
    }

    public int getCurrenciesQuantity(CurrenciesType currenciesType) {
        JsonObject currenciesObject = this.getProfile(ComponentType.PROFILE_CURRENCIES);
        JsonArray currenciesArray = currenciesObject.getAsJsonObject("Response").getAsJsonObject("profileCurrencies").getAsJsonObject("data").getAsJsonArray("items");

        int quantity = 0;

        for(int i = 0; i < currenciesArray.size(); i++) {
            JsonObject currencyObject = currenciesArray.get(i).getAsJsonObject();
            long itemHash = currencyObject.get("itemHash").getAsLong();
            if(itemHash == currenciesType.getItemHash()) {
                quantity = currencyObject.get("quantity").getAsInt();
            }
        }

        return quantity;
    }

    public int getMaterialQuantity(MaterialType materialType) {
        JsonObject inventoryObject = this.getProfile(ComponentType.PROFILE_INVENTORIES);
        JsonArray inventoryArray = inventoryObject.getAsJsonObject("Response").getAsJsonObject("profileInventory").getAsJsonObject("data").getAsJsonArray("items");

        int quantity = 0;

        for (int i = 0; i < inventoryArray.size(); i++) {
            JsonObject itemObject = inventoryArray.get(i).getAsJsonObject();
            long itemHash = itemObject.get("itemHash").getAsLong();
            if (itemHash == materialType.getItemHash()) {
                quantity = itemObject.get("quantity").getAsInt();
            }
        }

        return quantity;
    }

    public JsonObject getManifest(DestinyDefinitionType destinyDefinitionType) {
        if(!this.manifests.containsKey(destinyDefinitionType)) this.requestManifest(destinyDefinitionType);
        return this.manifests.get(destinyDefinitionType);
    }

    public int getRaidCompletions(String activityHash) {
        JsonObject metrics = this.getProfile(ComponentType.METRICS);
        return metrics.getAsJsonObject("Response").getAsJsonObject("metrics").getAsJsonObject("data").getAsJsonObject("metrics").getAsJsonObject(activityHash).getAsJsonObject("objectiveProgress").get("progress").getAsInt();
    }

    public boolean isRegistered() {
        return this.getBungieMembershipId() != -1L;
    }

    private void handleBungieData(String accessToken, long expires, String refreshToken, long bungieMembershipId) {
        this.accessToken = accessToken;
        this.expires = expires;
        this.refreshToken = refreshToken;
        this.bungieMembershipId = bungieMembershipId;

        this.startRefreshTimer();
        this.saveDataAsync();
    }

    private void requestAccessToken(boolean refresh) {
        try {
            String url = "https://www.bungie.net/platform/app/oauth/token/";

            String params = refresh ? "grant_type=refresh_token&refresh_token=" + this.getRefreshToken() : "grant_type=authorization_code&code=" + this.getAuthorisationCode();
            byte[] postData = params.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)obj.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());
            connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((Main.getFileManager().getConfigFile().getClientId() + ":" + Main.getFileManager().getConfigFile().getClientSecret()).getBytes()));
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));

            try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
                dataOutputStream.write(postData);
            }

            int responseCode = connection.getResponseCode();
            Logger.info("Request access token for user " + this.getUser().getName() + ".", false);
            Logger.info("Sending 'POST' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);

            if(responseCode != 200) {
                Logger.error("A request to Bungie.net was refused. (Response-Code: " + responseCode + ")", true);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = "";
            StringBuilder response = new StringBuilder();

            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

            String accessToken = jsonObject.get("access_token").getAsString();
            long expires = (System.currentTimeMillis() + TimeUtil.parseTime(jsonObject.get("expires_in").getAsString() + "s"));
            String refreshToken = jsonObject.get("refresh_token").getAsString();
            long bungieMembershipId = (refresh ? this.getBungieMembershipId() : jsonObject.get("membership_id").getAsLong());

            if(!refresh) {
                this.destinyMembershipType = MembershipType.ALL;
            }

            this.handleBungieData(accessToken, expires, refreshToken, bungieMembershipId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestBungieProfile() {
        try {
            String url = "https://www.bungie.net/platform/user/getmembershipsbyid/" + this.getBungieMembershipId() + "/" + MembershipType.BUNGIE_NEXT.getId() + "/";

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());
            connection.setRequestProperty("Authorization", "Bearer " + this.getAccessToken());

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);
            Logger.info("MembershipType: " + MembershipType.BUNGIE_NEXT.name(), false);

            if(responseCode != 200) {
                Logger.error("A request to Bungie.net was refused. (Response-Code: " + responseCode + ")", true);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = "";
            StringBuilder response = new StringBuilder();

            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

            JsonArray profileAccountArray = jsonObject.getAsJsonObject("Response").getAsJsonArray("destinyMemberships");
            boolean hasPrimaryMembershipId = jsonObject.getAsJsonObject("Response").has("primaryMembershipId");

            for(int i = 0; i < profileAccountArray.size(); i++) {
                JsonObject profileObject = profileAccountArray.get(i).getAsJsonObject();
                String displayName = profileObject.get("displayName").getAsString();
                int membershipType = profileObject.get("membershipType").getAsInt();
                long membershipId = profileObject.get("membershipId").getAsLong();

                this.accounts.put(membershipId, new AccountInformation(displayName, MembershipType.getById(membershipType), membershipId));
            }

            if(hasPrimaryMembershipId) {
                this.destinyMembershipId = jsonObject.getAsJsonObject("Response").get("primaryMembershipId").getAsLong();
            } else if(this.accounts.size() > 0) {
                this.destinyMembershipId = this.accounts.keySet().iterator().next();
            } else {
                this.sendNoDestinyProfileFoundMessage();
                return;
            }
            this.destinyMembershipType = this.accounts.get(this.getDestinyMembershipId()).getMembershipType();

            AccountInformation primaryAccount = this.accounts.get(this.destinyMembershipId);
            this.sendCompletedRegistrationMessage(primaryAccount);
            this.saveDataAsync();

            this.requestDestinyProfile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestManifest(DestinyDefinitionType destinyDefinitionType) {
        try {
            String url = "https://www.bungie.net/platform/destiny2/manifest/";

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

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

            String definitionUrl = jsonObject.getAsJsonObject("Response").getAsJsonObject("jsonWorldComponentContentPaths").getAsJsonObject("en").getAsJsonPrimitive(destinyDefinitionType.getPathName()).getAsString();
            this.requestDefinitionManifest(destinyDefinitionType, definitionUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestDefinitionManifest(DestinyDefinitionType destinyDefinitionType, String definitionUrl) {
        try {
            String url = "https://www.bungie.net" + definitionUrl;

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);
            Logger.info("DestinyDefinitionType: " + destinyDefinitionType.name(), false);

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

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

            this.manifests.put(destinyDefinitionType, jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestProfile(ComponentType componentType) {
        try {
            String url = "https://www.bungie.net/platform/destiny2/" + this.getDestinyMembershipType().getId() + "/profile/" + this.getDestinyMembershipId() + "/?components=" + componentType.getComponentId();

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());
            connection.setRequestProperty("Authorization", "Bearer " + this.getAccessToken());

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);
            Logger.info("Component-Type: " + componentType.getBetterName(), false);

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

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

            this.profileData.put(componentType, jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestCharacter(ComponentType componentType) {
        Map<DestinyCharacter, JsonObject> data = new HashMap<>();
        for(DestinyCharacter destinyCharacter : this.getDestinyCharacters()) {
            try {
                String url = "https://www.bungie.net/platform/destiny2/" + this.getDestinyMembershipType().getId() + "/profile/" + this.getDestinyMembershipId() + "/character/" + destinyCharacter.getCharacterId() + "/?components=" + componentType.getComponentId();

                URL obj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());
                connection.setRequestProperty("Authorization", "Bearer " + this.getAccessToken());

                int responseCode = connection.getResponseCode();
                Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
                Logger.info("Response Code: " + responseCode, false);
                Logger.info("Component-Type: " + componentType.getBetterName(), false);

                if (responseCode != 200) {
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

                JsonParser parser = new JsonParser();
                JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

                data.put(destinyCharacter, jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.characterData.put(componentType, data);
    }

    public void requestDestinyProfile() {
        this.destinyCharacters.clear();
        try {
            String url = "https://www.bungie.net/platform/destiny2/" + this.getDestinyMembershipType().getId() + "/profile/" + this.getDestinyMembershipId() + "/?components=200";

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());
            connection.setRequestProperty("Authorization", "Bearer " + this.getAccessToken());

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            String response = "";

            while ((inputLine = in.readLine()) != null) {
                response += inputLine;
            }

            in.close();

            JsonParser parser = new JsonParser();
            JsonObject destinyProfile = (JsonObject) parser.parse(response);

            JsonObject destinyProfileData = destinyProfile.getAsJsonObject("Response").getAsJsonObject("characters").getAsJsonObject("data");
            Iterable<String> keys = destinyProfileData.keySet();

            for(String characterId : keys) {
                JsonObject characterData = destinyProfileData.getAsJsonObject(characterId);

                JsonObject characterStatsData = characterData.getAsJsonObject("stats");
                Iterable<String> statsKeys = characterStatsData.keySet();

                Map<String, Integer> stats = new HashMap<>();
                for(String statsKey : statsKeys) {
                    int value = characterStatsData.getAsJsonPrimitive(statsKey).getAsInt();
                    stats.put(statsKey, value);
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                long dateLastPlayed = -1L;

                try {
                    dateLastPlayed = sdf.parse(characterData.get("dateLastPlayed").getAsString()).getTime();
                } catch (ParseException e) {
                    Logger.error("An error occurred while parsing string to long. (DestinyCharacter::DateLastPlayed::" + this.getUser().getName() + ")", true);
                }

                int minutesPlayedTotal = characterData.get("minutesPlayedTotal").getAsInt();
                int lightLevel = characterData.get("light").getAsInt();
                int raceType = characterData.get("raceType").getAsInt();
                int genderType = characterData.get("genderType").getAsInt();
                int classType = characterData.get("classType").getAsInt();
                String emblemPath = characterData.get("emblemPath").getAsString();

                DestinyCharacter destinyCharacter = new DestinyCharacter(Long.parseLong(characterId), dateLastPlayed, minutesPlayedTotal, lightLevel, raceType, genderType, classType, emblemPath);
                this.destinyCharacters.add(destinyCharacter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestHistoricalStats() {
        this.historicalStats.clear();
        for(DestinyCharacter destinyCharacter : this.destinyCharacters) {
            Logger.info("Collecting historical statistics for " + this.getUser().getName() + ". (Character: " + ClassType.getClassById(destinyCharacter.getClassType()).getBetterName() + ")", false);
            try {
                String url = "https://www.bungie.net/platform/destiny2/" + this.getDestinyMembershipType().getId() + "/account/" + this.getDestinyMembershipId() + "/character/" + destinyCharacter.getCharacterId() + "/stats/AggregateActivityStats/";

                URL obj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());
                connection.setRequestProperty("Authorization", "Bearer " + this.getAccessToken());

                int responseCode = connection.getResponseCode();

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

                JsonParser parser = new JsonParser();
                JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

                JsonArray jsonArray = jsonObject.getAsJsonObject("Response").getAsJsonArray("activities");
                for(int i = 0; i < jsonArray.size(); i++) {
                    JsonObject historicalStatObject = jsonArray.get(i).getAsJsonObject();
                    this.historicalStats.add(historicalStatObject);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Logger.info("Collected " + this.historicalStats.size() + " historical statistics for " + this.getUser().getName() + " from Bungie.net.", false);
    }

    public void requestDestinyActivityHistory(DestinyActivityModeType destinyActivityModeType, int count, boolean loop) {
        List<JsonObject> activityHistoryData = new ArrayList<>();
        if(this.activityHistory.containsKey(destinyActivityModeType)) this.activityHistory.remove(destinyActivityModeType);

        for(DestinyCharacter destinyCharacter : this.destinyCharacters) {
            Logger.info("Collecting '" + destinyActivityModeType.name() + "' history for " + this.getUser().getName() + ". (Character: " + ClassType.getClassById(destinyCharacter.getClassType()).getBetterName() + ")", false);
            boolean running = true;
            int page = 0;

            while(running) {
                try {
                    String url = "https://www.bungie.net/platform/destiny2/" + this.getDestinyMembershipType().getId() + "/account/" + this.getDestinyMembershipId() + "/character/" + destinyCharacter.getCharacterId() + "/stats/activities/?mode=" + destinyActivityModeType.getMode() + "&count=" + count + "&page=" + page;

                    URL obj = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());
                    connection.setRequestProperty("Authorization", "Bearer " + this.getAccessToken());

                    int responseCode = connection.getResponseCode();

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

                    JsonParser parser = new JsonParser();
                    JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

                    JsonArray activitiesArray = jsonObject.getAsJsonObject("Response").getAsJsonArray("activities");
                    if(activitiesArray == null) {
                        running = false;
                    } else {
                        for (int i = 0; i < activitiesArray.size(); i++) {
                            JsonObject patrolObject = activitiesArray.get(i).getAsJsonObject();
                            activityHistoryData.add(patrolObject);
                        }

                        if (activitiesArray.size() < count) running = false;
                        if (!loop) running = false;

                        page++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Logger.info("Collected " + activityHistoryData.size() + " '" + destinyActivityModeType.name() + "' activities for " + this.getUser().getName() + " from Bungie.net.", false);
        this.activityHistory.put(destinyActivityModeType, activityHistoryData);
    }

    public void requestVendor(VendorType vendorType, ComponentType componentType) {
        if(this.destinyCharacters.size() <= 0) {
            Logger.error("No Destiny 2 characters found for " + this.getUser().getName() + ".", true);
            return;
        }

        long characterId = this.destinyCharacters.get(0).getCharacterId();
        try {
            String url = "https://www.bungie.net/platform/destiny2/" + this.getDestinyMembershipType().getId() + "/profile/" + this.getDestinyMembershipId() +  "/character/" + characterId + "/vendors/" + vendorType.getVendorId() + "/?components=" + componentType.getComponentId();

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());
            connection.setRequestProperty("Authorization", "Bearer " + this.getAccessToken());

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);
            Logger.info("Vendor-Type: " + vendorType.name(), false);
            Logger.info("Component-Type: " + componentType.getBetterName(), false);

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

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

            Map<ComponentType, JsonObject> vendorComponentData = new HashMap<>();
            vendorComponentData.put(componentType, jsonObject);

            this.vendorData.put(vendorType, vendorComponentData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject requestPostGameCarnageReport(long instanceId) {
        try {
            String url = "https://stats.bungie.net/platform/Destiny2/Stats/PostGameCarnageReport/" + instanceId + "/";

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-API-KEY", Main.getFileManager().getConfigFile().getAPIKey());
            connection.setRequestProperty("Authorization", "Bearer " + this.getAccessToken());

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Bungie.net: " + url, false);
            Logger.info("Response Code: " + responseCode, false);

            if(responseCode != 200) {
                Logger.error("A request to Bungie.net was refused. (Response-Code: " + responseCode + ")", true);
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = (JsonObject) parser.parse(response.toString());

            return jsonObject;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String requestXurLocation() {
        try {
            String url = "https://paracausal.science/xur/current.json";

            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();


            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            int responseCode = connection.getResponseCode();
            Logger.info("Sending 'GET' request to Paracausal: " + url, false);
            Logger.info("Response Code: " + responseCode, false);

            if(responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                String response = "";

                while ((inputLine = in.readLine()) != null) {
                    response += inputLine;
                }

                in.close();

                JsonParser parser = new JsonParser();
                JsonObject jsonObject = (JsonObject) parser.parse(response);

                return jsonObject.get("locationName").getAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void requestMilestones() {
        try {
            String url = "https://www.bungie.net/platform/Destiny2/Milestones/";

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

            JsonParser parser = new JsonParser();
            this.milestonesObject = (JsonObject) parser.parse(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        return this.user;
    }

    public String getAuthorisationCode() {
        return this.authorisationCode;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public long getExpires() {
        return this.expires;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public String getSecurityToken() {
        return this.securityToken;
    }

    public long getBungieMembershipId() {
        return this.bungieMembershipId;
    }

    public long getDestinyMembershipId() {
        return this.destinyMembershipId;
    }

    public MembershipType getDestinyMembershipType() {
        return this.destinyMembershipType;
    }

    public List<DestinyCharacter> getDestinyCharacters() {
        return this.destinyCharacters;
    }

    public Map<Long, JsonObject> getTriumphs() {
        return this.triumphs;
    }

    public List<JsonObject> getTrialsHistory() {
        return this.trialsHistory;
    }

    public ClanUser getClanUser() {
        return this.clanUser;
    }

    public JsonObject getMilestonesObject() {
        return this.milestonesObject;
    }

    private void startRefreshTimer() {
        long delay = this.getExpires() - System.currentTimeMillis();
        if(delay <= 0L) {
            Logger.warning("Access token expired for user " + this.getUser().getName() + ".", false);
            this.requestAccessToken(true);
            delay = this.getExpires() - System.currentTimeMillis();
        }

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                BungieUser.this.requestAccessToken(true);
            }
        };

        this.refreshTimer = new Timer();
        this.refreshTimer.schedule(timerTask, delay);
    }

    @Override
    public void saveData() {
        try {
            PreparedStatement stCheck = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `bungie_users` WHERE `discord_id` = ?");
            stCheck.setLong(1, this.getUser().getIdLong());
            ResultSet rs = Main.getSQLManager().executeQuery(stCheck);
            if(!rs.next()) {
                PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("INSERT INTO `bungie_users` (discord_name, discord_id, bungie_membership_id, destiny_membership_id, destiny_membership_type, access_token, expires, refresh_token) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                st.setString(1, this.getUser().getName());
                st.setLong(2, this.getUser().getIdLong());
                st.setLong(3, -1L);
                st.setLong(4, -1);
                st.setString(5, MembershipType.ALL.name());
                st.setString(6, "none");
                st.setLong(7, -1L);
                st.setString(8, "none");
                Main.getSQLManager().executeUpdate(st);
            } else {
                PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("UPDATE `bungie_users` SET `discord_name` = ?, `bungie_membership_id` = ?, `destiny_membership_id` = ?, `destiny_membership_type` = ?, `access_token` = ?, `expires` = ?, `refresh_token` = ? WHERE `discord_id` = ?");
                st.setString(1, this.getUser().getName());
                st.setLong(2, this.getBungieMembershipId());
                st.setLong(3, this.getDestinyMembershipId());
                st.setString(4, this.getDestinyMembershipType().name());
                st.setString(5, Encryption.encodeString(this.getAccessToken()));
                st.setLong(6, this.getExpires());
                st.setString(7, Encryption.encodeString(this.getRefreshToken()));
                st.setLong(8, this.getUser().getIdLong());
                Main.getSQLManager().executeUpdate(st);
            }
            rs.close();
            stCheck.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDataAsync() {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                BungieUser.this.saveData();
            }
        });
    }

    @Override
    public void loadData() {
        try {
            PreparedStatement st = Main.getSQLManager().getConnection().prepareStatement("SELECT * FROM `bungie_users` WHERE `discord_id` = ?");
            st.setLong(1, this.getUser().getIdLong());
            ResultSet rs = Main.getSQLManager().executeQuery(st);
            if(!rs.next()) {
                this.saveData();
            } else {
                this.bungieMembershipId = rs.getLong("bungie_membership_id");
                this.destinyMembershipId = rs.getLong("destiny_membership_id");
                this.destinyMembershipType = MembershipType.getByName(rs.getString("destiny_membership_type"));
                this.accessToken = Encryption.decodeString(rs.getString("access_token"));
                this.expires = rs.getLong("expires");
                this.refreshToken = Encryption.decodeString(rs.getString("refresh_token"));
            }
            rs.close();
            st.close();
            this.setReady(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadDataAsync() {
        Main.getSQLManager().getAsyncHandler().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                BungieUser.this.loadData();
            }
        });
    }

    private void sendRegistrationMessage() {
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.ORANGE)
                .setTitle("Klicke hier, um dich zu registrieren", this.createLink())
                .setDescription("\n" + "**Registrierungsprozess initialisiert**" + "\n" + "\n" +
                        "Willkommen Hüter, mein Name ist Rasputin und ich werde dich durch diesen Prozess begleiten." + "\n" +
                        "Klicke auf den Link, um dich zu autorisieren.")
                .setFooter("Protocol " + new RandomStringGenerator(7).nextString(), Main.getJDA().getSelfUser().getAvatarUrl())
                .setThumbnail("http://vhost106.dein-gameserver.tech/rasputin-icon.png");
        this.user.openPrivateChannel().queue(channel -> {
            channel.sendMessage(embedBuilder.build()).queue();
        });
    }

    private void sendCompletedRegistrationMessage(AccountInformation primaryAccount) {
        StringBuilder sb = new StringBuilder();
        for(AccountInformation accountInformation : this.accounts.values()) {
            if(accountInformation == primaryAccount) continue;

            sb.append(Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAccountType(accountInformation.getMembershipType().getId())).getAsMention() + " - " + accountInformation.getDisplayName()).append("\n");
        }

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.GREEN)
                .setTitle("Registrierungsprozess abgeschlossen")
                .setThumbnail("http://vhost106.dein-gameserver.tech/rasputin-icon.png")
                .setDescription("\n" + "Registrierung erfolgreich." + "\n" +
                        "Zugriff auf weiter Funktionen wurde freigegeben." + "\n" + "\n" +
                        "**Primärer verlinkter Account: **" + "\n" +
                        Utils.getEmote(Main.getFileManager().getEmoteDefinitionFile().getAccountType(primaryAccount.getMembershipType().getId())).getAsMention() + " - " + primaryAccount.getDisplayName() + "\n\n" +
                        "**Weitere Accounts:**" + "\n" +
                        (this.accounts.size() == 1 ? "Keine" : sb.toString()));
        this.user.openPrivateChannel().queue(channel -> {
            channel.sendMessage(embedBuilder.build()).queue();
        });
    }

    private void sendNoDestinyProfileFoundMessage() {
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.RED)
                .setTitle("Fehler in der Registrierung...")
                .setThumbnail("http://vhost106.dein-gameserver.tech/rasputin-icon.png")
                .setDescription("\n" + "Bei der Registrierung ist ein Fehler aufgetreten." + "\n" +
                        "Es konnten keine verlinkten Destiny-Profile gefunden werden." + "\n" + "\n" +
                        "**Bitte melde dich bei Wolfur#9108**");
        this.user.openPrivateChannel().queue(channel -> {
            channel.sendMessage(embedBuilder.build()).queue();
        });
    }

    public boolean hasRole(Member member, long roleId) {
        for(Role role : member.getRoles()) {
            if(role.getIdLong() == roleId) {
                return true;
            }
        }
        return false;
    }

    public void addRole(long roleId) {
        Main.getGuild().addRoleToMember(this.getUser().getIdLong(), Main.getGuild().getRoleById(roleId)).complete();
    }

    public void removeRole(long roleId) {
        Main.getGuild().removeRoleFromMember(this.getUser().getIdLong(), Main.getGuild().getRoleById(roleId)).complete();
    }

}
