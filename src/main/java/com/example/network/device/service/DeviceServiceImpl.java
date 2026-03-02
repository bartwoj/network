package com.example.network.device.service;

import com.example.network.device.model.Device;
import com.example.network.device.model.DeviceType;
import com.example.network.device.repository.DeviceRepository;
import com.example.network.device.repository.DeviceTypeRepository;
import com.example.network.device.response.DeviceEntry;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository, DeviceTypeRepository deviceTypeRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceTypeRepository = deviceTypeRepository;
    }

    @Override
    public @Nullable Device registerDevice(String deviceType, String macAddress, String uplinkMacAddress) {
        return this.registerDevice(deviceTypeRepository.findByName(deviceType), macAddress, uplinkMacAddress);
    }

    @Override
    public @Nullable Device registerDevice(DeviceType deviceType, String macAddress, String uplinkMacAddress) {
        return deviceRepository.save(new Device(deviceType, macAddress, uplinkMacAddress));
    }

    @Override
    public Collection<DeviceEntry> retrieveAllSorted() {
        return deviceRepository.findAll().stream()
                .sorted(DeviceUtils.getSortingComparator())
                .map(DeviceEntry::fromDevice)
                .toList();
    }

}
