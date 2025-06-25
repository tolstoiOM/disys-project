package org.example;

public class Main {
    public static void main(String[] args) {
        // UserMessageGenerator starten
        Thread userMessageThread = new Thread(() -> {
            try {
                UserMessageGenerator.main(args);
            } catch (Exception e) {
                System.err.println("Fehler im UserMessageGenerator: " + e.getMessage());
            }
        });

        // ProducerMessageGenerator starten
        Thread producerMessageThread = new Thread(() -> {
            try {
                ProducerMessageGenerator.main(args);
            } catch (Exception e) {
                System.err.println("Fehler im ProducerMessageGenerator: " + e.getMessage());
            }
        });

        // MessageReceiver starten
        Thread messageReceiverThread = new Thread(() -> {
            try {
                MessageReceiver.main(args);
            } catch (Exception e) {
                System.err.println("Fehler im MessageReceiver: " + e.getMessage());
            }
        });

        // Threads starten
        userMessageThread.start();
        producerMessageThread.start();
        messageReceiverThread.start();
    }
}