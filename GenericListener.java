package net.avdw.discord;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.tinylog.Logger;

import java.net.UnknownHostException;
import java.util.Objects;

/**
 * @version 2021-02-04 Add silent method
 * 2020-12-14 Can now handle MessageUpdateEvent
 * 2020-12-12 Stripped down class
 * 2020-07-09 Added templating
 */
public class GenericListener implements EventListener {
    private final CliService cliService;
    private final String prefix;

    @Inject
    GenericListener(final CliService cliService, @Named(DiscordPropertyKey.PREFIX) final String prefix) {
        this.cliService = cliService;
        this.prefix = prefix;
    }

    @Override
    public void onEvent(final GenericEvent genericEvent) {
        if (genericEvent instanceof MessageReceivedEvent) {
            MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
            DiscordDaemon.resetTimeout();
            if (!event.getAuthor().isBot() && event.getMessage().getContentDisplay().startsWith(prefix)) {
                cliService.processEvent(event);
            }
        } else if (genericEvent instanceof MessageUpdateEvent) {
            MessageUpdateEvent event = (MessageUpdateEvent) genericEvent;
            DiscordDaemon.resetTimeout();
            if (!event.getAuthor().isBot() && event.getMessage().getContentDisplay().startsWith(prefix)) {
                cliService.processEvent(event);
            }
        } else if (genericEvent instanceof GuildMessageReceivedEvent) {
            silentlyHandle();
        } else if (genericEvent instanceof GatewayPingEvent) {
            silentlyHandle();
        } else if (genericEvent instanceof HttpRequestEvent) {
            HttpRequestEvent httpRequestEvent = (HttpRequestEvent) genericEvent;
            int code = Objects.requireNonNull(httpRequestEvent.getResponse()).code;
            if (code == -1) {
                if (httpRequestEvent.getResponse().getException() instanceof UnknownHostException) {
                    Logger.info("Unknown host, connection is likely down");
                } else {
                    Logger.warn("Unhandled error response: {}", httpRequestEvent.getResponse().getException());
                }
            } else if (code == 200) {
                silentlyHandle();
            } else {
                Logger.warn("Unhandled http event [{}]: {}", httpRequestEvent.getClass(), code);
            }
        } else if (genericEvent instanceof StatusChangeEvent) {
            StatusChangeEvent statusChangeEvent = (StatusChangeEvent) genericEvent;
            Logger.info(statusChangeEvent.getNewStatus());
        } else if (genericEvent instanceof ReadyEvent) {
            System.out.println("Discord bot is ready\n" +
                    "  (see './application.log' for further details)\n" +
                    "  (press <Ctrl+C> to stop)");
            Logger.info("API is ready!");
        } else if (genericEvent instanceof GuildReadyEvent) {
            silentlyHandle();
        } else {
            Logger.warn("Unhandled event [{}]: '{}'", genericEvent.getClass(), genericEvent);
        }
    }

    private void silentlyHandle() {
        // do nothing
    }
}
