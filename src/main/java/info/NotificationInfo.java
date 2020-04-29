package com.example.dnjsr.bakingcat.info;

public class NotificationInfo {
    public String token;
    public Notification notification = new Notification();
    public Data data = new Data();

    public static class Notification{
        public String title;
        public String text;
    }

    public static class Data{
        public String title;
        public String text;
    }
}
