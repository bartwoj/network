package com.example.network.device.controller;

import com.example.network.device.model.Device;
import com.example.network.device.response.DeviceEntry;
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
    public ResponseEntity<Device> registerDevice(
            @RequestParam String deviceType,
            @RequestParam String macAddress,
            @RequestParam(required = false) String uplinkMacAddress
    ) {
        return ResponseEntity.ok(
                deviceService.registerDevice(deviceType, macAddress, uplinkMacAddress)
        );
    }

    @GetMapping("/retrieveAll")
    public ResponseEntity<Collection<DeviceEntry>> retrieveAllDevices() {
        return ResponseEntity.ok(
                deviceService.retrieveAllSorted()
        );
    }
}
