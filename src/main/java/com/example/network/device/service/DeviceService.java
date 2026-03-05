package com.example.network.device.service;

import com.example.network.device.model.DeviceType;
import com.example.network.device.response.DeviceEntry;
import com.example.network.device.response.DeviceTreeNode;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public interface DeviceService {
    @Nullable DeviceEntry registerDevice(String macAddress, String deviceType, String uplinkMacAddress);

    @Nullable DeviceEntry registerDevice(String macAddress, DeviceType deviceType, String uplinkMacAddress);

    DeviceEntry retrieve(String macAddress);

    Collection<DeviceEntry> retrieveAllSorted();

    DeviceTreeNode retrieveOneAsTree(String macAddress);

    Collection<DeviceTreeNode> retrieveAllAsTree();
}
