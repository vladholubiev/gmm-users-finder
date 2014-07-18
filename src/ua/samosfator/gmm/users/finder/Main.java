package ua.samosfator.gmm.users.finder;

import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) throws IOException, ServiceException {
        Finder userFinder = new Finder();
        userFinder.setConfig();
        userFinder.start();
    }
}
