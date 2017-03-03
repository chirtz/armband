package de.chirtz.armband.i5lib;

import android.content.Intent;
import android.os.Bundle;

import de.chirtz.armband.common.Bundleable;


class Command extends Bundleable {

    private final static String START = "start";
    private final static String END = "end";
    private final static String DELAY = "delay";
    private final static String PAYLOAD = "payload";


    public Command(int start, int end, long delay) {
        super();
        data.putInt(START, start);
        data.putInt(END, end);
        data.putLong(DELAY, delay);

    }

    public Command(int start, int end, long delay, byte[] payload) {
        this(start, end, delay);
        data.putByteArray(PAYLOAD, payload);
    }

    public int getStart() {
        return data.getInt(START);
    }

    public int getEnd() {
        return data.getInt(END);
    }

    public long getDelay() {
        return data.getLong(DELAY);
    }

    public byte[] getPayload() {
        return data.getByteArray(PAYLOAD);
    }

    public Command(long delay, byte[] data) {
        this(-1, -1, delay, data);
    }


    public Command(Intent newData) {
        super(newData.getExtras());
    }

    public Command(Bundle newData) {
        super(newData);
    }
}
