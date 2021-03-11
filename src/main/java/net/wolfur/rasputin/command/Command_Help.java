package net.wolfur.rasputin.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.core.CommandHandler;
import net.wolfur.rasputin.permission.CommandPermission;

import java.awt.*;

public class Command_Help implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(args.length == 0) {
            event.getTextChannel().sendMessage(this.getEmbedBuilder("general", event.getMember()).build()).queue();
        } else {
            event.getTextChannel().sendMessage(this.getEmbedBuilder(args[0], event.getMember()).build()).queue();
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

    private EmbedBuilder getEmbedBuilder(String type, Member member) {
        StringBuilder sb = new StringBuilder();

        if(type.equalsIgnoreCase("general")) {
            sb.append("**General:**").append("\n");
            sb.append(" - .Register » Verknüpfe dich mit Rasputin").append("\n");
            sb.append("**Vendors:**").append("\n");
            sb.append(" - .Spider » Informationen über die Angebote von Spider.").append("\n");
            sb.append(" - .Xur » Informationen über die Angebote von Xûr.").append("\n");
            sb.append(" - .Gunsmith » Informationen über die Angebote von Banshee-44.").append("\n");
            sb.append("**Statistics:**").append("\n");
            sb.append(" - .DSC [@Spieler] » Statistiken vom Deep Stone Crypt Raid.").append("\n");
            sb.append(" - .GoS [@Spieler] » Statistiken vom Garden of Salvation Raid").append("\n");
            sb.append(" - .LW [@Spieler] » Statistiken vom Last Wish Raid").append("\n");
            sb.append(" - .Pit [@Spieler] » Statistiken von der Grube der Ketzerei.").append("\n");
            sb.append(" - .Prophecy [@Spieler] » Statistiken vom Dungeon 'Prophezeiung'.").append("\n");
            sb.append(" - .Throne [@Spieler] » Statistiken vom Zerbrochenen Thron.").append("\n");
            sb.append(" - .Characters [@Spieler] » Informationen über die Charaktere").append("\n");
            sb.append(" - .Trials [@Spieler] » Statistiken von Trials of Osiris").append("\n");
            sb.append(" - .Loadout [@Spieler] » Ausrüstung des zuletzt gespielten Charakter.").append("\n");
            sb.append(" - .Last <Activity> [@Spieler] » Statistiken der letzten Aktivität.").append("\n");
            sb.append(" - .Ranking <Type> » Clan-Internes Ranking.").append("\n");
            sb.append(" - .Fireteam [@Spieler] » Zeigt den aktuellen Einsatztrupp an.").append("\n");
            sb.append(" - .Current [@Spieler] » Zeigt die aktuelle Aktivität an.").append("\n");
            sb.append("**Clan:**").append("\n");
            sb.append(" - .Clan » Statistiken über den aktuellen Clan.").append("\n");
            sb.append("**Administration:**").append("\n");
            sb.append(this.commandWithPermission("ban", member, " - .Ban <Id> [Grund] » Sperrt einen Spieler.")).append("\n");
            sb.append(this.commandWithPermission("unban", member, " - .Unban <Id> » Entsperrt einen Spieler.")).append("\n");
            sb.append(this.commandWithPermission("tempban", member, " - .Tempban <Id> <Zeit> [Grund] » Sperrt einen Spieler temporär.")).append("\n");
        } else if(type.equalsIgnoreCase("last")) {
            sb.append("**Command: Last**").append("\n");
            sb.append(" - .Last Raid » Statistiken vom letzten Raid.").append("\n");
            sb.append(" - .Last Patrol » Statistiken von der letzten Patrouille.").append("\n");
            sb.append(" - .Last Nightfall » Statistiken von dem letzten Dämmerungsstrike.").append("\n");
            sb.append(" - .Last Dungeon » Statistiken von dem letzten Dungeon.").append("\n");
            sb.append(" - .Last Gambit » Statistiken von der letzten Gambit-Runde.").append("\n");
        } else if(type.equalsIgnoreCase("ranking")) {
            sb.append("**Command: Ranking**").append("\n");
            sb.append(" - .Ranking Triumph » Ranking der aktiven Triumph-Punkte.").append("\n");
            sb.append(" - .Ranking TotalTriumph » Ranking der totalen Triumph-Punkte.").append("\n");
            sb.append(" - .Ranking Glory » Ranking der Glory-Punkte.").append("\n");
            sb.append(" - .Ranking Valor » Ranking der Valor-Punkte.").append("\n");
            sb.append(" - .Ranking Infamy » Ranking der Infamy-Punkte.").append("\n");
            sb.append(" - .Ranking Tower » Ranking der Spielzeit auf dem Turm").append("\n");
        } else {
            sb.append("Bitte gebe einen gültigen Befehl an.");
        }

        /**sb.append("**Raid-Sperre:**").append("\n");
        sb.append(" - .ActiveBans » Listet alle aktiven Sperren auf.").append("\n");
        sb.append(" - .Ban <Spieler> [Grund] » Erstellt eine permanente Raid-Sperre.").append("\n");
        sb.append(" - .Tempban <Spieler> <Zeit> [Grund] » Erstellt eine temporäre Raid-Sperre.").append("\n");
        sb.append(" - .Unban <Spieler> » Entfernt eine Raid-Sperre.").append("\n").append("\n");
        sb.append("**Administration:**").append("\n");
        sb.append(" - .ForceUpdate » Erzwingt ein Datenbankupdate.").append("\n");
        sb.append(" - .Refresh » Erneuert den Bungie-API-Token manuell.").append("\n");
        sb.append(" - .RefreshRaids » Erzwingt ein Update der Raids aus der Datenbank.").append("\n");
        sb.append(" - .RefreshStats » Erzwingt ein Update der Stats aus der Datenbank.").append("\n");
        sb.append(" - .Reset <Daily|Weekly> » Führt den Daily/Weekly - Reset manuell aus.").append("\n");*/

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.YELLOW)
                .setDescription(sb.toString());
        return embedBuilder;
    }

    private String commandWithPermission(String command, Member member, String description) {
        CommandPermission commandPermission = Main.getFileManager().getPermissionFile().getCommandPermission(CommandHandler.commands.get(command));
        if(commandPermission.hasPermission(member)) {
            return description;
        }
        return "";
    }

}
