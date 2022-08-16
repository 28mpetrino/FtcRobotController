package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.hardware.Devices;
import org.firstinspires.ftc.teamcode.hardware.HardwareGetter;
import org.firstinspires.ftc.teamcode.hardware.physical.GamepadRequestInput;
import org.firstinspires.ftc.teamcode.hardware.physical.MotorOperation;
import org.firstinspires.ftc.teamcode.hardware.physical.StandardMotorParameters;
import org.firstinspires.ftc.teamcode.utils.opModeRegistration.OperationMode;
import org.firstinspires.ftc.teamcode.utils.opModeRegistration.TeleOperation;

public class GamepadTestOpMode extends OperationMode implements TeleOperation {
    @Override
    public void construct() {
    }

    @Override
    public void run() {
        Devices.getMotor0().setPower(Devices.getGamepad1().getRightTrigger());
    }
}
