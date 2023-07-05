package me.xmrvizzy.skyblocker.utils.title;

public class Title {
    public String text = "";
    public boolean active = true;
    public int color;
    public float lastX = 0;

    public Title(String text, int color) {
        this.text = text;
        this.color = color;
    }
}
