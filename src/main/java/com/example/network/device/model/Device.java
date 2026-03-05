package com.example.network.device.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "device")
public class Device {

    @Id
    @Column(name = "mac_address", nullable = false, updatable = false)
    private final String macAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private final DeviceType deviceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "uplink_mac_address",
            referencedColumnName = "mac_address",
            foreignKey = @ForeignKey(name = "fk_device_uplink")
    )
    private final Device uplink;

    public Device(String macAddress, DeviceType deviceType, Device uplink) {
        this.macAddress = macAddress;
        this.deviceType = deviceType;
        this.uplink = uplink;
    }

    protected Device() {
        this.macAddress = null;
        this.deviceType = null;
        this.uplink = null;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public Device getUplink() {
        return uplink;
    }

    public String getUplinkMacAddress() {
        return uplink != null ? uplink.getMacAddress() : null;
    }
}
