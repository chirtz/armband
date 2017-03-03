package de.chirtz.armband.i5lib;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.chirtz.armband.Alarm;
import de.chirtz.armband.common.SymbolMapper;
import de.chirtz.armband.common.Tools;


public class Band {


    private static final int ALERT_TYPE_CALL = 1;
    private static final int ALERT_TYPE_MESSAGE = 2;
    public static final int ALERT_TYPE_CLOUD = 3;
    public static final int ALERT_TYPE_ERROR = 4;
    private final static int LINE_LENGTH = 15;
    private final static int MAX_CHARS = 96;

    private static final int CMD_GRP_CONFIG = 1;
    private static final int CMD_GRP_DATALOG = 2;
    private static final int CMD_GRP_DEVICE = 0;
    private static final int CMD_GRP_MSG = 3;
    private static final int CMD_GRP_USER = 4;

    private static final int CMD_ID_CONFIG_GET_AC = 5;
    private static final int CMD_ID_CONFIG_GET_BLE = 3;
    private static final int CMD_ID_CONFIG_GET_HW_OPTION = 9;
    private static final int CMD_ID_CONFIG_GET_NMA = 7;
    private static final int CMD_ID_CONFIG_GET_TIME = 1;
    private static final int CMD_ID_CONFIG_SET_AC = 4;
    private static final int CMD_ID_CONFIG_SET_BLE = 2;
    private static final int CMD_ID_CONFIG_SET_HW_OPTION = 8;
    private static final int CMD_ID_CONFIG_SET_NMA = 6;
    private static final int CMD_ID_CONFIG_SET_TIME = 0;
    private static final int CMD_ID_CONFIG_SPORT_TYPE = 10;
    private static final int CMD_ID_CONFIG_SPORT_TYPE_GOAL = 11;
    private static final int CMD_ID_CONFIG_SPORT_TYPE_READ_GOAL = 12;
    private static final int CMD_ID_DATALOG_CLEAR_ALL = 2;
    private static final int CMD_ID_DATALOG_GET_BODY_PARAM = 1;
    private static final int CMD_ID_DATALOG_GET_CUR_DAY_DATA = 7;
    private static final int CMD_ID_DATALOG_SET_BODY_PARAM = 0;
    private static final int CMD_ID_DATALOG_START_GET_DAY_DATA = 3;
    private static final int CMD_ID_DATALOG_START_GET_MINUTE_DATA = 5;
    private static final int CMD_ID_DATALOG_STOP_GET_DAY_DATA = 4;
    private static final int CMD_ID_DATALOG_STOP_GET_MINUTE_DATA = 6;
    private static final int CMD_ID_DEVICE_GET_BATTERY = 1;
    private static final int CMD_ID_DEVICE_GET_INFORMATION = 0;
    private static final int CMD_ID_DEVICE_RESET = 2;
    private static final int CMD_ID_DEVICE_UPDATE = 3;
    private static final int CMD_ID_MSG_DOWNLOAD = 1;
    private static final int CMD_ID_MSG_MULTI_DOWNLOAD_CONTINUE = 3;
    private static final int CMD_ID_MSG_MULTI_DOWNLOAD_END = 4;
    private static final int CMD_ID_MSG_MULTI_DOWNLOAD_START = 2;
    private static final int CMD_ID_MSG_UPLOAD = 0;
    private static final int CMD_ID_PHONE_ALERT = 1;
    private static final int CMD_ID_PHONE_PRESSKEY = 0;
    private static final int CMD_PHONE_MSG = 4;
    private static final int PREFIX_NOTIFY = 65314;
    private static final int PREFIX_WRITE = 65313;

    private LocalBroadcastManager broadcastManager;
    private Context context;
    private final static String TAG = "Band";


    public Band(LocalBroadcastManager broadcastManager) {
        this.broadcastManager = broadcastManager;
    }

    public Band(Context context) {
        this.context = context;
    }

    private void sendRequest(int start, int end, long delay, byte[] data) {
        Intent i = new Intent(BluetoothLeService.BROADCAST);
        i.putExtra(BluetoothLeService.CMD, BluetoothLeService.CMD_WRITE);
        Command cmd = new Command(start, end, delay, data);
        i.putExtra(BluetoothLeService.COMMAND_PKG, cmd.getBundle());
        if (broadcastManager != null)
            broadcastManager.sendBroadcast(i);
        else
            context.sendBroadcast(i);
    }

    private void sendRequest(int start, int end, byte[] data) {
        sendRequest(start, end, 100, data);
    }

    private void sendRequest(int start, int end) {
        sendRequest(start, end, null);
    }

    public void requestPower() {
        sendRequest(CMD_GRP_DEVICE, CMD_ID_DEVICE_GET_BATTERY);
    }

    public void requestNMA() {
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_GET_NMA);
    }

    public void setNMA() {
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_SET_NMA);
    }


    public void requestVersionInfo() {
        sendRequest(CMD_GRP_DEVICE, CMD_ID_DEVICE_GET_INFORMATION);
    }

    public void supportSports() {
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_SPORT_TYPE);
    }

    public void unbindDevice() {
        sendRequest(CMD_GRP_DEVICE, 5);
    }

    public void updateDevice() {
        sendRequest(CMD_GRP_DEVICE, CMD_ID_DEVICE_UPDATE);
    }


    public void requestUserParams() {
        sendRequest(CMD_GRP_DATALOG, CMD_ID_DATALOG_GET_BODY_PARAM);
    }

    public void getSportsGoal(int sport) {
        byte[] data = {(byte) sport};
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_SPORT_TYPE_READ_GOAL, data);
    }

    public void requestBle() {
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_GET_BLE);
    }


    public void requestDayData() {
        sendRequest(CMD_GRP_DATALOG, CMD_ID_DATALOG_GET_CUR_DAY_DATA);
    }


    public void requestDate() {
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_GET_TIME);
    }

    public void requestConfig() {
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_GET_HW_OPTION);
    }


    public void subscribeForLocalSport() {
        sendRequest(CMD_GRP_DATALOG, CMD_ID_DATALOG_START_GET_MINUTE_DATA);
    }

    public void unsubscribeFromLocalSport() {
        sendRequest(CMD_GRP_DATALOG, CMD_ID_DATALOG_STOP_GET_MINUTE_DATA);
    }

    public void subscribeForSportUpdates(){
      sendRequest(CMD_GRP_DATALOG, CMD_ID_DATALOG_START_GET_DAY_DATA);
    }

    public void unsubscribeFromSportUpdates() {
        sendRequest(CMD_GRP_DATALOG, CMD_ID_DATALOG_STOP_GET_DAY_DATA);
    }

    public void clearSportData() {
        sendRequest(CMD_GRP_DATALOG, CMD_ID_DATALOG_CLEAR_ALL);
    }


    public void requestAlarm(int alarmID) {
        if (alarmID < 0 || alarmID > 6) {
            throw new IllegalArgumentException("Alarm ID must be in 0..6");
        }
        byte[] data = {(byte) alarmID};
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_GET_AC, data);
    }

    // id 0, 1, 2, 3, 4, 5, 6
    public void setAlarm(Alarm a){
        byte[] data = {
                (byte) a.getId(),
                0,
                (a.isEnabled() ? a.getWeekDays(): 0),
                (byte) a.getHour(),
                (byte) a.getMinute()
        };
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_SET_AC, data);
    }

    public void disableAlarm(int sectionId){
       byte[] data = {
                (byte) sectionId,
                0,
                (byte) 0,
                (byte) 0,
                (byte) 0
        };
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_SET_AC, data);
    }


    public void setBle(boolean enabled){
        byte[] data = {(byte) (enabled ? 1 : 0)};
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_SET_BLE, data);
    }

    public void setCameraUserInputMode(boolean enable){
        byte[] data = {enable ? (byte) 1 : (byte) 0};
        sendRequest(CMD_GRP_USER, 0, data);
    }

    public void setUserParams(int height, int weight, boolean gender, int age, int goal){
        byte[] data = new byte[6];
        data[0] = (byte)height;
        data[1] = (byte)weight;
        data[2] = (byte)( gender ? 1 : 0);
        data[3] = (byte)age;
        int goal_low = goal % AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
        int goal_high = (goal - goal_low) / AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
        data[4] = (byte) goal_low;
        data[5] = (byte) goal_high;
        sendRequest(CMD_GRP_DATALOG, CMD_ID_DATALOG_SET_BODY_PARAM, data);
    }


    public void setConfig(boolean light, boolean gesture, boolean englishUnits,
                          boolean use12hour, boolean autoSleep) {
        byte[] data = new byte[11];

        data[0] = light ?  (byte) 1 : 0;
        data[1] = gesture ? (byte) 1 : 0;
        data[2] = englishUnits ? (byte) 1 : 0;
        data[3] = use12hour ? (byte) 1 : 0;
        data[4] = autoSleep ? (byte) 1 : 0;

        data[5] = 1;
        data[6] = 8;
        data[7] = 20;

        data[8] = 0;
        data[9] = 0;
        data[10] = 0;
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_SET_HW_OPTION, data);
    }


    public void sendMessage(String msg){
        sendAlert(convertTextForDisplay(msg), ALERT_TYPE_MESSAGE);
    }

    public void resetDevice() {
        sendRequest(CMD_GRP_DEVICE, CMD_ID_DEVICE_RESET);
    }

    public static String convertTextForDisplay(String input) {
        input = SymbolMapper.replaceSymbols(input);
        input = input.replaceAll("\\s+"," ").trim();
        input = WordUtils.wrap(input, LINE_LENGTH, "\n", true, " ");
        StringBuilder sb = new StringBuilder();
        for (String s: input.split("\n")) {
            sb.append(StringUtils.rightPad(s.trim(), LINE_LENGTH+1));
        }
        return StringUtils.abbreviate(sb.toString(), MAX_CHARS);
    }

    private void sendAlert(String msg, int type){
        if( msg == null )
            return ;
        byte[] buffer = new byte[0];
        try
        {
            buffer = msg.getBytes("utf-8");
        }
        catch(UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        }

        byte[] data = new byte[2+buffer.length];
        data[0] = (byte) type;
        data[1] = (byte) -1;

        System.arraycopy(buffer, 0, data, 2, buffer.length);

        byte[] d = Tools.getDataByte(Tools.getCommandHeader(CMD_GRP_MSG, CMD_ID_PHONE_ALERT), data);
        for (int i = 0; i < d.length; i += 20) {
            byte[] writeData;
            if (i + 20 > d.length) {
                writeData = Arrays.copyOfRange(d, i, d.length);
            } else {
                writeData = Arrays.copyOfRange(d, i, i + 20);
            }
            sendRequest(-1, -1, 200, writeData);
        }

    }

    public void sendCall(String msg){
        sendAlert(msg, ALERT_TYPE_CALL);
    }

    public void sendCallEnd() {
        byte[] data = {0};
        sendRequest(CMD_GRP_USER, 1, data);
    }

    public void setDate() {
        GregorianCalendar date = new GregorianCalendar();
        byte[] data = new byte[6];
        data[0] =  ((byte)(date.get(Calendar.YEAR) - 2000)) ;
        data[1] =  ((byte)(date.get(Calendar.MONTH))) ;
        data[2] =  ((byte)(date.get(Calendar.DAY_OF_MONTH)-1)) ;
        data[3] =  ((byte)date.get(Calendar.HOUR_OF_DAY)) ;
        data[4] =  ((byte)date.get(Calendar.MINUTE)) ;
        data[5] =  ((byte)date.get(Calendar.SECOND)) ;
        sendRequest(CMD_GRP_CONFIG, CMD_ID_CONFIG_SET_TIME, data);
    }


}
