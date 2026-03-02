package com.example.network.device.repository;

import com.example.network.device.model.DeviceType;
import org.springframework.stereotype.Component;

@Component
public class DeviceTypeRepository {

    public DeviceType findByName(String deviceType) {
        return DeviceType.valueOf(deviceType);
    }
}
