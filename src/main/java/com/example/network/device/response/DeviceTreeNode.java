package com.example.network.device.response;

public class DeviceTreeNode {
    private final String macAddress;
    private final String deviceType;
    private DeviceTreeNode uplink;

    public DeviceTreeNode(String macAddress, String deviceType) {
        this.macAddress = macAddress;
        this.deviceType = deviceType;
        this.uplink = null;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setUplink(DeviceTreeNode uplink) {
        this.uplink = uplink;
    }

    public DeviceTreeNode getUplink() {
        return this.uplink;
    }
}