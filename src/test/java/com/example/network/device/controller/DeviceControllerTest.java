package com.example.network.device.controller;

import com.example.network.device.model.DeviceType;
import com.example.network.device.response.DeviceEntry;
import com.example.network.device.response.DeviceTreeNode;
import com.example.network.device.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    // -------------------------------------------------------------------------
    // POST /device/register
    // -------------------------------------------------------------------------

    @Test
    void registerDevice_withUplink_returns200() throws Exception {
        DeviceEntry entry = new DeviceEntry("AA:02", DeviceType.SWITCH.name());
        when(deviceService.registerDevice("AA:02", "SWITCH", "AA:01")).thenReturn(entry);

        mockMvc.perform(post("/device/register")
                        .param("macAddress", "AA:02")
                        .param("deviceType", "SWITCH")
                        .param("uplinkMacAddress", "AA:01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.macAddress").value("AA:02"))
                .andExpect(jsonPath("$.deviceType").value("SWITCH"));
    }

    @Test
    void registerDevice_withoutUplink_returns200() throws Exception {
        DeviceEntry entry = new DeviceEntry("AA:01", DeviceType.GATEWAY.name());
        when(deviceService.registerDevice("AA:01", "GATEWAY", null)).thenReturn(entry);

        mockMvc.perform(post("/device/register")
                        .param("macAddress", "AA:01")
                        .param("deviceType", "GATEWAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.macAddress").value("AA:01"))
                .andExpect(jsonPath("$.deviceType").value("GATEWAY"));
    }

    @Test
    void registerDevice_serviceThrows_returns400() throws Exception {
        when(deviceService.registerDevice("AA:02", "SWITCH", "AA:99"))
                .thenThrow(new IllegalArgumentException("Uplink device not found: AA:99"));

        mockMvc.perform(post("/device/register")
                        .param("macAddress", "AA:02")
                        .param("deviceType", "SWITCH")
                        .param("uplinkMacAddress", "AA:99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerDevice_missingRequiredParam_returns400() throws Exception {
        mockMvc.perform(post("/device/register")
                        .param("deviceType", "SWITCH"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /device/retrieve
    // -------------------------------------------------------------------------

    @Test
    void retrieveDevice_existingDevice_returns200() throws Exception {
        DeviceEntry entry = new DeviceEntry("AA:01", DeviceType.GATEWAY.name());
        when(deviceService.retrieve("AA:01")).thenReturn(entry);

        mockMvc.perform(get("/device/retrieve")
                        .param("macAddress", "AA:01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.macAddress").value("AA:01"))
                .andExpect(jsonPath("$.deviceType").value("GATEWAY"));
    }

    @Test
    void retrieveDevice_notFound_returns400() throws Exception {
        when(deviceService.retrieve("AA:99"))
                .thenThrow(new IllegalArgumentException("Device not found: AA:99"));

        mockMvc.perform(get("/device/retrieve")
                        .param("macAddress", "AA:99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retrieveDevice_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/device/retrieve"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /device/retrieveAll
    // -------------------------------------------------------------------------

    @Test
    void retrieveAllDevicesSorted_returns200WithList() throws Exception {
        when(deviceService.retrieveAllSorted()).thenReturn(List.of(
                new DeviceEntry("AA:01", DeviceType.GATEWAY.name()),
                new DeviceEntry("AA:02", DeviceType.SWITCH.name()),
                new DeviceEntry("AA:03", DeviceType.ACCESS_POINT.name())
        ));

        mockMvc.perform(get("/device/retrieveAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].macAddress").value("AA:01"))
                .andExpect(jsonPath("$[1].macAddress").value("AA:02"))
                .andExpect(jsonPath("$[2].macAddress").value("AA:03"));
    }

    @Test
    void retrieveAllDevicesSorted_emptyList_returns200WithEmptyArray() throws Exception {
        when(deviceService.retrieveAllSorted()).thenReturn(List.of());

        mockMvc.perform(get("/device/retrieveAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /device/retrieveOneAsTree
    // -------------------------------------------------------------------------

    @Test
    void retrieveDeviceAsTree_leafDevice_returnsNestedUplinks() throws Exception {
        DeviceTreeNode gateway = new DeviceTreeNode("AA:01", "GATEWAY");
        DeviceTreeNode switch_ = new DeviceTreeNode("AA:02", "SWITCH");
        DeviceTreeNode ap      = new DeviceTreeNode("AA:03", "ACCESS_POINT");
        switch_.setUplink(gateway);
        ap.setUplink(switch_);

        when(deviceService.retrieveOneAsTree("AA:03")).thenReturn(ap);

        mockMvc.perform(get("/device/retrieveOneAsTree")
                        .param("macAddress", "AA:03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.macAddress").value("AA:03"))
                .andExpect(jsonPath("$.uplink.macAddress").value("AA:02"))
                .andExpect(jsonPath("$.uplink.uplink.macAddress").value("AA:01"))
                .andExpect(jsonPath("$.uplink.uplink.uplink").doesNotExist());
    }

    @Test
    void retrieveDeviceAsTree_rootDevice_returnsNullUplink() throws Exception {
        DeviceTreeNode gateway = new DeviceTreeNode("AA:01", "GATEWAY");

        when(deviceService.retrieveOneAsTree("AA:01")).thenReturn(gateway);

        mockMvc.perform(get("/device/retrieveOneAsTree")
                        .param("macAddress", "AA:01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.macAddress").value("AA:01"))
                .andExpect(jsonPath("$.uplink").doesNotExist());
    }

    @Test
    void retrieveDeviceAsTree_notFound_returns400() throws Exception {
        when(deviceService.retrieveOneAsTree("AA:99"))
                .thenThrow(new IllegalArgumentException("Device not found: AA:99"));

        mockMvc.perform(get("/device/retrieveOneAsTree")
                        .param("macAddress", "AA:99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retrieveDeviceAsTree_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/device/retrieveOneAsTree"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /device/retrieveAllAsTree
    // -------------------------------------------------------------------------

    @Test
    void retrieveAllDevicesAsTree_returns200WithList() throws Exception {
        DeviceTreeNode gateway = new DeviceTreeNode("AA:01", "GATEWAY");
        DeviceTreeNode switch_ = new DeviceTreeNode("AA:02", "SWITCH");
        switch_.setUplink(gateway);

        DeviceTreeNode loose = new DeviceTreeNode("BB:01", "SWITCH");

        when(deviceService.retrieveAllAsTree()).thenReturn(List.of(switch_, loose));

        mockMvc.perform(get("/device/retrieveAllAsTree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].macAddress").value("AA:02"))
                .andExpect(jsonPath("$[0].uplink.macAddress").value("AA:01"))
                .andExpect(jsonPath("$[1].macAddress").value("BB:01"))
                .andExpect(jsonPath("$[1].uplink").doesNotExist());
    }

    @Test
    void retrieveAllDevicesAsTree_emptyList_returns200WithEmptyArray() throws Exception {
        when(deviceService.retrieveAllAsTree()).thenReturn(List.of());

        mockMvc.perform(get("/device/retrieveAllAsTree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}