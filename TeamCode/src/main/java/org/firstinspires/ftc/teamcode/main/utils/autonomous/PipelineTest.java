package org.firstinspires.ftc.teamcode.main.utils.autonomous;

import android.graphics.Path;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.main.utils.autonomous.location.pipeline.PositionSystem;
import org.firstinspires.ftc.teamcode.main.utils.interactions.groups.StandardTankVehicleDrivetrain;
import org.firstinspires.ftc.teamcode.main.utils.io.InputSpace;
import org.firstinspires.ftc.teamcode.main.utils.queuing.ActionQueuing;
import org.firstinspires.ftc.teamcode.main.utils.resources.Resources;

@TeleOp(name = "Pipeline Test")
public class PipelineTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        PositionSystem positionSystem = Resources.Navigation.Sensors.getPositionSystem(hardwareMap);

        InputSpace input = new InputSpace(hardwareMap);
        StandardTankVehicleDrivetrain tank = (StandardTankVehicleDrivetrain) input.getTank().getInternalInteractionSurface();
        positionSystem.setDrivetrain(tank);

        waitForStart();

        while (opModeIsActive()) {
            positionSystem.getAndEvalReadings();
            positionSystem.updateAngle();
            telemetry.addData("Angle (Degrees)", positionSystem.coordinateSystem.angle.toString());
            telemetry.addData("Position", positionSystem.coordinateSystem.current.toString());
            telemetry.addLine();
            telemetry.addLine("Readings: ");
            telemetry.addData("    North", positionSystem.rawNorthReading);
            telemetry.addData("    East", positionSystem.rawEastReading);
            telemetry.addData("    West", positionSystem.rawWestReading);
            telemetry.update();

            if (gamepad1.dpad_right) { positionSystem.turnDegree((int) (positionSystem.imuData.getHeading() + 90), Path.Direction.CW); }
            if (gamepad1.dpad_left) { positionSystem.turnDegree((int) (positionSystem.imuData.getHeading() - 90), Path.Direction.CCW); }
            if (gamepad1.dpad_up) { positionSystem.encoderDrive(24); }
            if (gamepad1.dpad_down) { positionSystem.encoderDrive(-24); }
            if (gamepad1.left_bumper) { positionSystem.imuOffset = 90; }
            if (gamepad1.right_bumper) { positionSystem.imuOffset = 270; }

            EncoderTimeoutManager encoderTimeout = new EncoderTimeoutManager(5000);

            while (positionSystem.areMotorsBusy() && !encoderTimeout.hasTimedOut()) {
                telemetry.addData("Motors busy for", encoderTimeout.getOperationTime());
                telemetry.update();
            }
        }
    }
}
