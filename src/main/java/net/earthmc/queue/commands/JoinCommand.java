package net.earthmc.queue.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.earthmc.queue.Queue;
import net.earthmc.queue.QueuePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collections;
import java.util.List;

public class JoinCommand extends BaseCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("This command cannot be used by console.", NamedTextColor.RED));
            return;
        }

        if (invocation.arguments().length == 0) {
            source.sendMessage(Component.text("Not enough arguments. Usage: /joinqueue [server].", NamedTextColor.RED));
            return;
        }

        if (!invocation.source().hasPermission("queue.join." + invocation.arguments()[0].toLowerCase()) && !invocation.source().hasPermission("queue.join.*")) {
            source.sendMessage(Component.text("You do not have enough permissions to join this queue."));
            return;
        }

        String server = invocation.arguments()[0];
        if (player.getCurrentServer().isPresent() && player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase(server)) {
            player.sendMessage(Component.text("You are already connected to this server.", NamedTextColor.RED));
            return;
        }

        Queue queue = QueuePlugin.instance().queue(server);
        if (queue == null) {
            player.sendMessage(Component.text(server + " is not a valid server.", NamedTextColor.RED));
            return;
        }

        queue.enqueue(QueuePlugin.instance().queued(player));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 1)
            return filterByStart(QueuePlugin.instance().queues().keySet(), invocation.arguments()[0]);

        return Collections.emptyList();
    }
}