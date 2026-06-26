package com.martino;

/*
 * Author: M
 * Date: 25-Jun-26
 * Project Name: m-v
 * Description: beExcellent
 */

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class CamillaDspRemote implements AutoCloseable {

    private final CountDownLatch connectedLatch = new CountDownLatch(1);

    private Client client;

    private volatile Float currentVolumeDb;
    private volatile Boolean muted;

    public CamillaDspRemote(String url) {
        client = new Client(URI.create(url));
    }

    public void connect() {
        client.connect();

        try {
            boolean connected = connectedLatch.await(5, TimeUnit.SECONDS);

            if (!connected || !client.isOpen()) {
                throw new IllegalStateException("Cannot connect to CamillaDSP websocket.");
            }

            System.out.println("Connected to CamillaDSP.");

            requestVolume();
            requestMute();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while connecting to CamillaDSP.", e);
        }
    }

    public void changeGain(float deltaDb) {
        String json = String.format(
                Locale.US,
                "{\"AdjustVolume\":[%.2f, %.2f, %.2f]}",
                deltaDb,
                AppConfig.MIN_GAIN_DB,
                AppConfig.MAX_GAIN_DB
        );

        send(json);
    }

    public void toggleMute() {
        send("\"ToggleMute\"");
    }

    public void requestVolume() {
        send("\"GetVolume\"");
    }

    public void requestMute() {
        send("\"GetMute\"");
    }

    private void send(String message) {
        if (client == null || !client.isOpen()) {
            throw new IllegalStateException("CamillaDSP websocket is not connected.");
        }

        client.send(message);
    }

    private void handleMessage(String message) {
        String value = message.trim();

        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            muted = Boolean.parseBoolean(value);
            System.out.println("CamillaDSP mute = " + muted);
            return;
        }

        try {
            float volume = Float.parseFloat(value);
            currentVolumeDb = volume;
            System.out.printf(Locale.US, "CamillaDSP volume = %.1f dB%n", volume);
        } catch (NumberFormatException ignored) {
            System.out.println("CamillaDSP response: " + message);
        }
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    private final class Client extends WebSocketClient {

        private Client(URI uri) {
            super(uri);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            connectedLatch.countDown();
        }

        @Override
        public void onMessage(String message) {
            handleMessage(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("Disconnected from CamillaDSP.");
        }

        @Override
        public void onError(Exception ex) {
            System.err.println("CamillaDSP websocket error: " + ex.getMessage());
        }
    }
}