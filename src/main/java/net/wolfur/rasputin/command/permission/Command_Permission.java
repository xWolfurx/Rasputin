package net.wolfur.rasputin.command.permission;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.Main;
import net.wolfur.rasputin.core.Command;
import net.wolfur.rasputin.core.CommandHandler;
import net.wolfur.rasputin.permission.CommandPermission;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Command_Permission implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }
    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        String commandName = args[0];
        if(CommandHandler.commands.containsKey(commandName)) {
            Command command = CommandHandler.commands.get(commandName);
            CommandPermission commandPermission = Main.getFileManager().getPermissionFile().getCommandPermission(command);
            if(args.length == 3) {
                long id = -1;
                try {
                    if(!args[1].equalsIgnoreCase("needPermission")) {
                        id = Long.parseLong(args[2]);
                        if(id <= 0) throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).setDescription("Bitte gebe eine gültige Id ein.").build()).queue(message -> {
                        message.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                    return;
                }

                switch (args[1]) {
                    case "addUser":
                        if(commandPermission.getWhitelistedUsers().contains(id)) {
                            event.getTextChannel().sendMessage("The user is already on the whitelist.").complete();
                            break;
                        }
                        commandPermission.addWhitelistedUser(id);
                        event.getTextChannel().sendMessage("The user has been added to the whitelist.").complete();
                        break;
                    case "removeUser":
                        if(!commandPermission.getWhitelistedUsers().contains(id)) {
                            event.getTextChannel().sendMessage("The user is not on the whitelist.").complete();
                            break;
                        }
                        commandPermission.removeWhitelistedUser(id);
                        event.getTextChannel().sendMessage("The user has been removed from the whitelist.").complete();
                        break;
                    case "addRole":
                        if(commandPermission.getWhitelistedRoles().contains(id)) {
                            event.getTextChannel().sendMessage("The role is already on the whitelist.").complete();
                            break;
                        }
                        commandPermission.addWhitelistedRole(id);
                        event.getTextChannel().sendMessage("The role has been added to the whitelist.").complete();
                        break;
                    case "removeRole":
                        if(!commandPermission.getWhitelistedRoles().contains(id)) {
                            event.getTextChannel().sendMessage("The role is not on the whitelist.").complete();
                            break;
                        }
                        commandPermission.removeWhitelistedRole(id);
                        event.getTextChannel().sendMessage("The role has been removed from the whitelist.").complete();
                        break;
                    case "blockUser":
                        if(commandPermission.getBlacklistedUsers().contains(id)) {
                            event.getTextChannel().sendMessage("The user is already on the blacklist.").complete();
                            break;
                        }
                        commandPermission.addBlacklistedUser(id);
                        event.getTextChannel().sendMessage("The user has been added to the blacklist.").complete();
                        break;
                    case "unblockUser":
                        if(!commandPermission.getBlacklistedUsers().contains(id)) {
                            event.getTextChannel().sendMessage("The user is not on the blacklist.").complete();
                            break;
                        }
                        commandPermission.removeBlacklistedUser(id);
                        event.getTextChannel().sendMessage("The user has been removed from the blacklist.").complete();
                        break;
                    case "needPermission":
                        boolean value = Boolean.parseBoolean(args[2]);
                        commandPermission.setNeedPermission(value);
                        event.getTextChannel().sendMessage("Value for need permission has been set to '" + value + "'.").complete();
                        break;
                    default:
                        event.getTextChannel().sendMessage("Unknown argument").complete();
                        break;
                }
            } else {
                event.getTextChannel().sendMessage("Usage: .Permission <command> <addUser | removeUser | addRole | removeRole | blockUser | unblockUser | needPermission> <value>").complete();
            }
        } else {
            event.getTextChannel().sendMessage("The specified command does not exist").complete();
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }
}
