package ua.samosfator.gmm.users.finder;

import com.google.gdata.util.ServiceException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public interface Saver {
    //Read existing users from source
    HashMap<String, String> read() throws IOException, ServiceException;

    //Write new users
    void write(HashMap<String, String> users) throws IOException, ServiceException;

    //Prepare file/table/database
    void prepare() throws IOException, ServiceException;
}
