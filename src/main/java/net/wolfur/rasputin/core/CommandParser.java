package net.wolfur.rasputin.core;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.wolfur.rasputin.util.Variables;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {

    public static CommandContainer parser(String raw, MessageReceivedEvent event) {

        String beheaded = raw.replaceFirst(Variables.PREFIX, "");
        String[] splitedBeheaded = beheaded.split(" ");
        String invoke = splitedBeheaded[0].toLowerCase();

        List<String> split = new ArrayList<>();
        for(String str : splitedBeheaded) {
            split.add(str);
        }

        String[] args = new String[split.size() - 1];
        split.subList(1, split.size()).toArray(args);

        return new CommandContainer(raw, beheaded, splitedBeheaded, invoke, args, event);
    }

    public static class CommandContainer {

        public final String raw;
        public final String beheaded;
        public final String[] splitedBeheaded;
        public final String invoke;
        public final String[] args;
        public final MessageReceivedEvent event;

        public CommandContainer(String raw, String beheaded, String[] splitedBeheaded, String invoke, String[] args, MessageReceivedEvent event) {
            this.raw = raw;
            this.beheaded = beheaded;
            this.splitedBeheaded = splitedBeheaded;
            this.invoke = invoke;
            this.args = args;
            this.event = event;
        }

    }
}
