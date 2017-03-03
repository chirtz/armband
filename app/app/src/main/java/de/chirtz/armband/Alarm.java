package de.chirtz.armband;

import android.annotation.SuppressLint;

public class Alarm {

    private int hour, minute;
    private boolean enabled;
    private byte weekDays;
    private final int id;

    public Alarm(int id, byte weekDays, int hour, int minute, boolean enabled) {
        setData(weekDays, hour, minute, enabled);
        this.id = id;
    }

    public void setData(byte weekDays, int hour, int minute) {
        if (hour < 0 || hour > 23)
            throw new IllegalArgumentException("Hour not in range (0..23)");
        if (minute < 0 || minute > 59)
            throw new IllegalArgumentException("Minute not in range (0..59)");
        this.weekDays = weekDays;
        this.hour = hour;
        this.minute = minute;
    }

    private void setData(byte weekDays, int hour, int minute, boolean enabled) {
        setData(weekDays, hour, minute);
        this.enabled = enabled;
    }

    public void setEnabled(boolean enable) {
        this.enabled = enable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public byte getWeekDays() {
        return weekDays;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getId() {
        return id;
    }

    @SuppressLint("DefaultLocale")
    public String getTimeString() {
        return String.format("%02d:%02d", hour, minute);
    }

    @SuppressLint("DefaultLocale")
    public String toString() {
        return String.format("%d/%d/%d/%d", weekDays, hour, minute, enabled ? 1: 0);
    }

    public static Alarm fromString(int id, String input) {
        String[] split = input.split("/");
        return new Alarm(id, Byte.parseByte(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])==1);
    }


}
