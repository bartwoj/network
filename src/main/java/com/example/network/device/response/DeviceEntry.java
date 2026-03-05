package com.example.network.device.response;

import com.example.network.device.model.Device;

public class DeviceEntry {

    private final String macAddress;
    private final String deviceType;

    public DeviceEntry(String macAddress, String deviceType) {
        this.macAddress = macAddress;
        this.deviceType = deviceType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public static DeviceEntry fromDevice(Device device) {
        return  new DeviceEntry(device.getMacAddress(), device.getDeviceType().name());
    }
}
