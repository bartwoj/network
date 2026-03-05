package com.example.network.device.service;

import com.example.network.device.model.Device;
import com.example.network.device.model.DeviceType;
import com.example.network.device.repository.DeviceRepository;
import com.example.network.device.repository.DeviceTypeRepository;
import com.example.network.device.response.DeviceEntry;
import com.example.network.device.response.DeviceTreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceTypeRepository deviceTypeRepository;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private Device gatewayDevice;
    private Device switchDevice;
    private Device apDevice;

    @BeforeEach
    void setUp() {
        gatewayDevice = new Device("AA:01", DeviceType.GATEWAY,      null);
        switchDevice  = new Device("AA:02", DeviceType.SWITCH,        gatewayDevice);
        apDevice      = new Device("AA:03", DeviceType.ACCESS_POINT,  switchDevice);
    }

    // -------------------------------------------------------------------------
    // registerDevice
    // -------------------------------------------------------------------------

    @Test
    void registerDevice_withUplink_savesAndReturnsEntry() {
        when(deviceRepository.findById("AA:01")).thenReturn(Optional.of(gatewayDevice));
        when(deviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeviceEntry result = deviceService.registerDevice("AA:02", DeviceType.SWITCH, "AA:01");

        assertThat(result).isNotNull();
        assertThat(result.getMacAddress()).isEqualTo("AA:02");
        verify(deviceRepository).save(argThat(d -> d.getUplinkMacAddress().equals("AA:01")));
    }

    @Test
    void registerDevice_withoutUplink_savesRootDevice() {
        when(deviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeviceEntry result = deviceService.registerDevice("AA:01", DeviceType.GATEWAY, null);

        assertThat(result).isNotNull();
        assertThat(result.getMacAddress()).isEqualTo("AA:01");
        verify(deviceRepository).save(argThat(d -> d.getUplinkMacAddress() == null));
    }

    @Test
    void registerDevice_uplinkNotFound_throwsException() {
        when(deviceRepository.findById("AA:99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.registerDevice("AA:02", DeviceType.SWITCH, "AA:99"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AA:99");
    }

    @Test
    void registerDevice_byTypeName_resolvesTypeAndSaves() {
        when(deviceTypeRepository.findByName("GATEWAY")).thenReturn(DeviceType.GATEWAY);
        when(deviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeviceEntry result = deviceService.registerDevice("AA:01", "GATEWAY", null);

        assertThat(result).isNotNull();
        verify(deviceTypeRepository).findByName("GATEWAY");
    }

    // -------------------------------------------------------------------------
    // retrieve
    // -------------------------------------------------------------------------

    @Test
    void retrieve_existingDevice_returnsEntry() {
        when(deviceRepository.findById("AA:01")).thenReturn(Optional.of(gatewayDevice));

        DeviceEntry result = deviceService.retrieve("AA:01");

        assertThat(result.getMacAddress()).isEqualTo("AA:01");
    }

    @Test
    void retrieve_notFound_throwsException() {
        when(deviceRepository.findById("AA:99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.retrieve("AA:99"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AA:99");
    }

    // -------------------------------------------------------------------------
    // retrieveAllSorted
    // -------------------------------------------------------------------------

    @Test
    void retrieveAllSorted_returnsAllDevices() {
        when(deviceRepository.findAll()).thenReturn(List.of(gatewayDevice, switchDevice, apDevice));

        Collection<DeviceEntry> result = deviceService.retrieveAllSorted();

        assertThat(result).hasSize(3);
    }

    @Test
    void retrieveAllSorted_emptyRepository_returnsEmptyList() {
        when(deviceRepository.findAll()).thenReturn(List.of());

        Collection<DeviceEntry> result = deviceService.retrieveAllSorted();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // retrieveAllAsTree
    // -------------------------------------------------------------------------

    @Test
    void retrieveAllAsTree_returnsOnlyLeafDevicesAtTopLevel() {
        when(deviceRepository.findAll()).thenReturn(List.of(gatewayDevice, switchDevice, apDevice));

        Collection<DeviceTreeNode> tree = deviceService.retrieveAllAsTree();

        // only AP is not referenced as anyone's uplink → single entry point
        assertThat(tree).hasSize(1);
        assertThat(tree.iterator().next().getMacAddress()).isEqualTo("AA:03");
    }

    @Test
    void retrieveAllAsTree_structureIsCorrect() {
        when(deviceRepository.findAll()).thenReturn(List.of(gatewayDevice, switchDevice, apDevice));

        DeviceTreeNode leaf = deviceService.retrieveAllAsTree().iterator().next();

        // AP → Switch → Gateway
        assertThat(leaf.getMacAddress()).isEqualTo("AA:03");
        assertThat(leaf.getUplink().getMacAddress()).isEqualTo("AA:02");
        assertThat(leaf.getUplink().getUplink().getMacAddress()).isEqualTo("AA:01");
        assertThat(leaf.getUplink().getUplink().getUplink()).isNull();
    }

    @Test
    void retrieveAllAsTree_multipleLeaves_allAppearAtTopLevel() {
        Device apDevice2 = new Device("AA:04", DeviceType.ACCESS_POINT, switchDevice);
        when(deviceRepository.findAll()).thenReturn(List.of(gatewayDevice, switchDevice, apDevice, apDevice2));

        Collection<DeviceTreeNode> tree = deviceService.retrieveAllAsTree();

        assertThat(tree).hasSize(2);
        assertThat(tree.stream().map(DeviceTreeNode::getMacAddress))
                .containsExactlyInAnyOrder("AA:03", "AA:04");
    }

    @Test
    void retrieveAllAsTree_emptyRepository_returnsEmptyList() {
        when(deviceRepository.findAll()).thenReturn(List.of());

        Collection<DeviceTreeNode> tree = deviceService.retrieveAllAsTree();

        assertThat(tree).isEmpty();
    }

    @Test
    void retrieveAllAsTree_singleDeviceWithNoUplink_returnsThatDevice() {
        when(deviceRepository.findAll()).thenReturn(List.of(gatewayDevice));

        Collection<DeviceTreeNode> tree = deviceService.retrieveAllAsTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.iterator().next().getMacAddress()).isEqualTo("AA:01");
        assertThat(tree.iterator().next().getUplink()).isNull();
    }

    // -------------------------------------------------------------------------
    // retrieveOneAsTree
    // -------------------------------------------------------------------------

    @Test
    void retrieveOneAsTree_leafDevice_buildsFullUplinkChain() {
        when(deviceRepository.findById("AA:03")).thenReturn(Optional.of(apDevice));

        DeviceTreeNode result = deviceService.retrieveOneAsTree("AA:03");

        // AP → Switch → Gateway
        assertThat(result.getMacAddress()).isEqualTo("AA:03");
        assertThat(result.getUplink().getMacAddress()).isEqualTo("AA:02");
        assertThat(result.getUplink().getUplink().getMacAddress()).isEqualTo("AA:01");
        assertThat(result.getUplink().getUplink().getUplink()).isNull();
    }

    @Test
    void retrieveOneAsTree_midDevice_buildsPartialUplinkChain() {
        when(deviceRepository.findById("AA:02")).thenReturn(Optional.of(switchDevice));

        DeviceTreeNode result = deviceService.retrieveOneAsTree("AA:02");

        // Switch → Gateway (AP should NOT appear)
        assertThat(result.getMacAddress()).isEqualTo("AA:02");
        assertThat(result.getUplink().getMacAddress()).isEqualTo("AA:01");
        assertThat(result.getUplink().getUplink()).isNull();
    }

    @Test
    void retrieveOneAsTree_rootDevice_hasNullUplink() {
        when(deviceRepository.findById("AA:01")).thenReturn(Optional.of(gatewayDevice));

        DeviceTreeNode result = deviceService.retrieveOneAsTree("AA:01");

        assertThat(result.getMacAddress()).isEqualTo("AA:01");
        assertThat(result.getUplink()).isNull();
    }

    @Test
    void retrieveOneAsTree_notFound_throwsException() {
        when(deviceRepository.findById("AA:99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.retrieveOneAsTree("AA:99"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AA:99");
    }

    @Test
    void retrieveOneAsTree_correctDeviceType_isPreservedInChain() {
        when(deviceRepository.findById("AA:03")).thenReturn(Optional.of(apDevice));

        DeviceTreeNode result = deviceService.retrieveOneAsTree("AA:03");

        assertThat(result.getDeviceType()).isEqualTo("ACCESS_POINT");
        assertThat(result.getUplink().getDeviceType()).isEqualTo("SWITCH");
        assertThat(result.getUplink().getUplink().getDeviceType()).isEqualTo("GATEWAY");
    }

    @Test
    void retrieveAllAsTree_looseDeviceAndChain_returnsTwoRoots() {
        Device looseDevice = new Device("BB:01", DeviceType.SWITCH, null);
        Device chainRoot   = new Device("CC:01", DeviceType.GATEWAY, null);
        Device chainLeaf   = new Device("CC:02", DeviceType.ACCESS_POINT, chainRoot);

        when(deviceRepository.findAll()).thenReturn(List.of(looseDevice, chainRoot, chainLeaf));

        Collection<DeviceTreeNode> tree = deviceService.retrieveAllAsTree();

        // BB:01 (loose) and CC:02 (leaf of chain) are both not referenced as anyone's uplink
        assertThat(tree).hasSize(2);

        DeviceTreeNode loose = tree.stream()
                .filter(n -> n.getMacAddress().equals("BB:01"))
                .findFirst()
                .orElseThrow();

        DeviceTreeNode leaf = tree.stream()
                .filter(n -> n.getMacAddress().equals("CC:02"))
                .findFirst()
                .orElseThrow();

        // loose device has no uplink
        assertThat(loose.getUplink()).isNull();

        // chain leaf points up to chain root
        assertThat(leaf.getUplink()).isNotNull();
        assertThat(leaf.getUplink().getMacAddress()).isEqualTo("CC:01");
        assertThat(leaf.getUplink().getUplink()).isNull();
    }
}