import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.*;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class LogParser {

    static Connection con;
    static String connectionString = "jdbc:hsqldb:file:hsqldb/logDatabase";

    public static void main(String[] args) {
        String createLogTable = "create table if not exists log_entries (id varchar(30), durationMillis INTEGER, type varchar(45), host varchar(10), alert BOOLEAN)";
        Logger logger = Logger.getLogger(LogParser.class.getName());
        JSONParser parser = new JSONParser();
        ArrayList<LogEntry> logEntries = new ArrayList<LogEntry>();
        ArrayList<String> parsedIds = new ArrayList<String>();

        Scanner input = new Scanner(System.in);
        System.out.println("Welcome User!");
        System.out.println("Enter log file location: ");
        String myString = input.next();
//        String myString = "src/main/resources/exampleLogs1.txt";
        logger.info("Searching for file");
        try (FileReader reader = new FileReader(myString))
        {
            logger.info("Parsing JSON from file");
            Object obj = parser.parse(reader);

            JSONArray entryList = (JSONArray) obj;
            System.out.println(entryList);
            logger.info("Separating JSON into objects");
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
            logEntries.forEach(e -> System.out.println(e.getId() + " " + e.getStart() + " " + e.getEnd() + " " + e.getDuration() + " " + e.getAlert()));
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
//                    String insertQuery = "INSERT INTO `logEntries`(id,durationMillis,type,host,alert) VALUE ('"+le.getId()+"','"+le.getDuration()+"','"+le.getType()+"',"+le.getHost()+",'"+le.getAlert()+"');";
                    PreparedStatement ps = con.prepareStatement("INSERT INTO log_entries(id,durationMillis,type,host,alert) VALUES (?,?,?,?,?)");
                    ps.setString(1, le.getId());
                    ps.setLong(2, le.getDuration());
                    ps.setString(3, le.getType());
                    ps.setString(4, le.getHost());
                    ps.setBoolean(5, le.getAlert());
                    ps.executeUpdate();
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void InsertLogEntry() {

    }

}
