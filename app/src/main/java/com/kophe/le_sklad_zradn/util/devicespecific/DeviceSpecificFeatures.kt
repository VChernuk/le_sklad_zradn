package com.kophe.le_sklad_zradn.util.devicespecific

import android.os.Build.MANUFACTURER

interface DeviceSpecificFeaturesUtil {
    val deviceIsXiaomi: Boolean
    val deviceIsHuawei: Boolean
    val deviceIsSamsung: Boolean
    val deviceIsMotorola: Boolean
}

class DefaultDeviceSpecificFeaturesUtil : DeviceSpecificFeaturesUtil {

    override val deviceIsSamsung = MANUFACTURER.contains("samsung", true)

    override val deviceIsMotorola = MANUFACTURER.contains("motorola", true)
    override val deviceIsHuawei = MANUFACTURER.contains("huawei", true)
    override val deviceIsXiaomi =
        MANUFACTURER.contains("xiaomi", true) || MANUFACTURER.contains("redmi", true)

}
