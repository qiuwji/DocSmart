package com.qiu.backend.common.utils;

import jakarta.servlet.http.HttpServletRequest;

public class DeviceIdUtil {

    private static final String HEADER_DEVICE_ID = "X-Device-Id";

    /**
     * 获取设备ID的优先顺序：
     * 1. 从请求头 X-Device-Id
     * 2. 如果没有，则用请求IP地址降级
     */
    public static String getDeviceId(HttpServletRequest request) {
        String deviceId = request.getHeader(HEADER_DEVICE_ID);
        if (deviceId != null && !deviceId.isEmpty()) {
            return deviceId;
        }

        // 降级用IP
        deviceId = request.getRemoteAddr();
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "unknown-device";
        }
        return deviceId;
    }
}

