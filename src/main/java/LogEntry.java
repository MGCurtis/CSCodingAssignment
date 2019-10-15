public class LogEntry {
    private String id;
    private long start;
    private long end;
    private long duration;
    private String type;
    private String host;
    private Boolean alert;

    public LogEntry(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getDuration() {
        return duration;
    }

    public void setId(int duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert() {
        if(this.duration > 4){
            this.alert = true;
        }
        else{
            this.alert = false;
        };
    }

    public void calcDuration() {
        this.duration = this.end - this.start;
    }
}
