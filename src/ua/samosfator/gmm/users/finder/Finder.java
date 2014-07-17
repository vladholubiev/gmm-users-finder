package ua.samosfator.gmm.users.finder;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Finder {
    private final String filename = String.valueOf(System.nanoTime());
    private HashMap<String, String> users = new HashMap<>();

    public void start() throws IOException {
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

        parse(mainPage.asXml());

        webClient.closeAllWindows();
    }

    private void parse(String html) throws IOException {
        Document doc = Jsoup.parse(html);
        Elements script = doc.select("script");
        String raw = null;

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
            String strNext = rawSplit[i+1];

            if (str.indexOf("gaia_id\":") > 0 && strNext.indexOf("profile_name\":") > 0) {
                String uid = str.replaceAll("\\D*", "");
                String username = strNext.replaceAll("(.*):", "").replaceAll("\"", "");
                users.put(uid, username);
            }
        }
        for (Map.Entry<String, String> s : users.entrySet()) {
            String[] row = {s.getKey(), s.getValue()};
            write(row);
        }
    }
    private void write(String[] row) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter("users.csv", true), ',');
        writer.writeNext(row);
        writer.close();
    }
    public void read() throws IOException {
        CSVReader reader = new CSVReader(new FileReader("users.csv"));
        String [] row;
        while ((row = reader.readNext()) != null) {
            System.out.println(row[0] + ", " + row[1]);
        }
    }
}
