package de.chirtz.armband.common;

import android.util.SparseBooleanArray;

import com.google.common.primitives.Bytes;

public class Tools {

    public static byte weekDaysToByte(SparseBooleanArray ar) {
        int b = 0;
        for (int j=0; j<7; j++) {
            b |= ((ar.get(j, false) ? 1: 0) << 6-j);
        }
        return (byte)(128+b);
    }

    public static boolean[] byteToWeekDays(byte b) {
        boolean[] weekDays = new boolean[7];
        for (int j=0; j<7; j++) {
            weekDays[j] = ((b & (1L << (6-j))) != 0);
        }
        return weekDays;
    }

    public static byte weekDaysToByte(boolean[] ar) {
        if (ar.length != 7)
            throw new IllegalArgumentException("Array must have length 7");
        int b = 0;
        for (int j=0; j<7; j++) {
            b |= ((ar[j] ? 1: 0) << 6-j);
        }
        return (byte)(128+b);
    }

    public static int bytesToInt(byte[] bytes) {
        switch(bytes.length) {
            case 1:
                return bytes[0] & 255;
            case 2:
                return (bytes[0] & 255) | ((bytes[1] << 8) & 65280);
            case 3:
                return ((bytes[0] & 255) | ((bytes[1] << 8) & 65280)) | ((bytes[2] << 16) & 16711680);
            case 4:
                return (((bytes[0] & 255) | ((bytes[1] << 8) & 65280)) | ((bytes[2] << 16) & 16711680)) | ((bytes[3] << 24) & -16777216);
            default:
                return 0;
        }
    }


    public static byte getCommandHeader(int group, int command) {
        return (byte) (((((byte) group) & 15) << 4) | (((byte) command) & 15));
    }


    public static byte[] getDataByte(byte header, byte[] data) {
        byte[] dat = new byte[4];
        dat[0] = (byte) 33;
        dat[1] = (byte) -1;
        dat[2] = header;
        if (data != null) {
            dat[3] = (byte) data.length;
            return Bytes.concat(dat, data);
        }
        dat[3] = (byte) 0;
        return dat;
    }

    public static String byteToMacString(byte[] copyOfRange) {
        StringBuilder sb = new StringBuilder();
        for (byte b : copyOfRange) {
            String i = Integer.toHexString(b & 255);
            if (i.length() == 1) {
                sb.append(0);
            }
            sb.append(i);
        }
        return sb.toString();
    }


}