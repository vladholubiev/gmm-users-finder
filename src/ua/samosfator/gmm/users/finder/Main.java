package ua.samosfator.gmm.users.finder;

/**
 * https://github.com/samosfator/gmm-users-finder
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Finder userFinder = new Finder();
        Config.setConfig();
        userFinder.start();
    }
}
