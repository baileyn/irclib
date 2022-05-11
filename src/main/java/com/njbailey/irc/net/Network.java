package com.njbailey.irc.net;

import com.njbailey.irc.core.Channel;
import com.njbailey.irc.core.Message;
import com.njbailey.irc.core.User;
import com.njbailey.irc.core.messages.NumericMessage;
import com.njbailey.irc.impl.DefaultNumericHandler;
import com.njbailey.irc.net.event.ConnectionListener;
import com.njbailey.irc.net.event.NumericMessageListener;

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

    private List<Channel> channels = new ArrayList<Channel>();
    private List<User> users = new ArrayList<User>();

    private List<ConnectionListener> connectionListeners = new ArrayList<>();
    private List<NumericMessageListener> numericMessageListeners = new ArrayList<>();

    public Network(final String host, final int port) {
        this.host = host;
        this.port = port;

        addNumericMessageListener(new DefaultNumericHandler(this));
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
     * Adds a {@code NumericMessageListener} for this {@code Network}.
     */
    public void addNumericMessageListener(NumericMessageListener messageListener) {
        this.numericMessageListeners.add(messageListener);
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

    /**
     * Called when a {@code Message} is received from the server.
     */
    public void messageReceived(Message message) {
        if(message instanceof NumericMessage) {
            numericMessageListeners.forEach(listener -> listener.onNumericMessage((NumericMessage) message));
        } else {
            System.out.println("Message {");
            System.out.println("\tPrefix: " + message.getPrefix());
            System.out.println("\tCommand: " + message.getCommand());
            System.out.println("\tArguments: " + Arrays.toString(message.getArguments().toArray()));
            System.out.println("}");
        }
    }

    /**
     * Sends the specified {@code Message} to the server.
     * 
     * Note: this function will automatically flush the stream.
     * 
     * @param message the {@code Message} to send
     */
    public void send(Message message) {
        if(channel != null) {
            channel.writeAndFlush(message);
        }
    }

    /**
     * Try to find a user with the specified name, and if they
     * don't exist, create them and add them to the list.
     */
    public User addOrGetUser(final String nickname) {
        User user = null;

        for(User u : users) {
            if(u.getNickname().equalsIgnoreCase(nickname)) {
                user = u;
                break;
            }
        }

        if(user == null) {
            user = new User(nickname);
        }

        return user;
    }

    /**
     * Try to find a channel with the specified name, and if it doesn't 
     * exist create it and add it to the list.
     * 
     * @param name the name of the channel
     * 
     * @return the channel that was in the list
     */
    public Channel addOrGetChannel(final String name) {
        Channel channel = null;

        for(Channel c : channels) {
            if(c.getName().equalsIgnoreCase(name)) {
                channel = c;
                break;
            }
        }

        if(channel == null) {
            channel = new Channel(name);
        }

        return channel;
    }
}
