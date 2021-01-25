package net.wolfur.rasputin.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.bungie.BungieUser;
import net.wolfur.rasputin.util.Logger;

import java.io.IOException;
import java.util.Map;

public class CodeHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Map<String,String> parameter = WebServer.queryToMap(httpExchange.getRequestURI().getQuery());

        String code = parameter.get("code");
        String state = parameter.get("state");

        BungieUser bungieUser = Main.getCoreManager().getBungieUserManager().getBungieUserFromSecurityToken(state);
        if(bungieUser != null) {
            if(bungieUser.isRegistered()) {
                Headers headers = httpExchange.getResponseHeaders();
                headers.add("Location", "https://vhost106.dein-gameserver.tech/already_registered.html");
                httpExchange.sendResponseHeaders(302, -1);
                httpExchange.close();
                return;
            }
            bungieUser.handleAuthorisationCode(code);
            Headers headers = httpExchange.getResponseHeaders();
            headers.add("Location", "https://vhost106.dein-gameserver.tech/successfull.html");
            httpExchange.sendResponseHeaders(302, -1);
            httpExchange.close();
            Logger.info("Registered new user.", true);
        } else {
            Headers headers = httpExchange.getResponseHeaders();
            headers.add("Location", "https://vhost106.dein-gameserver.tech/invalid.html");
            httpExchange.sendResponseHeaders(302, -1);
            httpExchange.close();
            Logger.error("Invalid state parameter", true);
        }
    }
}
