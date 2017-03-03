package de.chirtz.armband.common;


interface ModifyAlarmFragmentListener {
    void onTimeChanged(int hour, int minute);
    void onDaysChanged(byte days);
}