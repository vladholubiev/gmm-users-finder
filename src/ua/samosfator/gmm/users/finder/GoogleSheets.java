package ua.samosfator.gmm.users.finder;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GoogleSheets implements Saver {
    private SpreadsheetService service;
    private URL listFeedUrl;

    public void prepare() throws ServiceException, IOException {
        service = new SpreadsheetService("Print Google Spreadsheet Demo");
        service.setUserCredentials(Finder.GOOGLE_ACCOUNT_USERNAME, Finder.GOOGLE_ACCOUNT_PASSWORD);
        URL metaFeedUrl = new URL(Finder.SPREADSHEET_URL);
        SpreadsheetEntry spreadsheet = service.getEntry(metaFeedUrl, SpreadsheetEntry.class);
        listFeedUrl = spreadsheet.getWorksheets().get(0).getListFeedUrl();
    }

    public HashMap<String, String> read() throws ServiceException, IOException {
        ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
        HashMap<String, String> existing = new HashMap<>();
        for (ListEntry entry : feed.getEntries()) {
            existing.put(
                    entry.getCustomElements().getValue("uid"),
                    entry.getCustomElements().getValue("username")
            );
        }
        return existing;
    }

    public void write(HashMap<String, String> users) throws ServiceException, IOException {
        ListEntry row = new ListEntry();
        for (Map.Entry<String, String> s : mapDiff(read(), users).entrySet()) {
            row.getCustomElements().setValueLocal("uid", s.getKey());
            row.getCustomElements().setValueLocal("username", s.getValue());
            System.out.println(s.getKey() + " : " + s.getValue());
            service.insert(listFeedUrl, row);
        }
    }

    public static HashMap<String, String> mapDiff(HashMap<String, String> a, HashMap<String, String> b) {
        HashMap<String, String> diff = new HashMap<>();
        diff.putAll(a);
        diff.putAll(b);
        diff.entrySet().removeAll(a.entrySet());
        return diff;
    }
}
