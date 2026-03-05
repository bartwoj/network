package com.example.network.device.controller;

import com.example.network.device.response.DeviceEntry;
import com.example.network.device.response.DeviceTreeNode;
import com.example.network.device.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@Controller("/device")
public class DeviceController {

    DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register")
    public ResponseEntity<DeviceEntry> registerDevice(
            @RequestParam String macAddress,
            @RequestParam String deviceType,
            @RequestParam(required = false) String uplinkMacAddress
    ) {
        return ResponseEntity.ok(
                deviceService.registerDevice(macAddress, deviceType, uplinkMacAddress)
        );
    }

    @GetMapping("/retrieve")
    public ResponseEntity<DeviceEntry> retrieveDevice(@RequestParam String macAddress) {
        return ResponseEntity.ok(
                deviceService.retrieve(macAddress)
        );
    }

    @GetMapping("/retrieveAll")
    public ResponseEntity<Collection<DeviceEntry>> retrieveAllDevicesSorted() {
        return ResponseEntity.ok(
                deviceService.retrieveAllSorted()
        );
    }

    @GetMapping("/retrieveOneAsTree")
    public ResponseEntity<DeviceTreeNode> retrieveDeviceAsTree(@RequestParam String macAddress) {
        return ResponseEntity.ok(
                deviceService.retrieveOneAsTree(macAddress)
        );
    }

    @GetMapping("/retrieveAllAsTree")
    public ResponseEntity<Collection<DeviceTreeNode>> retrieveAllDevicesAsTree() {
        return ResponseEntity.ok(
                deviceService.retrieveAllAsTree()
        );
    }

}
