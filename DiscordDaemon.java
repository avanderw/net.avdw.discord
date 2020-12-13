package net.avdw.discord;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.tinylog.Logger;

/**
 * @version 2020-12-12 Refactored to not have a main method
 * 2020-12-11 Refactor to use simpler PropertyFile
 * 2020-06-11 Added reconnect ability
 */
public final class DiscordDaemon {
    private static long reconnectTimeout = 1;
    private final String apiToken;
    private final GenericListener genericListener;
    private final String prefix;

    @Inject
    DiscordDaemon(@Named(DiscordPropertyKey.API_TOKEN) final String apiToken, @Named(DiscordPropertyKey.PREFIX) final String prefix, final GenericListener genericListener) {
        this.apiToken = apiToken;
        this.prefix = prefix;
        this.genericListener = genericListener;
    }

    static void resetTimeout() {
        reconnectTimeout = 1;
    }

    public void start() {
        System.setProperty("picocli.ansi", "false");

        boolean reconnect = true;
        while (reconnect) {
            try {
                // Note: It is important to register your ReadyListener before building
                JDA jda = JDABuilder.createDefault(apiToken)
                        .addEventListeners(genericListener)
                        .setActivity(Activity.of(Activity.ActivityType.WATCHING,
                                String.format("for %s --help", prefix)))
                        .build();

                // optionally block until JDA is ready
                jda.awaitReady();
                reconnect = false;
            } catch (final Exception e) {
                Logger.debug(e);
                reconnect = true;
                if (reconnectTimeout < 128) {
                    reconnectTimeout *= 2;
                }

                try {
                    Logger.info("Exception connecting to discord\n  (likely no connection, retrying in {}s)\n  (enable DEBUG to see more)", reconnectTimeout);
                    Thread.sleep(reconnectTimeout * 1000);
                } catch (InterruptedException ex) {
                    Logger.debug("Thread interrupted, will not reconnect");
                    reconnect = false;
                }
            }
        }
    }
}
