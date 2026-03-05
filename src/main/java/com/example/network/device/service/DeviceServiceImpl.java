package com.example.network.device.service;

import com.example.network.device.model.Device;
import com.example.network.device.model.DeviceType;
import com.example.network.device.repository.DeviceRepository;
import com.example.network.device.repository.DeviceTypeRepository;
import com.example.network.device.response.DeviceEntry;
import com.example.network.device.response.DeviceTreeNode;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository, DeviceTypeRepository deviceTypeRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceTypeRepository = deviceTypeRepository;
    }

    @Override
    public @Nullable DeviceEntry registerDevice(String macAddress, String deviceType, String uplinkMacAddress) {
        return this.registerDevice(macAddress, deviceTypeRepository.findByName(deviceType), uplinkMacAddress);
    }

    @Override
    public @Nullable DeviceEntry registerDevice(String macAddress, DeviceType deviceType, String uplinkMacAddress) {
        Device uplink = null;
        if (uplinkMacAddress != null) {
            uplink = deviceRepository.findById(uplinkMacAddress)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Uplink device not found: " + uplinkMacAddress
                    ));
        }

        return DeviceEntry.fromDevice(
                deviceRepository.save(
                        new Device(macAddress, deviceType, uplink)
                )
        );
    }

    @Override
    public DeviceEntry retrieve(String macAddress) {
        return deviceRepository.findById(macAddress)
                .map(DeviceEntry::fromDevice)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + macAddress));
    }

    @Override
    public Collection<DeviceEntry> retrieveAllSorted() {
        return deviceRepository.findAll().stream()
                .sorted(DeviceUtils.getSortingComparator())
                .map(DeviceEntry::fromDevice)
                .toList();
    }

    @Override
    public DeviceTreeNode retrieveOneAsTree(String macAddress) {
        Device device = deviceRepository.findById(macAddress)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + macAddress));

        return buildUplinkChain(device);
    }

    @Override
    public Collection<DeviceTreeNode> retrieveAllAsTree() {
        List<Device> allDevices = deviceRepository.findAll();

        Set<String> uplinkMacs = allDevices.stream()
                .map(Device::getUplinkMacAddress)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return allDevices.stream()
                .filter(d -> !uplinkMacs.contains(d.getMacAddress()))
                .map(this::buildUplinkChain)
                .toList();
    }


    private DeviceTreeNode buildUplinkChain(Device device) {
        DeviceTreeNode node = new DeviceTreeNode(device.getMacAddress(), device.getDeviceType().name());

        if (device.getUplink() != null) {
            node.setUplink(buildUplinkChain(device.getUplink()));
        }

        return node;
    }

}
