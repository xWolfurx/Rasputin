package net.wolfur.rasputin.core;

import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.permission.CommandPermission;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    public static final CommandParser parser = new CommandParser();
    public static Map<String, Command> commands = new HashMap<>();

    public static void handleCommand(CommandParser.CommandContainer commandContainer) {
        if(Main.getReloadTask().isReloading()) {
            commandContainer.event.getTextChannel().sendMessage("Commands are disabled while reloading.").complete();
            return;
        }
        if(Main.isMaintenance() && (!commandContainer.invoke.equalsIgnoreCase("raid") || !commandContainer.invoke.equalsIgnoreCase("maintenance"))) {
            commandContainer.event.getTextChannel().sendMessage("Commands are disabled while Bungie.net maintenance.").complete();
            return;
        }
        if(CommandHandler.commands.containsKey(commandContainer.invoke)) {
            CommandPermission commandPermission = Main.getFileManager().getPermissionFile().getCommandPermission(CommandHandler.commands.get(commandContainer.invoke));
            if(commandPermission.hasPermission(commandContainer.event.getMember())) {
                boolean safe = CommandHandler.commands.get(commandContainer.invoke).called(commandContainer.args, commandContainer.event);
                if(!safe) {
                    CommandHandler.commands.get(commandContainer.invoke).action(commandContainer.args, commandContainer.event);
                    CommandHandler.commands.get(commandContainer.invoke).executed(false, commandContainer.event);
                } else {
                    CommandHandler.commands.get(commandContainer.invoke).executed(true, commandContainer.event);
                }
            } else {
                commandContainer.event.getTextChannel().sendMessage("You do not have permission to perform this command.").complete();
            }
        } else {
            commandContainer.event.getTextChannel().sendMessage("No command called '" + commandContainer.invoke + "' found.").complete();
        }
    }
}
