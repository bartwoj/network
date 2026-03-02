package com.example.network.device.response;

import com.example.network.device.model.Device;

public class DeviceEntry {

    private final String deviceType;
    private final String macAddress;

    public DeviceEntry(String deviceType, String macAddress) {
        this.deviceType = deviceType;
        this.macAddress = macAddress;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public static DeviceEntry fromDevice(Device device) {
        return  new DeviceEntry(device.getDeviceType().name(), device.getMacAddress());
    }
}
