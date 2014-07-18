package ua.samosfator.gmm.users.finder;

import com.google.gdata.util.ServiceException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        Finder userFinder = new Finder();
        userFinder.setConfig();
        userFinder.start();
    }
}
