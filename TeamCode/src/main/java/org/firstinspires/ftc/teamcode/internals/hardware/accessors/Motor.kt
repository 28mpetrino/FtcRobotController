package org.firstinspires.ftc.teamcode.utils.hardware.accessors

import com.michaell.looping.ScriptParameters
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.teamcode.utils.hardware.HardwareGetter
import org.firstinspires.ftc.teamcode.utils.hardware.data.MotorOperation
import org.firstinspires.ftc.teamcode.utils.hardware.data.StandardMotorParameters

/**
 * A motor is a big thing that can rotate and drive things. Note that driving to a position is not supported by this API, and that's very much by design. The internal encoder and PID controller of motors both have a horribly low resolution, and thus should never be used when attempting to precisely drive to a position. Use an external encoder and write a custom PID loop for this functionality instead. If absolutely necessary, you can still use the motor's internal position driver via the internal motor accessor in this class.
 */
class Motor(var name: String) {

    /**
     * The jlooping request managing the underlying hardware.
     */
    val request: ScriptParameters.Request = HardwareGetter.makeMotorRequest(name)

    /**
     * The underlying DC motor being managed by the jlooping request.
     */
    val motor: DcMotor
        get() = HardwareGetter.getMotorFromRequest(name)

    /**
     * The power of the motor from -100 to 100. This will be converted to a voltage with a maximum voltage somewhere between 5 and 12 volts. The motor will simply be sent a voltage by this mode, and thus may run at inconsistent speeds depending on external forces applied to the motor.
     */
    var power: Double = 0.0
        get() {
            field = motor.power
            return field
        } set(value) {
            field = value
            HardwareGetter.setMotorValue(name, StandardMotorParameters(value, MotorOperation.POWER))
        }

    /**
     * The speed of the motor from -100 to 100. Unlike when setting the power of the motor, when setting the speed, the motor will use a PID loop to attempt to stay at a constant speed regardless of how much voltage must be sent as long as the voltage is within proper bounds. This mode is acceptable for use cases where the precision of speed isn't a big issue and can vary by a few units. In cases where extremely low tolerance is required, such as odometry, a custom speed PID loop should be written.
     */
    var speed: Double = 0.0
        get() {
            field = motor.power
            return field
        } set(value) {
            field = value
            HardwareGetter.setMotorValue(name, StandardMotorParameters(value, MotorOperation.ENCODER_POWER))
        }

    /**
     * Determines whether the robot is busy, that is, when a motor is acively driving to a position. If you're using this method, you should probably consider redesigning whatever you're using this method for.
     */
    fun isBusy(): Boolean {
        return HardwareGetter.isMotorBusy(name)
    }

}
