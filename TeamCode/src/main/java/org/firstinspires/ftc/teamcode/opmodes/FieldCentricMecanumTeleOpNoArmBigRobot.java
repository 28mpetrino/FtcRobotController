package org.firstinspires.ftc.teamcode.opmodes;

import com.michaell.looping.ScriptRunner;
import org.firstinspires.ftc.teamcode.internals.features.Feature;
import org.firstinspires.ftc.teamcode.internals.features.MecanumDrivetrainFeature;
import org.firstinspires.ftc.teamcode.internals.hardware.Devices;
import org.firstinspires.ftc.teamcode.internals.misc.DrivetrainMapMode;
import org.firstinspires.ftc.teamcode.internals.registration.OperationMode;
import org.firstinspires.ftc.teamcode.internals.registration.TeleOperation;


public class FieldCentricMecanumTeleOpNoArmBigRobot extends OperationMode implements TeleOperation {

    @Override
    public void construct() {
        Devices.initializeExpansionHubMotors();
        Devices.initializeIntegratedIMU();
        MecanumDrivetrainFeature drivetrain = new MecanumDrivetrainFeature(DrivetrainMapMode.FR_BR_FL_BL, true, true, false);

        try {
            registerFeature(drivetrain);
        } catch (ScriptRunner.DuplicateScriptException e) {
            e.printStackTrace();
            stop();
        }
    }

    @Override
    public void run() {

    }
}