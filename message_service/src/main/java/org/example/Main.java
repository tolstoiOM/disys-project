package org.example;

public class Main {
    public static void main(String[] args) {
        // UserMessageGenerator starten
        Thread userMessageThread = new Thread(() -> {
            try {
                UserMessageGenerator.main(args);
            } catch (Exception e) {
                System.err.println("❌ Fehler im UserMessageGenerator: " + e.getMessage());
            }
        });

        // ProducerMessageGenerator starten
        Thread producerMessageThread = new Thread(() -> {
            try {
                ProducerMessageGenerator.main(args);
            } catch (Exception e) {
                System.err.println("❌ Fehler im ProducerMessageGenerator: " + e.getMessage());
            }
        });

        // Threads starten
        userMessageThread.start();
        producerMessageThread.start();
    }
}