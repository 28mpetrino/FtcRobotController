package org.firstinspires.ftc.teamcode.utils.hardware.emulated

import com.michaell.looping.ScriptParameters
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

class EmulatedLaserDistanceSensorRequest(name: String) : ScriptParameters.Request(name) {

    override fun issueRequest(p0: Any?): Any {
        return 0.0
    }

    override fun getOutputType(): Class<Double>? {
        return Double::class.javaPrimitiveType
    }

    override fun getInputType(): Class<*> {
        return DistanceUnit::class.java
    }

}