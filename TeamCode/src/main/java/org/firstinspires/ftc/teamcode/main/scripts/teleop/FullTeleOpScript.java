package org.firstinspires.ftc.teamcode.main.scripts.teleop;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.main.utils.gamepads.GamepadManager;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardMotor;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardServo;
import org.firstinspires.ftc.teamcode.main.utils.io.InputSpace;
import org.firstinspires.ftc.teamcode.main.utils.io.OutputSpace;
import org.firstinspires.ftc.teamcode.main.utils.locations.DuckMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.ElevatorBottomLimitSwitchLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.ElevatorLeftLiftMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.ElevatorRightLiftMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.HandSpinningServoLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.IntakeLiftingServoLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.IntakeSpinningMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.TankDrivetrainLocation;
import org.firstinspires.ftc.teamcode.main.utils.resources.Resources;
import org.firstinspires.ftc.teamcode.main.utils.scripting.TeleOpScript;

import java.util.HashMap;

public class FullTeleOpScript extends TeleOpScript {

    /* TODO: Full manual control.
     *  Toggling between manual and automatic control
     *  and the ability to escape automatic routines once manual mode is activated
     *  Elevator support, hand support, intake support
     *  For the elevator and hand, use the right stick
     */

    private GamepadManager gamepadManager;
    private InputSpace inputSpace;
    private OutputSpace outputSpace;
    private double timeAsOfLastManualIntakeMovement = 0, timeAsOfLastFullLiftMovement = 0, timeAsOfLastManualHandMovement = 0;
    private int step = 0, manualHandPos = 23;
    private boolean intakeShouldBeDown = false, intakeButtonWasDown = false, manualMode = false;
    private boolean isMovingToLBall = false, isMovingToMBall = false, isMovingToTBall = false, isMovingToLBlock = false, isMovingToMBlock = false, isMovingToTBlock = false, isMovingToBasePos = false, isMovingToIntakePos = false;

    public FullTeleOpScript(LinearOpMode opMode) {
        super(opMode);
        // set fields and calibrate robot
        gamepadManager = new GamepadManager(getOpMode().gamepad1, getOpMode().gamepad2, getOpMode().gamepad2, getOpMode().gamepad1, getOpMode().gamepad1, getOpMode().gamepad1);
        gamepadManager.functionOneGamepad().reset();
        inputSpace = new InputSpace(getOpMode().hardwareMap);
        outputSpace = new OutputSpace(getOpMode().hardwareMap);
        /*
        * GamepadManager Functions:
        *   F1: Driving
        *   F2: Intake Motor Control, Intake Lift Control, Intake Lift Manual Control
        *   F3: Lift Control, Elevator/Hand Manual Positioning
        *   F4: Duck Spinner Control
        *   F5: Unassigned
        *   F6: Unassigned
        * */
        inputSpace.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 30);
        calibrateElevator();
        inputSpace.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 70);
        gamepadManager.functionOneGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.Calibrated);
        gamepadManager.functionTwoGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.Calibrated);
        gamepadManager.functionThreeGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.Calibrated);
        gamepadManager.functionFourGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.Calibrated);
        gamepadManager.functionFiveGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.Calibrated);
        gamepadManager.functionSixGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.Calibrated);
    }

    @Override
    public void main() {
        controlDrivetrain();
        controlIntakeLifter();
        controlIntake();
        // these methods are for manual control of the lift. currently, they do not work with the controlEntireLiftAutonomously() method that well. they technically function but it's not idea. for now, comment that method out if you uncomment these two
//        controlElevator();
//        controlHand();
        controlEntireLiftAutonomously();
        controlDuck();
        // debug
        debug();
    }

    private void calibrateElevator() {
        // move elevator up for a second
        int timeAsOfLastElevatorCalibrationBegin = (int) getOpMode().time;
        while(outputSpace.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) == 0 && timeAsOfLastElevatorCalibrationBegin > (int) getOpMode().time - 1) {
            inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, -100);
            inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, -100);
        }
        // move elevator down until it reaches the bottom
        while(outputSpace.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) == 0) {
            inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 30);
            inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 30);
        }
        // reset the elevator and hand
        ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).reset();
        ((StandardMotor) inputSpace.getElevatorRightLift().getInternalInteractionSurface()).reset();
        inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 23);
    }

    private void controlDrivetrain() {
        // calculate the x and y speeds
        int left = (int) Range.clip((gamepadManager.functionOneGamepad().left_stick_y - gamepadManager.functionOneGamepad().left_stick_x) * 75, -75, 75);
        int right = (int) Range.clip((gamepadManager.functionOneGamepad().left_stick_y + gamepadManager.functionOneGamepad().left_stick_x) * 75, -75, 75);
        // set the defined speeds
        inputSpace.sendInputToTank(TankDrivetrainLocation.Action.SET_SPEED, -right, -left);
    }

    private void controlIntakeLifter() {
        // move the intake based on the left bumper's state
        if(gamepadManager.functionTwoGamepad().left_bumper) {
            if(!intakeButtonWasDown) {
                intakeShouldBeDown = !intakeShouldBeDown;
            }
            intakeButtonWasDown = true;
        }else{
            intakeButtonWasDown = false;
        }
        if(intakeShouldBeDown) {
            inputSpace.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 30);
        }else{
            inputSpace.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 70);
        }
    }

    private void controlIntake() {
        // control the intake motor based on the trigger inputs
        int intakeGas = (int) Range.clip(gamepadManager.functionTwoGamepad().left_trigger * 100, 0, 100);
        int intakeBrake = (int) Range.clip(gamepadManager.functionTwoGamepad().right_trigger * 100, 0, 100);
        int intakeSpeed = Range.clip(intakeGas - intakeBrake, -100, 100);
        inputSpace.sendInputToIntakeSpinner(IntakeSpinningMotorLocation.Action.SET_SPEED, intakeSpeed);
    }

    /**
     * This method controls all the autonomous stuff for the lift in TeleOps. Basically, it contains a bunch of routines. On every run, if no routine is running and a button is pressed to toggle a certain routine, the routine will fire. It will enable its routine, making all other routines impossible to run. It also has a step counter for routines with multiple steps. All of the steps are inside the statement checking if the routine is enabled.
     */
    private void controlEntireLiftAutonomously() {
        // enables intake pos routine if requested
        if(gamepadManager.functionThreeGamepad().a && !isMovingToBasePos && !isMovingToLBall && !isMovingToMBall && !isMovingToTBall && !isMovingToLBlock && !isMovingToMBlock && !isMovingToTBlock && !isMovingToIntakePos) {
            isMovingToIntakePos = true;
            step = 0;
        }
        if(isMovingToIntakePos) {
            // sets the hand to base position
            if(step == 0) {
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 23);
                timeAsOfLastFullLiftMovement = getOpMode().time;
                step++;
            }
            // after moving the hand, move the elevator to the base position
            if(step == 1 && timeAsOfLastFullLiftMovement + 1.5 <= getOpMode().time) {
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 40);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 40);
                step++;
            }
            // once the elevator is at the bottom, reset it
            if(step == 2 && outputSpace.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) != 0) {
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 0);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 0);
                ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).reset();
                ((StandardMotor) inputSpace.getElevatorRightLift().getInternalInteractionSurface()).reset();
                step++;
            }
            // once at base, move the hand to the intake position, currently only does this for 10 seconds but will eventually do this until the ball is in place
            // TODO: distance sensor stuff
            if(step == 3) {
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 20);
                timeAsOfLastFullLiftMovement = getOpMode().time;
                step++;
            }
            // once ball is in place, move to base position
            if(step == 4 && timeAsOfLastFullLiftMovement + 10 <= getOpMode().time) {
                step = 0;
                isMovingToIntakePos = false;
                isMovingToBasePos = true;
            }
        }
        // moves to base pos - this is not a routine that can be enabled by user input, but rather enabled by other routines to reset them after use
        if(isMovingToBasePos) {
            // sets the hand to base position
            if(step == 0) {
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 23);
                timeAsOfLastFullLiftMovement = getOpMode().time;
                step++;
            }
            // after moving the hand, move the elevator to the base position
            if(step == 1 && timeAsOfLastFullLiftMovement + 1.5 <= getOpMode().time) {
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 40);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 40);
                step++;
            }
            // once the elevator is at the bottom, reset it
            if(step == 2 && outputSpace.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) != 0) {
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 0);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 0);
                ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).reset();
                ((StandardMotor) inputSpace.getElevatorRightLift().getInternalInteractionSurface()).reset();
                isMovingToBasePos = false;
                step = 0;
            }
            gamepadManager.functionOneGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.RoutineCompleted);
            gamepadManager.functionTwoGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.RoutineCompleted);
            gamepadManager.functionThreeGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.RoutineCompleted);
            gamepadManager.functionFourGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.RoutineCompleted);
            gamepadManager.functionFiveGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.RoutineCompleted);
            gamepadManager.functionSixGamepad().runRumbleEffect(Resources.GamepadEffects.Vibrations.RoutineCompleted);
        }
        // enables lower level ball routine if requested
        if(gamepadManager.functionThreeGamepad().b && !gamepadManager.functionThreeGamepad().touchpad && !isMovingToBasePos && !isMovingToLBall && !isMovingToMBall && !isMovingToTBall && !isMovingToLBlock && !isMovingToMBlock && !isMovingToTBlock  && !isMovingToIntakePos) {
            isMovingToLBall = true;
            step = 0;
        }
        // dispenses ball at lower level
        if(isMovingToLBall) {
            // move the elevator to allow hand room to turn
            if(step == 0) {
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -500);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -500);
                timeAsOfLastFullLiftMovement = getOpMode().time;
                step++;
            }
            // turn hand to safest position once elevator reaches its position
            if(step == 1 && ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -500) {
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 33);
                timeAsOfLastFullLiftMovement = getOpMode().time;
                step++;
            }
            // move elevator down to position
            if(step == 2 && timeAsOfLastFullLiftMovement + 0.25 <= getOpMode().time) {
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, 0);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, 0);
                step++;
            }
            // turn hand to the position to dispense the ball
            if(step == 3 && ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() >= -20) {
                timeAsOfLastFullLiftMovement = getOpMode().time;
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 36);
                step++;
            }
            // turn hand back to a safe position and move elevator to turning point position
            if(step == 4 && timeAsOfLastFullLiftMovement + 2 <= getOpMode().time) {
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 31);
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -500);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -500);
                step++;
            }
            // tell hand/elevator to reset once in a safe position to do so
            if(step == 5 && ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -500) {
                step = 0;
                isMovingToLBall = false;
                isMovingToBasePos = true;
            }
        }
        // enables middle level ball routine routine if requested
        if(gamepadManager.functionThreeGamepad().y && !gamepadManager.functionThreeGamepad().touchpad && !isMovingToBasePos && !isMovingToLBall && !isMovingToMBall && !isMovingToTBall && !isMovingToLBlock && !isMovingToMBlock && !isMovingToTBlock  && !isMovingToIntakePos) {
            isMovingToMBall = true;
            step = 0;
        }
        // dispenses ball at middle level
        if(isMovingToMBall) {
            // moves hand to safe turning position
            if(step == 0) {
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -500);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -500);
                timeAsOfLastFullLiftMovement = getOpMode().time;
                step++;
            }
            // once at that position, turn hand to safe position
            if(step == 1 && ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -500) {
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 33);
                timeAsOfLastFullLiftMovement = getOpMode().time;
                step++;
            }
            // move hand down to dispensing position
            if(step == 2 && timeAsOfLastFullLiftMovement + 2 <= getOpMode().time) {
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -350);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -350);
                step++;
            }
            // turn hand to dispensing position
            if(step == 3 && ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() >= -350) {
                timeAsOfLastFullLiftMovement = getOpMode().time;
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 36);
                step++;
            }
            // after ball rolls out, move to safe turning position
            if(step == 4 && timeAsOfLastFullLiftMovement + 2 <= getOpMode().time) {
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 31);
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -500);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -500);
                step++;
            }
            // reset once safe to do so
            if(step == 5 && ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -500) {
                step = 0;
                isMovingToMBall = false;
                isMovingToBasePos = true;
            }
        }
        // enables top level ball routine if requested
        if(gamepadManager.functionThreeGamepad().x && !gamepadManager.functionThreeGamepad().touchpad && !isMovingToBasePos && !isMovingToLBall && !isMovingToMBall && !isMovingToTBall && !isMovingToLBlock && !isMovingToMBlock && !isMovingToTBlock && !isMovingToIntakePos) {
            isMovingToTBall = true;
            step = 0;
        }
        // dispenses ball at top level
        if(isMovingToTBall) {
            // move to dispensing position, doesnt need to worry about safe position because its higher up
            if(step == 0) {
                inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_POSITION, -700);
                inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_POSITION, -700);
                timeAsOfLastFullLiftMovement = getOpMode().time;
                step++;
            }
            // turn to dispensing position once position reached
            if(step == 1 && ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition() <= -700) {
                inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 36);
                timeAsOfLastFullLiftMovement = getOpMode().time;
                step++;
            }
            // after ball is dispensed, reset hand because its in a safe position
            if(step == 2 && timeAsOfLastFullLiftMovement + 3 <= getOpMode().time) {
                step = 0;
                isMovingToTBall = false;
                isMovingToBasePos = true;
            }
        }
        // TODO: block. block rot%: 38-40
    }

    /**
     * This controls the elevator manually. This should only be used when manual mode is enabled.
     */
    private void controlElevator() {
        // take input from user and map to elevator power
        double elevatorInput = gamepadManager.functionThreeGamepad().right_stick_y;
        int finalElevatorInput = elevatorInput > 0.5 ? 1 : (elevatorInput < -0.5 ? -1 : 0);
        int inputVal = Math.abs(((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition()) < 500 ? Range.clip(finalElevatorInput * 75, -75, 25) : Range.clip(finalElevatorInput * 75, -75, 75);
        // set elevator power, capping it when the elevator is at the bottom
        if(inputVal < 0 || outputSpace.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) == 0) {
            inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, inputVal);
            inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, inputVal);
        }else{
            inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 0);
            inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 0);
            // reset encoders every time the bottom is reached to minimize error
            ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).reset();
            ((StandardMotor) inputSpace.getElevatorRightLift().getInternalInteractionSurface()).reset();
        }
    }

    /**
     * This controls the hand manually. This should only be used when manual mode is enabled.
     */
    private void controlHand() {
        // set the hand to its allowed positions
        if(timeAsOfLastManualHandMovement + 0.25 <= getOpMode().time) {
            double handInput = gamepadManager.functionThreeGamepad().right_stick_x;
            if(handInput > 0.5) {
                manualHandPos += 1;
                manualHandPos = Range.clip(manualHandPos, 0, 100);
            }else if(handInput < -0.5) {
                manualHandPos -= 1;
                manualHandPos = Range.clip(manualHandPos, 0, 100);
            }
            inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, manualHandPos);
            timeAsOfLastManualHandMovement = getOpMode().time;
        }
    }

    private void controlDuck() {
        // turn duck motor slowly in the correct direction
        inputSpace.sendInputToDuckMotor(DuckMotorLocation.Action.SET_SPEED, gamepadManager.functionFourGamepad().right_bumper ? -50 : 0);
    }

    private void debug() {
        getOpMode().telemetry.addData("Elevator Encoder Position:", ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).getDcMotor().getCurrentPosition());
        getOpMode().telemetry.addData("Intake Lift %:", ((StandardServo) inputSpace.getIntakeLifter().getInternalInteractionSurface()).getPosition());
        getOpMode().telemetry.addData("Hand %:", ((StandardServo) inputSpace.getHandSpinner().getInternalInteractionSurface()).getPosition());
        getOpMode().telemetry.update();
    }

    @Override
    public void stop() {
        inputSpace.stop();
        outputSpace.stop();
    }

}
