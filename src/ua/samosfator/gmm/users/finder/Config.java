package ua.samosfator.gmm.users.finder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.LogManager;

public class Config {
    public static String GOOGLE_ACCOUNT_USERNAME = "";
    public static String GOOGLE_ACCOUNT_PASSWORD = "";
    public static String SPREADSHEET_URL = "";
    public static int REFRESH_TIMEOUT;

    public static void setConfig() throws InterruptedException {
        Path path = Paths.get(".config");
        try {
            List<String> config = Files.readAllLines(path);
            GOOGLE_ACCOUNT_USERNAME = config.get(0);
            GOOGLE_ACCOUNT_PASSWORD = config.get(1);
            SPREADSHEET_URL = config.get(2);
            REFRESH_TIMEOUT = Integer.parseInt(config.get(3).replaceAll("\\D", ""));

            System.out.println();
        } catch (IOException e) {
            System.out.println("\nYou have to create .config file with user login, password and" +
                    " spreadsheet link per row before launch");
            Thread.sleep(Long.MAX_VALUE);
        }
        LogManager.getLogManager().reset();
    }
}
