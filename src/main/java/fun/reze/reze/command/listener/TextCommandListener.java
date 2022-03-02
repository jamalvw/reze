package fun.reze.reze.command.listener;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import fun.reze.reze.Config;
import fun.reze.reze.EventListener;
import fun.reze.reze.Replies;
import fun.reze.reze.command.Cmd;
import fun.reze.reze.command.Commands;
import fun.reze.reze.command.context.Context;
import fun.reze.reze.command.exception.CommandException;
import reactor.core.publisher.Mono;

public class TextCommandListener implements EventListener<MessageCreateEvent>
{
    @Override
    public Class<MessageCreateEvent> getEventType()
    {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<?> execute(final MessageCreateEvent event)
    {
        Message message = event.getMessage();
        User author = message.getAuthor().get();

        // Don't respond to bots
        if (author.isBot()) return Mono.empty();

        String content = message.getContent();
        String prefix = Config.get().getPrefix();

        // Check if message starts with command prefix
        if (!content.startsWith(prefix)) return Mono.empty();

        // Get the alias from the message
        String alias = content.split(" ")[0].replaceFirst(prefix, "");
        // Find the command from the alias
        Cmd cmd = Commands.find(alias);

        if (cmd == null) return Mono.empty();

        // Try to execute the command
        try
        {
            return cmd.tryExecute(new Context(event));
        }
        // Catch command-based exceptions and message them
        catch (CommandException err)
        {
            return message.getChannel().flatMap(ch -> Replies.failure(ch, err.getMessage()));
        }
        // All-else-fails, send a generic exception message
        catch (Exception err)
        {
            return message.getChannel().flatMap(ch -> Replies.failure(ch, "Something went wrong using this command. Please contact the developers about this!"));
        }
    }
}
