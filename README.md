# processing-twitch
Twitch API interface for Processing. Allows for reading chat messages from Twitch channels in real time.

# Usage

To use this package, you need:
1. A Twitch account.
2. An OAuth token for your Twitch account. OAuth tokens can be generated easily at [this link](https://twitchapps.com/tmi/)

### Example
The following Processing sketch displays the most recent chat message in a Twitch channel.
```java
import com.alialhasnawi.twitch.*;

TwitchWebSocket socket = new TwitchWebSocket("your_twitch_username", "your_twitch_oauth_token");

void setup() {
  size(600, 400);
  
  socket.open();
  socket.joinChannel("channel_username_here");
}

void draw(){
  if (socket.hasMoreMessages()) {
    background(0);
    text(socket.getNextMessage(), 20, 20);
  }
}
```

# Installation
To install: 
1. Unzip the **processing_twitch.zip** (available in the [releases tab](https://github.com/alialhasnawi/processing-twitch/releases/)) into the Documents/processing/libraries folder.
2. Restart Processing.
3. Add the library using the menubar: **Sketch > Import Library > processing_twitch** .

# Interface
### TwitchWebSocket(String username, String OAuthToken)
Create a new TwitchWebSocket and sign in to an account.
- **username** Twitch login username in lowercase.
- **OAuthToken** Twitch OAuth token.

### open()
Open this WebSocket and connect to the Twitch servers.

### joinChannel(String channelName)
Join the specified twitch.tv channel and begin listening to messages there.
The websocket must be already open.
- **channelName** Name of channel as it appears in the URL to the stream.

### boolean hasMoreMessages()
Check if there are more messages waiting to be received.

### String getNextMessage()
Get the next most recent received message or **null** if no more messages have been received.
Always check that the websocket has messages using **hasMoreMessages()** before trying to get the next message.
