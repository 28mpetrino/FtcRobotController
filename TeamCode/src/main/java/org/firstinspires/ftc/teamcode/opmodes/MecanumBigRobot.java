package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.features.MecanumDrivetrainFeature;
import org.firstinspires.ftc.teamcode.internals.hardware.Devices;
import org.firstinspires.ftc.teamcode.internals.misc.DrivetrainMapMode;
import org.firstinspires.ftc.teamcode.internals.registration.OperationMode;
import org.firstinspires.ftc.teamcode.internals.registration.TeleOperation;


public class MecanumBigRobot extends OperationMode implements TeleOperation {

    @Override
    public void construct() {
        Devices.initializeExpansionHubMotors();
        MecanumDrivetrainFeature drivetrain = new MecanumDrivetrainFeature(DrivetrainMapMode.FR_BR_FL_BL, true);
        registerFeature(drivetrain);
    }

    @Override
    public void run() {

    }
}