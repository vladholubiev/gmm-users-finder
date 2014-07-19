package ua.samosfator.gmm.users.finder;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gdata.util.ServiceException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Finder {
    private HashMap<String, String> users = new HashMap<>();

    public void start() throws IOException, ServiceException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);

        HtmlPage loginPage = webClient.getPage("https://accounts.google.com/ServiceLogin?hl=en&continue=http://www.google.com.ua/mapmaker%3Fhl%3Den&service=geowiki");

        HtmlForm form = loginPage.getForms().get(0);
        form.getInputByName("Email").setValueAttribute(Config.GOOGLE_ACCOUNT_USERNAME);
        form.getInputByName("Passwd").setValueAttribute(Config.GOOGLE_ACCOUNT_PASSWORD);
        form.getInputByName("signIn").click();
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    HtmlPage refreshed = webClient.getPage("http://www.google.com.ua/mapmaker?hl=en");
                    parse(refreshed.asXml());
                } catch (Exception ignored) {
                }
            }
        }, 0, Config.REFRESH_TIMEOUT * 1000);

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
        extractUsers(raw);

        Saver gSheets = new GoogleSheets();
        gSheets.prepare();
        gSheets.write(users);
    }

    private void extractUsers(String raw) {
        String[] rawSplit = raw.split(",");
        for (int i = 0; i < rawSplit.length - 1; i++) {
            String str = rawSplit[i];
            String strNext = rawSplit[i + 1];

            if (str.indexOf("gaia_id\":") > 0 && strNext.indexOf("profile_name\":") > 0) {
                String uid = str.replaceAll("\\D*", "");
                String username = strNext.replaceAll("(.*):", "").replaceAll("\"", "");
                users.put(uid, username);
            }
        }
    }
}
