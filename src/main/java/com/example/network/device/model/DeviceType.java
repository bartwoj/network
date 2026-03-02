package com.example.network.device.model;

//TODO: refactor to @Entity to avoid recompilation after new type will be added
public enum DeviceType {
    GATEWAY(1),
    SWITCH(2),
    ACCESS_POINT(3);

    private final int sortingOrder;

    private DeviceType(int sortingOrder) {
        this.sortingOrder = sortingOrder;
    }

    public int getSortingOrder() {
        return sortingOrder;
    }
}
