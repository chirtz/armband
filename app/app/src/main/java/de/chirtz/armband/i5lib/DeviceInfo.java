package de.chirtz.armband.i5lib;

import android.os.Parcel;
import android.os.Parcelable;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;

import de.chirtz.armband.common.Tools;

public class DeviceInfo implements Parcelable {
    private final String model;
    private final int oadmode;
    private final String swversion;
    private final String bleAddr;
    private final int displayWidthFont;
    private String name;


    public String toString(){
        String buff = "";
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
           // if ( (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL )
            //    continue;
            try {
                buff += field.getName() + ": " + field.get(this) + " ";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return buff;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getModel(){
        return this.model;
    }


    public String getName() {
        return name;
    }

    public int getOadmode(){
        return this.oadmode;
    }


    public String getSwversion(){
        return this.swversion;
    }


    public String getBleAddr(){
        return this.bleAddr;
    }

    public int getDisplayWidthFont(){
        return this.displayWidthFont;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static DeviceInfo fromData(byte[] data){
        String model = new String(Arrays.copyOfRange(data, 6, 10));
        int oadMode = (data[10] * 255) + data[11];
        String swVersion = data[12] + "." + data[13] + "." + data[14] + "." + data[15];
        String bleAddr = Tools.byteToMacString(Arrays.copyOfRange(data, 16, 22));
        int fontWidth = 0;
        if (data.length == 29)
            fontWidth = Tools.bytesToInt(Arrays.copyOfRange(data, 28, 29));
        else if (data.length == 28)
            fontWidth = Tools.bytesToInt(Arrays.copyOfRange(data, 27, 28));
        return new DeviceInfo(model, oadMode, bleAddr, swVersion, fontWidth, "");
    }



    public DeviceInfo(String model, int oadmode, String bleAddr, String swversion, int displayWidthFont, String name) {
        this.model = model;
        this.oadmode = oadmode;
        this.bleAddr = bleAddr;
        this.swversion = swversion;
        this.displayWidthFont = displayWidthFont;
        this.name = name;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(model);
        parcel.writeInt(oadmode);
        parcel.writeString(bleAddr);
        parcel.writeString(swversion);
        parcel.writeInt(displayWidthFont);
        parcel.writeString(name);
    }

    static final Parcelable.Creator<DeviceInfo> CREATOR
            = new Parcelable.Creator<DeviceInfo>() {

        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in.readString(), in.readInt(), in.readString(), in.readString(), in.readInt(), in.readString());
        }

        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };
}