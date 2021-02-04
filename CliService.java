package net.avdw.discord;

import com.google.inject.Inject;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @version 2021-02-04 Cleanup unused code
 * 2020-12-14 Updated to handle MessageUpdateEvent
 * 2020-12-12 renamed from Wrapper to Service
 */
public class CliService {
    private final CommandLine commandLine;

    @Inject
    public CliService(final CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    private void process(final String command, final MessageChannel channel) {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        commandLine.setOut(new PrintWriter(out));
        commandLine.setErr(new PrintWriter(err));

        commandLine.execute(command.split("\\s+"));

        if (out.toString().isEmpty() && err.toString().isEmpty()) {
            Logger.debug("Command has no output: {}", command);
        }
        out.flush();
        String output = out.toString().isEmpty() ? err.toString() : out.toString();
        String response = String.format("```\n" +
                "%s\n" +
                "```", output);

        Logger.info("REQUEST: {}", command);
        Logger.info("RESPONSE: {}", response);
        if (response.length() > 2000) {
            String[] argumentList = command.split(" ");
            String filename;
            if (argumentList.length >= 2) {
                filename = String.format("%s-%s", argumentList[0], argumentList[1]);
            } else {
                filename = "cli";
            }
            channel.sendFile(response.getBytes(StandardCharsets.UTF_8), String.format("%s-%s.txt", filename, new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date()))).queue();
        } else {
            channel.sendMessage(response).queue();
        }
    }

    public void processEvent(final MessageReceivedEvent event) {
        String command = event.getMessage().getContentRaw().replaceFirst("<\\S+>", "").trim();
        process(command, event.getChannel());
    }

    public void processEvent(final MessageUpdateEvent event) {
        String command = event.getMessage().getContentRaw().replaceFirst("<\\S+>", "").trim();
        process(command, event.getChannel());
    }
}
