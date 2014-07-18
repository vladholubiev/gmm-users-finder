package ua.samosfator.gmm.users.finder;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CSV implements Saver {

    public void prepare() throws IOException {
        File file = new File("users.csv");
        if (!file.exists()) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("users.csv"), "UTF-8"
            ));
            out.close();
        }
    }

    public HashMap<String, String> read() throws IOException {
        HashMap<String, String> users = new HashMap<>();
        CSVReader reader = null;
        reader = new CSVReader(new FileReader("users.csv"));
        String[] row;
        while ((row = reader.readNext()) != null) {
            users.put(row[0], row[1]);
        }
        return users;
    }

    public void write(HashMap<String, String> users) throws IOException {
        for (Map.Entry<String, String> s : users.entrySet()) {
            String[] row = {s.getKey(), s.getValue()};

            CSVWriter writer = new CSVWriter(new FileWriter("users.csv", true), ',');
            writer.writeNext(row);
            writer.close();
        }
    }


}
