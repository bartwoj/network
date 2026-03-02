package com.example.network.device.service;

import com.example.network.device.model.Device;

import java.util.Comparator;

public class DeviceUtils {

    public static Comparator<Device> getSortingComparator() {
        return Comparator.comparingInt(d -> d.getDeviceType().getSortingOrder());
    }
}
