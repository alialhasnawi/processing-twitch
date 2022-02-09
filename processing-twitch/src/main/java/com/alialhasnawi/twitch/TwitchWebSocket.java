package com.alialhasnawi.twitch;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.net.ssl.SSLSocketFactory;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

public class TwitchWebSocket {
    // Constants for interfacing with the Twitch IRC.
    private static final String BASE_URI = "wss://irc-ws.chat.twitch.tv:443";

    // IRC Commands
    private static final String NICK_PREFIX = "NICK ";
    private static final String PASS_PREFIX = "PASS ";
    private static final String JOIN_PREFIX = "JOIN #";
    private static final String PING_PREFIX = "PING";
    private static final String PRIVMSG_CHANNEL_INDICATOR = "PRIVMSG #";

    private static final String PONG_RESPONSE = "PONG :tmi.twitch.tv";

    // Timeout is how long until the websocket decides to close.
    private static final int TIMEOUT = 5000;

    private static final WebSocketFactory wsFactory = new WebSocketFactory()
            .setSocketFactory(SSLSocketFactory.getDefault());

    private WebSocket ws;
    private boolean open = false;

    private String username;
    private String OAuthToken;

    private boolean inChannel;
    private String connectedChannel;
    private String channelMsgSeparator;

    private Queue<String> buffer = new LinkedList<String>();

    /**
     * Create a new TwitchWebsocket.
     * 
     * @param username   Twitch login username in lowercase.
     * @param OAuthToken Twitch OAuth token.
     * 
     *                   The OAuth token can be generated easily by going to
     *                   https://twitchapps.com/tmi/
     */
    public TwitchWebSocket(String username, String OAuthToken) {
        // Format input in case the user did not read documentation.
        this.username = username.toLowerCase();

        // OAuth token must start with oauth:<TOKEN> for Twitch to recognize it.
        if (OAuthToken.startsWith("oauth:")) {
            this.OAuthToken = OAuthToken;
        } else {
            this.OAuthToken = "oauth:" + OAuthToken;
        }
    }

    /**
     * Open this websocket and connect to Twitch.
     * 
     * @throws RuntimeException
     */
    public void open() throws RuntimeException {
        try {
            ws = wsFactory.createSocket(BASE_URI, TIMEOUT);
            ws.connect();
            open = true;
        } catch (IOException | WebSocketException e) {
            throw new RuntimeException("Failed to connect to twitch websocket. Double check your connection.", e);
        }

        this.authenticate();
        ws.addListener(new TwitchWebSocketAdapter());
    }

    /**
     * Authenticate this WebSocket connection using the provided OAuth token and
     * Twitch username.
     */
    private void authenticate() {
        ws.sendText(PASS_PREFIX + this.OAuthToken);
        ws.sendText(NICK_PREFIX + this.username);
    }

    /**
     * Check if there are more messages to receive.
     * 
     * @return whether there are more messages to receive.
     */
    public synchronized boolean hasMoreMessages() {
        return !buffer.isEmpty();
    }

    /**
     * Get the next most recent received message or null if no more messages have
     * been received.
     * 
     * @return the next most recent message received or null if there are currently
     *         no new messages.
     */
    public synchronized String getNextMessage() {
        if (buffer.isEmpty()) {
            System.err.println("No messages to read so getNextMessage() returned null.");
        }
        return buffer.poll();
    }

    /**
     * Join the specified twitch.tv channel and begin listening to messages there.
     * The websocket must be already open.
     * 
     * @param channelName name of channel as it appears in the URL to the stream.
     */
    public void joinChannel(String channelName) {
        if (open && !inChannel) {
            ws.sendText(JOIN_PREFIX + channelName);
            this.connectedChannel = channelName;
            this.channelMsgSeparator = PRIVMSG_CHANNEL_INDICATOR + connectedChannel + " :";
            inChannel = true;
        } else if (inChannel) {
            System.err.format("Already connected to channel @%s!\n", connectedChannel);
        } else if (!open) {
            System.err.println("Socket is still closed! .open() must be called before attempting to join a channel.");
        }
    }

    /**
     * Adapter which handles IRC messages and passes them back to the parent object
     * for public access.
     */
    private class TwitchWebSocketAdapter extends WebSocketAdapter {
        @Override
        public void onDisconnected(WebSocket websocket,
                WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                boolean closedByServer) throws Exception {
            open = false;
            if (closedByServer) System.err.println("Socket was disconnected by server!");
            else System.err.println("Socket was disconnected by client!");
        }

        /**
         * Called when the server sends messages.
         */
        @Override
        public void onTextMessage(WebSocket ws, String message) throws Exception {
            if (message.startsWith(PING_PREFIX)) {
                ws.sendText(PONG_RESPONSE);
            } else if (inChannel) {

                int indexOfSeparator = message.indexOf(channelMsgSeparator);

                if (indexOfSeparator != -1) {
                    buffer.add(
                            message.substring(indexOfSeparator + channelMsgSeparator.length()));
                }
            }
        }
    }
}
