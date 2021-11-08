package net.earthmc.queue;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.earthmc.queue.commands.JoinCommand;
import net.earthmc.queue.commands.LeaveCommand;
import net.earthmc.queue.commands.PauseCommand;
import net.earthmc.queue.commands.QueueCommand;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin(id = "queue", name = "Queue", version = "0.0.1", authors = {"Warriorrr"})
public class QueuePlugin {

    private static ProxyServer proxy;
    private static QueuePlugin instance;
    private final Map<String, Queue> queues = new HashMap<>();
    private final Map<Player, QueuedPlayer> queuedPlayers = new HashMap<>();

    @Inject
    public QueuePlugin(ProxyServer proxy, CommandManager commandManager) {
        QueuePlugin.instance = this;
        QueuePlugin.proxy = proxy;

        commandManager.register("joinqueue", new JoinCommand());
        commandManager.register("leavequeue", new LeaveCommand());
        commandManager.register("pausequeue", new PauseCommand());
        commandManager.register("queue", new QueueCommand());
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        for (RegisteredServer server : proxy.getAllServers()) {
            queues.put(server.getServerInfo().getName().toLowerCase(), new Queue(server));
        }

        proxy.getScheduler().buildTask(this, () -> {
            for (Queue queue : queues().values())
                queue.sendNext();
        }).repeat(250, TimeUnit.MILLISECONDS).schedule();

        proxy.getScheduler().buildTask(this, () -> {
            for (Queue queue : queues.values())
                queue.refreshMaxPlayers();

        }).repeat(10, TimeUnit.SECONDS).schedule();
    }

    public Map<String, Queue> queues() {
        return queues;
    }

    public static QueuePlugin instance() {
        return instance;
    }

    public static ProxyServer proxy() {
        return proxy;
    }

    @Nullable
    public Queue queue(String serverName) {
        Queue queue = queues.get(serverName.toLowerCase());

        if (queue != null)
            return queue;

        Optional<RegisteredServer> registeredServer = proxy.getServer(serverName);
        if (registeredServer.isEmpty())
            return null;

        queue = new Queue(registeredServer.get());
        queues.put(serverName.toLowerCase(), queue);

        return queue;
    }

    public QueuedPlayer queued(Player player) {
        queuedPlayers.computeIfAbsent(player, k -> new QueuedPlayer(player));

        return queuedPlayers.get(player);
    }
}