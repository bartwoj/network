package com.example.network.device.service;

import com.example.network.device.model.Device;
import com.example.network.device.model.DeviceType;
import com.example.network.device.response.DeviceEntry;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public interface DeviceService {
    @Nullable Device registerDevice(String deviceType, String macAddress, String uplinkMacAddress);

    @Nullable Device registerDevice(DeviceType deviceType, String macAddress, String uplinkMacAddress);

    Collection<DeviceEntry> retrieveAllSorted();
}
