package com.njbailey.irc.net;

import com.njbailey.irc.core.Message;
import com.njbailey.irc.net.event.ConnectionListener;
import io.netty.channel.socket.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a Network that's connected to by the Internet Relay Chat.
 */
public class Network {
    private SocketChannel channel;
    private final String host;
    private final int port;

    private List<ConnectionListener> connectionListeners = new ArrayList<>();

    public Network(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public void setChannel(final SocketChannel channel) {
        this.channel = channel;
    }

    public void destroyChannel() {
        this.channel = null;
    }

    /**
     * Adds a {@code ConnectionListener} for this {@code Network}.
     */
    public void addConnectionListener(ConnectionListener connectionListener) {
        this.connectionListeners.add(connectionListener);
    }

    /**
     * This function is called whenever the client has successfully connected to the network.
     */
    public void connected() {
        connectionListeners.forEach(listener -> listener.connectionAcquired(this));
    }

    /**
     * This function is called whenever the client has been disconnected from the network.
     */
    public void dropped() {
        connectionListeners.forEach(listener -> listener.connectionLost(this));
    }

    public void messageReceived(Message message) {
        System.out.println("Message {");
        System.out.println("\tPrefix: " + message.getPrefix());
        System.out.println("\tCommand: " + message.getCommand());
        System.out.println("\tArguments: " + Arrays.toString(message.getArguments().toArray()));
        System.out.println("}");
    }

    public void send(Message message) {
        if(channel != null) {
            channel.writeAndFlush(message);
        }
    }
}
