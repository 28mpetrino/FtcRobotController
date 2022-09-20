package org.firstinspires.ftc.teamcode.utils.hardware.emulated

import com.michaell.looping.ScriptParameters
import org.firstinspires.ftc.teamcode.utils.hardware.physical.data.AnalogData

class EmulatedAnalogDeviceRequest(name: String?) : ScriptParameters.Request(name) {
    override fun issueRequest(p0: Any?): Any {
        return AnalogData(0.0, 0.0)
    }

    override fun getOutputType(): Class<*> {
        return AnalogData::class.java
    }

    override fun getInputType(): Class<*> {
        return Any::class.java
    }
}