import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.*;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.io.*;

public class LogParser {

    static Connection con;
    static String connectionString = "jdbc:hsqldb:file:hsqldb/logDatabase";

    public static void main(String[] args) throws FileNotFoundException {
        String createLogTable = "create table if not exists log_entries (id varchar(30), durationMillis INTEGER, type varchar(45), host varchar(10), alert BOOLEAN)";
        Logger logger = Logger.getLogger(LogParser.class.getName());
        JSONParser parser = new JSONParser();
        ArrayList<LogEntry> logEntries;

        logger.info("Searching for file");

        try (FileReader reader = new FileReader(args[0]))
        {
            logger.info("Parsing JSON from file");

            Object obj = parser.parse(reader);

            JSONArray entryList = (JSONArray) obj;
            System.out.println(entryList);

            logger.info("Separating JSON into objects");

            logEntries = separateJSON(entryList);

            logger.info("Saving JSON objects to HSQLDB");

            try {
                Class.forName("org.hsqldb.jdbc.JDBCDriver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try {
                con = DriverManager.getConnection(connectionString, "SA", "");

                con.createStatement()
                        .executeUpdate(createLogTable);

                for(LogEntry le : logEntries){
                    insertLogEntry(con, le.getId(), le.getDuration(), le.getType(), le.getHost(), le.getAlert());
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<LogEntry> separateJSON(JSONArray entryList){
        ArrayList<LogEntry> logEntries = new ArrayList<LogEntry>();
        ArrayList<String> parsedIds = new ArrayList<String>();
        for (Object o : entryList) {
            JSONObject entry = (JSONObject) o;
            if (parsedIds.isEmpty() || !parsedIds.contains((String) entry.get("id"))) {
                LogEntry le = new LogEntry((String) entry.get("id"));
                if (((String) entry.get("state")).equals("STARTED")) {
                    le.setStart((Long) entry.get("timestamp"));
                } else {
                    le.setEnd((Long) entry.get("timestamp"));
                }
                if(entry.containsKey("type")) {
                    le.setType((String) entry.get("type"));
                }
                if(entry.containsKey("host")) {
                    le.setHost((String) entry.get("host"));
                }
                logEntries.add(le);
                parsedIds.add(le.getId());
            } else {
                int pos = parsedIds.indexOf((String) entry.get("id"));
                if (((String) entry.get("state")).equals("STARTED")) {
                    logEntries.get(pos).setStart((Long) entry.get("timestamp"));
                } else {
                    logEntries.get(pos).setEnd((Long) entry.get("timestamp"));
                }
            }
        }
        logEntries.forEach(LogEntry::calcDuration);
        logEntries.forEach(LogEntry::setAlert);
        return logEntries;
    }

    public static void insertLogEntry(Connection con, String id, Long duration, String type, String host, boolean alert) {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("INSERT INTO log_entries(id,durationMillis,type,host,alert) VALUES (?,?,?,?,?)");
            ps.setString(1, id);
            ps.setLong(2, duration);
            ps.setString(3, type);
            ps.setString(4, host);
            ps.setBoolean(5, alert);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
