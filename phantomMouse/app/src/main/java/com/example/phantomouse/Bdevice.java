package com.example.phantomouse;

public class Bdevice {
    public String deviceName;
    public int connectionState;

    public Bdevice(String deviceName, int connectionState){
        this.deviceName = deviceName;
        this.connectionState = connectionState;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName){
        this.deviceName = deviceName;
    }

    public  String getConnectionStatus(){
        switch (connectionState){
            case 0:
                return "not connected";
            case 1:
                return "connecting...";
            case 2:
                return "connected";
            default:
                return "undefined";
        }
    }

    public void  setConnectionState(int connectionState){
        this.connectionState = connectionState;
    }

}
