package net.llamadevelopment.surveysystem.components.utils;

public class SurveyUtil {

    private String title;
    private String text;
    private String status;
    private String id;
    private long time;
    private int yes;
    private int no;
    private int all;

    public SurveyUtil(String title, String text, String status, String id, long time, int yes, int no, int all) {
        this.title = title;
        this.text = text;
        this.status = status;
        this.id = id;
        this.time = time;
        this.yes = yes;
        this.no = no;
        this.all = all;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public int getYes() {
        return yes;
    }

    public int getNo() {
        return no;
    }

    public int getAll() {
        return all;
    }
}
