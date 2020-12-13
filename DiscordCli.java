package net.avdw.discord;

import com.google.inject.Inject;
import picocli.CommandLine.Command;

@Command(name = "discord", description = "Start discord bot", hidden = true)
public class DiscordCli implements Runnable {
    @Inject private DiscordDaemon discordDaemon;

    @Override
    public void run() {
        discordDaemon.start();
    }
}
