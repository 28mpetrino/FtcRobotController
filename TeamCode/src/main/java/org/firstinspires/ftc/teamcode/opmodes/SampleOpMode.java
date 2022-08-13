package org.firstinspires.ftc.teamcode.opmodes;

import com.michaell.looping.ScriptParameters;
import org.firstinspires.ftc.teamcode.hardware.HardwareGetter;
import org.firstinspires.ftc.teamcode.hardware.physical.MotorOperation;
import org.firstinspires.ftc.teamcode.hardware.physical.StandardMotorParameters;
import org.firstinspires.ftc.teamcode.utils.opModeRegistration.OperationMode;
import org.firstinspires.ftc.teamcode.utils.opModeRegistration.TeleOperation;

public class SampleOpMode extends OperationMode implements TeleOperation {
    @Override
    public void construct() {
        HardwareGetter.makeMotorRequest("Motor1");
    }

    @Override
    public void run() {
        try {
            environment.issueRequest(new StandardMotorParameters(1.0, MotorOperation.POWER), environment.getRequest(
                "Motor1"));
        } catch (ScriptParameters.InvalidParametersException | ScriptParameters.RequestNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
