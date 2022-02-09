package com.alialhasnawi.twitch;

public class Test {

    /**
     * Testing a simple connection example.
     * @param args
     */
    public static void main(String[] args) {
        // PROCESSING
        TwitchWebSocket socket = new TwitchWebSocket("alialhasnawi", "oauth:rnoqnrdq5xg45ab0c4moa5ahf2kya6");

        socket.open();
        socket.joinChannel("schlatt");

        System.out.println();

        while (true) {
            if (socket.hasMoreMessages()) {
                System.out.println(socket.getNextMessage().trim());
            }
        }
    }
}
