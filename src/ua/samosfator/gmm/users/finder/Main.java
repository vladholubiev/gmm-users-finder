package ua.samosfator.gmm.users.finder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) throws IOException {
        Finder userFinder = new Finder("users.csv");

        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    userFinder.start();
                } catch (IOException e) {e.printStackTrace();}
            }
        }, 0, 120000);
    }
}
