package org.firstinspires.ftc.teamcode.main.utils.autonomous.starting;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.main.utils.autonomous.EncoderTimeoutManager;
import org.firstinspires.ftc.teamcode.main.utils.autonomous.image.TFLITE_Wrapper;
import org.firstinspires.ftc.teamcode.main.utils.autonomous.location.pipeline.PositionSystem;
import org.firstinspires.ftc.teamcode.main.utils.geometry.Angle;
import org.firstinspires.ftc.teamcode.main.utils.interactions.groups.StandardTankVehicleDrivetrain;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardMotor;
import org.firstinspires.ftc.teamcode.main.utils.io.InputSpace;
import org.firstinspires.ftc.teamcode.main.utils.io.OutputSpace;
import org.firstinspires.ftc.teamcode.main.utils.locations.ElevatorBottomLimitSwitchLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.ElevatorLeftLiftMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.ElevatorRightLiftMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.HandSpinningServoLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.IntakeLiftingServoLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.IntakeSpinningMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.resources.Resources;

public class StartingPositionManager {
    PositionSystem positionSystem;
    EncoderTimeoutManager encoderTimeout;
    LinearOpMode opMode;
    StandardTankVehicleDrivetrain tank;
    InputSpace input;
    OutputSpace output;
    int ballDropHeight;
    double timeAsOfLastManualIntakeMovement = 0, timeAsOfLastFullLiftMovement = 0, timeAsOfLastManualHandMovement = 0;
    int step = 0, manualHandPos = 23;
    boolean intakeShouldBeDown = false, intakeButtonWasDown = false, manualMode = false;
    boolean isMovingToLBall = false, isMovingToMBall = false, isMovingToTBall = false, isMovingToLBlock = false, isMovingToMBlock = false, isMovingToTBlock = false, isMovingToBasePos = false, isMovingToIntakePos = false;
    TFLITE_Wrapper imgProc;

    boolean isBlueSide, isCloseToParking;

    public StartingPositionManager(LinearOpMode opMode, boolean isBlueSide, boolean isCloseToParking, int ballDropHeight) {
        this.opMode = opMode;
        this.isBlueSide = isBlueSide;
        this.isCloseToParking = isCloseToParking;
        this.ballDropHeight = ballDropHeight;

        positionSystem = Resources.Navigation.Sensors.getPositionSystem(opMode.hardwareMap);

        input = new InputSpace(opMode.hardwareMap);
        output = new OutputSpace(opMode.hardwareMap);
        tank = (StandardTankVehicleDrivetrain) input.getTank().getInternalInteractionSurface();
        positionSystem.setDrivetrain(tank);

        imgProc = new TFLITE_Wrapper(opMode.hardwareMap);

        opMode.waitForStart();

        calibrateElevator();
        calibrateIntake();

        if (isBlueSide && !isCloseToParking) {
            encoderTimeout = new EncoderTimeoutManager(0);

            // Drop the intake
            toggleIntakeLifter();
            opMode.sleep(1000);

            // Move Forward 1.5 tiles
            positionSystem.encoderDrive(24);
            drivetrainHold();

            opMode.sleep(1000);

            // Turn clockwise 90 degrees
            positionSystem.turn(new Angle(-85, Angle.AngleUnit.DEGREE));
            drivetrainHold();

            opMode.sleep(1000);

            // Move Forward 1 Tile
            positionSystem.encoderDrive(12);
            drivetrainHold();

            opMode.sleep(1000);

            // Turn counter-clockwise 90 degrees
            positionSystem.turn(new Angle(95, Angle.AngleUnit.DEGREE));
            drivetrainHold();

            // Do lift
            controlEntireLiftAutonomously(ballDropHeight);
            opMode.sleep(7000);

            // Turn clockwise 90 degrees
            positionSystem.turn(new Angle(-85, Angle.AngleUnit.DEGREE));
            drivetrainHold();

            opMode.sleep(1000);

            // Move Forward 1 Tile
            positionSystem.encoderDrive(12);
            drivetrainHold();

            opMode.sleep(1000);

            // Turn clockwise 90 degrees
            positionSystem.turn(new Angle(-50, Angle.AngleUnit.DEGREE));
            drivetrainHold();

            //TODO: Check if spot is busy

            positionSystem.turn(new Angle(-45, Angle.AngleUnit.DEGREE));
            drivetrainHold();

            opMode.sleep(1000);

            // Move Forward 1 Tile
            positionSystem.encoderDrive(13);
            drivetrainHold();

            opMode.sleep(1000);

            // Turn counter-clockwise 90 degrees
            positionSystem.turn(new Angle(85, Angle.AngleUnit.DEGREE));
            drivetrainHold();

            opMode.sleep(1000);

            // Move Forward 1 Tile
/*            positionSystem.encoderDrive(12);
            motorHold();*/
        }
    }

    private void drivetrainHold() {
        encoderTimeout.restart();
        encoderTimeout.durationMillis = 5000;

        while (positionSystem.areMotorsBusy() && !encoderTimeout.hasTimedOut() && opMode.opModeIsActive()) {
            opMode.telemetry.addData("Motors busy for", encoderTimeout.getOperationTime());
            opMode.telemetry.update();
        }

        positionSystem.getDrivetrain().brake();
        encoderTimeout.restart();
    }

    private void calibrateElevator() {
        // move elevator up for a second
        int timeAsOfLastElevatorCalibrationBegin = (int) opMode.time;
        while(output.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) == 0 && timeAsOfLastElevatorCalibrationBegin > (int) opMode.time - 1) {
            input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, -100);
            input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, -100);
        }
        // move elevator down until it reaches the bottom
        while(output.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) == 0) {
            input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 30);
            input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 30);
        }
        // reset the elevator and hand
        ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).reset();
        ((StandardMotor) input.getElevatorRightLift().getInternalInteractionSurface()).reset();
        input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 23);
    }

    private void calibrateIntake() {
        // move the intake to the *UPPER* position
        input.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 70);
    }

    private void toggleIntakeLifter() {
        // move the intake based on the left bumper's state
        intakeShouldBeDown = !intakeShouldBeDown;
        if(intakeShouldBeDown) {
            input.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 35);
        }else{
            input.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 70);
        }
    }

    private void setIntakeSpeed(int speed) {
        input.sendInputToIntakeSpinner(IntakeSpinningMotorLocation.Action.SET_SPEED, speed);
    }

    private void controlEntireLiftAutonomously(int h) {
        // enables intake pos routine if requested
        if(h == 0 && !isMovingToBasePos && !isMovingToLBall && !isMovingToMBall && !isMovingToTBall && !isMovingToLBlock && !isMovingToMBlock && !isMovingToTBlock && !isMovingToIntakePos) {
            isMovingToIntakePos = true;
            step = 0;
        }
        if(isMovingToIntakePos) {
            // sets the hand to base position
            if(step == 0) {
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 23);
                timeAsOfLastFullLiftMovement = opMode.time;
                step++;
            }
            // after moving the hand, move the elevator to the base position
            if(step == 1 && timeAsOfLastFullLiftMovement + 1.5 <= opMode.time) {
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 40);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 40);
                step++;
            }
            // once the elevator is at the bottom, reset it
            if(step == 2 && output.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) != 0) {
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 0);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 0);
                ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).reset();
                ((StandardMotor) input.getElevatorRightLift().getInternalInteractionSurface()).reset();
                step++;
            }
            // once at base, move the hand to the intake position, currently only does this for 10 seconds but will eventually do this until the ball is in place
            // TODO: distance sensor stuff
            if(step == 3) {
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 20);
                timeAsOfLastFullLiftMovement = opMode.time;
                step++;
            }
            // once ball is in place, move to base position
            if(step == 4 && timeAsOfLastFullLiftMovement + 10 <= opMode.time) {
                step = 0;
                isMovingToIntakePos = false;
                isMovingToBasePos = true;
            }
        }
        // moves to base pos - this is not a routine that can be enabled by user input, but rather enabled by other routines to reset them after use
        if(isMovingToBasePos) {
            // sets the hand to base position
            if(step == 0) {
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 23);
                timeAsOfLastFullLiftMovement = opMode.time;
                step++;
            }
            // after moving the hand, move the elevator to the base position
            if(step == 1 && timeAsOfLastFullLiftMovement + 1.5 <= opMode.time) {
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 40);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 40);
                step++;
            }
            // once the elevator is at the bottom, reset it
            if(step == 2 && output.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) != 0) {
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 0);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 0);
                ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).reset();
                ((StandardMotor) input.getElevatorRightLift().getInternalInteractionSurface()).reset();
                isMovingToBasePos = false;
                step = 0;
            }
        }
        // enables lower level ball routine if requested
        if(h == 1 && !isMovingToBasePos && !isMovingToLBall && !isMovingToMBall && !isMovingToTBall && !isMovingToLBlock && !isMovingToMBlock && !isMovingToTBlock  && !isMovingToIntakePos) {
            isMovingToLBall = true;
            step = 0;
        }
        // dispenses ball at lower level
        if(isMovingToLBall) {
            // move the elevator to allow hand room to turn
            if(step == 0) {
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -500);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -500);
                timeAsOfLastFullLiftMovement = opMode.time;
                step++;
            }
            // turn hand to safest position once elevator reaches its position
            if(step == 1 && ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -500) {
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 33);
                timeAsOfLastFullLiftMovement = opMode.time;
                step++;
            }
            // move elevator down to position
            if(step == 2 && timeAsOfLastFullLiftMovement + 0.25 <= opMode.time) {
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, 0);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, 0);
                step++;
            }
            // turn hand to the position to dispense the ball
            if(step == 3 && ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() >= -20) {
                timeAsOfLastFullLiftMovement = opMode.time;
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 36);
                step++;
            }
            // turn hand back to a safe position and move elevator to turning point position
            if(step == 4 && timeAsOfLastFullLiftMovement + 2 <= opMode.time) {
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 31);
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -500);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -500);
                step++;
            }
            // tell hand/elevator to reset once in a safe position to do so
            if(step == 5 && ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -500) {
                step = 0;
                isMovingToLBall = false;
                isMovingToBasePos = true;
            }
        }
        // enables middle level ball routine routine if requested
        if(h == 2 && !isMovingToBasePos && !isMovingToLBall && !isMovingToMBall && !isMovingToTBall && !isMovingToLBlock && !isMovingToMBlock && !isMovingToTBlock  && !isMovingToIntakePos) {
            isMovingToMBall = true;
            step = 0;
        }
        // dispenses ball at middle level
        if(isMovingToMBall) {
            // moves hand to safe turning position
            if(step == 0) {
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -500);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -500);
                timeAsOfLastFullLiftMovement = opMode.time;
                step++;
            }
            // once at that position, turn hand to safe position
            if(step == 1 && ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -500) {
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 33);
                timeAsOfLastFullLiftMovement = opMode.time;
                step++;
            }
            // move hand down to dispensing position
            if(step == 2 && timeAsOfLastFullLiftMovement + 2 <= opMode.time) {
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -350);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -350);
                step++;
            }
            // turn hand to dispensing position
            if(step == 3 && ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() >= -350) {
                timeAsOfLastFullLiftMovement = opMode.time;
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 36);
                step++;
            }
            // after ball rolls out, move to safe turning position
            if(step == 4 && timeAsOfLastFullLiftMovement + 2 <= opMode.time) {
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 31);
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -500);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -500);
                step++;
            }
            // reset once safe to do so
            if(step == 5 && ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -500) {
                step = 0;
                isMovingToMBall = false;
                isMovingToBasePos = true;
            }
        }
        // enables top level ball routine if requested
        if(h == 3 && !isMovingToBasePos && !isMovingToLBall && !isMovingToMBall && !isMovingToTBall && !isMovingToLBlock && !isMovingToMBlock && !isMovingToTBlock && !isMovingToIntakePos) {
            isMovingToTBall = true;
            step = 0;
        }
        // dispenses ball at top level
        if(isMovingToTBall) {
            // move to dispensing position, doesnt need to worry about safe position because its higher up
            if(step == 0) {
                input.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -700);
                input.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -700);
                timeAsOfLastFullLiftMovement = opMode.time;
                step++;
            }
            // turn to dispensing position once position reached
            if(step == 1 && ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -700) {
                input.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 36);
                timeAsOfLastFullLiftMovement = opMode.time;
                step++;
            }
            // after ball is dispensed, reset hand because its in a safe position
            if(step == 2 && timeAsOfLastFullLiftMovement + 3 <= opMode.time) {
                step = 0;
                isMovingToTBall = false;
                isMovingToBasePos = true;
            }
        }
        // TODO: block. block rot%: 38-40
    }
}
