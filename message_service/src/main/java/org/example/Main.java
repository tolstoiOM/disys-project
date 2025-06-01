package org.example;
public class Main {
    public static void main(String[] args) {
        // UserMessageGenerator starten
        Thread userMessageThread = new Thread(() -> {
            UserMessageGenerator userMessageGenerator = new UserMessageGenerator();
            while (true) {
                userMessageGenerator.generateAndSendMessage();
                try {
                    Thread.sleep(7000); // alle 7 Sekunden
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // ProducerMessageGenerator starten
        Thread producerMessageThread = new Thread(() -> {
            ProducerMessageGenerator.main(args);
        });

        // Threads starten
        userMessageThread.start();
        producerMessageThread.start();
    }
}