package ua.samosfator.gmm.users.finder;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gdata.util.ServiceException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class Finder {
    private HashMap<String, String> users = new HashMap<>();
    static String GOOGLE_ACCOUNT_USERNAME = "";
    static String GOOGLE_ACCOUNT_PASSWORD = "";
    static String SPREADSHEET_URL = "";

    public void setConfig() throws IOException {
        Path path = Paths.get(".config");
        List<String> config = Files.readAllLines(path);
        GOOGLE_ACCOUNT_USERNAME = config.get(0);
        GOOGLE_ACCOUNT_PASSWORD = config.get(1);
        SPREADSHEET_URL = config.get(2);
    }

    public void start() throws IOException, ServiceException {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(true);

        HtmlPage loginPage = webClient.getPage("https://accounts.google.com/ServiceLogin?hl=en&continue=http://www.google.com.ua/mapmaker%3Fhl%3Den&service=geowiki");

        HtmlForm form = loginPage.getForms().get(0);
        form.getInputByName("Email").setValueAttribute("mapmaker.users@gmail.com");
        form.getInputByName("Passwd").setValueAttribute("mNTUxyzk7HAp");
        HtmlPage mainPage = form.getInputByName("signIn").click();
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    HtmlPage refreshed = webClient.getPage("http://www.google.com.ua/mapmaker?hl=en");
                    parse(refreshed.asXml());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 120000);

        webClient.closeAllWindows();
    }

    private void parse(String html) throws IOException, ServiceException {
        Document doc = Jsoup.parse(html);
        Elements script = doc.select("script");
        String raw = "";

        for (Element e : script) {
            String scriptEl = e.toString();
            if (scriptEl.indexOf("review_form_data") > 0) {
                raw = scriptEl.replace("<script type=\"text/javascript\">", "")
                        .replace("//<![CDATA[", "")
                        .replace("//]]>", "")
                        .replace("</script>", "")
                        .replace("\n", "")
                        .replace("\r", "")
                        .replaceFirst("^(?s:.*?)review_form_data", "");
                raw = "{\"review_form_data:" + raw.substring(0, raw.length() - 2) + "}";
            }
        }
        String[] rawSplit = raw.split(",");
        for (int i = 0; i < rawSplit.length - 1; i++) {
            String str = rawSplit[i];
            String strNext = rawSplit[i + 1];

            if (str.indexOf("gaia_id\":") > 0 && strNext.indexOf("profile_name\":") > 0) {
                String uid = str.replaceAll("\\D*", "");
                String username = strNext.replaceAll("(.*):", "").replaceAll("\"", "");
                //Temporary hack: necessary to set cell format as text instead of number
                users.put(uid + "?", username);
            }
        }

        Saver gSheets = new GoogleSheets();
        gSheets.prepare();
        gSheets.write(users);
    }
}
