package me.lalayu.serializer;

import java.io.Serializable;

/**
 *
 **/
public class TestCustomBody implements Serializable {

    int id;

    String name;

    String content;

    public TestCustomBody(int id, String name, String content) {
        this.content = content;
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "" + id + name + content;
    }
}
