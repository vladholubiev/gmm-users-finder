package ua.samosfator.gmm.users.finder;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Finder userFinder = new Finder();
        userFinder.read();
    }
}
