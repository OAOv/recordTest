package com.example.recordtest;

public class JsonDataList {
    int index;
    String account, title, description, time;

    public JsonDataList() {}

    public JsonDataList(int index, String account, String title, String description, String time) {
        this.index = index;
        this.account = account;
        this.title = title;
        this.description = description;
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public String getAccount() {
        return account;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
