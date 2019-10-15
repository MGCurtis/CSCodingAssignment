import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class LogParserTest {
    LogParser parser = new LogParser();

    @Test(expected = IOException.class)
    public void mainFileNotFound() throws FileNotFoundException {
        String[] args = {"src/resources/fakeFile.txt"};
        parser.main(args);
    }

    @Test
    public void separateJSONTest() {
        JSONObject entry1 = new JSONObject();
        entry1.put("id", "1357");
        entry1.put("state", "STARTED");
        entry1.put("timestamp", (long) 12377568);
        JSONObject entry2 = new JSONObject();
        entry2.put("id", "1357");
        entry2.put("state", "FINISHED");
        entry2.put("timestamp", (long) 12377572);
        JSONArray entryList = new JSONArray();
        entryList.add(entry1);
        entryList.add(entry2);

        assertEquals(entry1.get("id"), parser.separateJSON(entryList).get(0).getId());
        assertEquals(entry1.get("timestamp"), parser.separateJSON(entryList).get(0).getStart());
        assertEquals(entry2.get("timestamp"), parser.separateJSON(entryList).get(0).getEnd());
    }
}