import org.junit.Test;

import static org.junit.Assert.*;

public class LogEntryTest {
    LogEntry entry = new LogEntry("1");


    @Test
    public void setAlertShouldBeTrue() {
        entry.setStart(12345);
        entry.setEnd(12352);
        entry.calcDuration();
        entry.setAlert();
        assertEquals(true, entry.getAlert());
    }

    @Test
    public void setAlertShouldBeFalse() {
        entry.setStart(12345);
        entry.setEnd(12348);
        entry.calcDuration();
        entry.setAlert();
        assertEquals(false, entry.getAlert());
    }

    @Test
    public void calcDuration() {
        entry.setStart(12345);
        entry.setEnd(12347);
        entry.calcDuration();
        assertEquals(2, entry.getDuration());
    }
}