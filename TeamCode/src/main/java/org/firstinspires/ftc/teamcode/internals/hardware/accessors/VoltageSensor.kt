package org.firstinspires.ftc.teamcode.utils.hardware.accessors

import com.michaell.looping.ScriptParameters
import com.qualcomm.robotcore.hardware.VoltageSensor
import org.firstinspires.ftc.teamcode.utils.hardware.HardwareGetter

/**
 * A voltage sensor can detect a voltage.
 */
class VoltageSensor(var name: String) {

    /**
     * The jlooping request managing the underlying hardware.
     */
    val request: ScriptParameters.Request = HardwareGetter.makeVoltageSensorRequest(name)

    /**
     * The underlying sensor being managed by the jlooping request.
     */
    val sensor: VoltageSensor
        get() {
            return HardwareGetter.getVoltageSensorFromRequest(name)
        }

    /**
     * The current voltage being sent through the sensor.
     */
    val voltage: Double
        get() {
            return HardwareGetter.getVoltageSensorData(name)
        }

}
