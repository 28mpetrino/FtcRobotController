package org.firstinspires.ftc.teamcode.main.utils.helpers.elevator;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.main.utils.gamepads.GamepadManager;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardDistanceSensor;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardMotor;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardServo;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardTouchSensor;
import org.firstinspires.ftc.teamcode.main.utils.io.InputSpace;
import org.firstinspires.ftc.teamcode.main.utils.io.OutputSpace;

public class ElevatorDriver {

    /*
    * INIT
    * */

    private final StandardMotor RIGHT_MOTOR, LEFT_MOTOR;
    private final StandardServo RIGHT_SERVO, LEFT_SERVO, HAND_SPINNER;
    private final StandardDistanceSensor DISTANCE;
    private final StandardTouchSensor LIMIT;

    private int handGrabbingPositionRight = 30;
    private int handGrabbingPositionLeft = 55;
    private int handReleasingPositionRight = 60;
    private int handReleasingPositionLeft = 30;
    private int distanceSensorDistance = 120;
    private int handTurningGrabbingPosition = 20;
    private int handTurningDefaultPosition = 23;
    private int handTurningBottomBallPosition = 36;
    private int handTurningMiddleBallPosition = 36;
    private int handTurningTopBallPosition = 36;
    private int handTurningBottomBlockPosition = 38;
    private int handTurningMediumBlockPosition = 38;
    private int handTurningTopBlockPosition = 38;
    private int handTurningSafePosition = 33;
    private int elevatorSafePosition = -500;
    private int elevatorLowerBallPosition = -20;
    private int elevatorMiddleBallPosition = -350;
    private int elevatorTopBallPosition = -700;
    private int elevatorLowerBlockPosition = -150;
    private int elevatorMiddleBlockPosition = -575;
    private int elevatorTopBlockPosition = -1000;

    private int step = 0;
    private final LinearOpMode OP_MODE;
    private double time = 0;
    private double rumbleTracker = 0;

    /**
     * Whether the robot is stable or not. This should only be true if the robot is not moving and in its default position, {@link #step} is 0, and/or when all the "isPos*" boolean values are false besides.
     */
    private boolean isStable = false;

    private boolean isPosIntake = false;
    private boolean isPosLowBall = false;
    private boolean isPosMedBall = false;
    private boolean isPosTopBall = false;
    private boolean isPosLowBlock = false;
    private boolean isPosMedBlock = false;
    private boolean isPosTopBlock = false;

    private GamepadManager optionalGamepadManager;

    /**
     * This creates an ElevatorDriver with two elevator motors, two hand grabber servos, a hand spinner servo, a limit switch, and a distance sensor to determine when the servos should grab the hand. It uses the default configuration for each motor, servo, and sensor, which at the time of writing is best for our 2021-2022 season robot.
     * @param input The InputSpace to get the motors and servos from
     * @param output The OutputSpace to get the distance sensor from
     * @param opMode The OpMode this driver is being used in
     */
    public ElevatorDriver(InputSpace input, OutputSpace output, LinearOpMode opMode) {
        RIGHT_MOTOR = ((StandardMotor) input.getElevatorRightLift().getInternalInteractionSurface());
        LEFT_MOTOR = ((StandardMotor) input.getElevatorLeftLift().getInternalInteractionSurface());
        RIGHT_SERVO = ((StandardServo) input.getRightHandGrabber().getInternalInteractionSurface());
        LEFT_SERVO = ((StandardServo) input.getLeftHandGrabber().getInternalInteractionSurface());
        DISTANCE = ((StandardDistanceSensor) output.getHandDistanceSensor().getInternalInteractionSurface());
        HAND_SPINNER = ((StandardServo) input.getHandSpinner().getInternalInteractionSurface());
        LIMIT = ((StandardTouchSensor) output.getElevatorBottomLimitSwitch().getInternalInteractionSurface());
        OP_MODE = opMode;
    }

    /**
     * Enables the elevator driver to give feedback to drivers during TeleOps via gamepad vibration. This is optional, although recommended in TeleOps.
     * @param gamepadManager The manager of the gamepads
     */
    public void enableFeedback(GamepadManager gamepadManager) {
        optionalGamepadManager = gamepadManager;
    }

    /**
     * Disables the elevator driver to give feedback to drivers during TeleOps via gamepad vibration.
     */
    public void disableFeedback() {
        optionalGamepadManager = null;
    }

    /*
    * DRIVER
    * */

    /**
     * Runs the elevator. This method will run whatever needs to be ran for the elevator to reach the position it needs to. It should be called in a loop until the elevator has completed its task.
     */
    public void run() {
        if(!isStable()) {
            rumble();
            if(isPosIntake) {
                doPosIntake();
            }else if(isPosLowBall) {
                doPosLowBall();
            }else if(isPosMedBall) {
                doPosMedBall();
            }else if(isPosTopBall) {
                doPosTopBall();
            }else if(isPosLowBlock) {
                doPosLowBlock();
            }else if(isPosMedBlock) {
                doPosMedBlock();
            }else if(isPosTopBlock) {
                doPosTopBlock();
            }
        }else{
            derumble();
        }
    }

    /*
    * CONTROLLERS
    * */

    /**
     * Tells the driver to attempt to drive to the intake position if possible.
     */
    public void setToIntakePosition() {
        if(isStable()) {
            unstabalize();
            isPosIntake = true;
        }
    }

    /**
     * Tells the driver to attempt to drive to the lower ball position if possible.
     */
    public void setToLowerBallPosition() {
        if(isStable()) {
            unstabalize();
            isPosLowBall = true;
        }
    }

    /**
     * Tells the driver to attempt to drive to the medium ball position if possible.
     */
    public void setToMediumBallPosition() {
        if(isStable()) {
            unstabalize();
            isPosMedBall = true;
        }
    }

    /**
     * Tells the driver to attempt to drive to the top ball position if possible.
     */
    public void setToTopBallPosition() {
        if(isStable()) {
            unstabalize();
            isPosTopBall = true;
        }
    }

    /**
     * Tells the driver to attempt to drive to the lower block position if possible.
     */
    public void setToLowerBlockPosition() {
        if(isStable()) {
            unstabalize();
            isPosLowBlock = true;
        }
    }

    /**
     * Tells the driver to attempt to drive to the medium block position if possible.
     */
    public void setToMediumBlockPosition() {
        if(isStable()) {
            unstabalize();
            isPosMedBlock = true;
        }
    }

    /**
     * Tells the driver to attempt to drive to the top block position if possible.
     */
    public void setToTopBlockPosition() {
        if(isStable()) {
            unstabalize();
            isPosTopBlock = true;
        }
    }

    /**
     * Tells the driver to attempt to reset after driving to the intake position if possible.
     */
    private void unsetFromIntakePosition() {
        stabalize();
    }

    /**
     * Tells the driver to attempt to reset after driving to the lower ball position if possible.
     */
    private void unsetFromLowerBallPosition() {
        stabalize();
    }

    /**
     * Tells the driver to attempt to reset after driving to the medium ball position if possible.
     */
    private void unsetFromMediumBallPosition() {
        stabalize();
    }

    /**
     * Tells the driver to attempt to reset after driving to the top ball position if possible.
     */
    private void unsetFromTopBallPosition() {
        stabalize();
    }

    /**
     * Tells the driver to attempt to reset after driving to the lower block position if possible.
     */
    private void unsetFromLowerBlockPosition() {
        stabalize();
    }

    /**
     * Tells the driver to attempt to reset after driving to the medium block position if possible.
     */
    private void unsetFromMediumBlockPosition() {
        stabalize();
    }

    /**
     * Tells the driver to attempt to reset after driving to the top block position if possible.
     */
    private void unsetFromTopBlockPosition() {
        stabalize();
    }

    private void unstabalize() {
        isStable = false;
    }

    private void stabalize() {
        isStable = true;
        step = 0;
        isPosIntake = false;
        isPosLowBall = false;
        isPosMedBall = false;
        isPosTopBall = false;
        isPosLowBlock = false;
        isPosMedBlock = false;
        isPosTopBlock = false;
    }

    private void updateTime() {
        time = OP_MODE.time;
    }

    private void rumble() {
        if(optionalGamepadManager != null && rumbleTracker + 1 <= getOpModeTime()) {
            optionalGamepadManager.functionOneGamepad().rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS);
            optionalGamepadManager.functionTwoGamepad().rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS);
            optionalGamepadManager.functionThreeGamepad().rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS);
            optionalGamepadManager.functionFourGamepad().rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS);
            optionalGamepadManager.functionFiveGamepad().rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS);
            optionalGamepadManager.functionSixGamepad().rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS);
            rumbleTracker = getOpModeTime();
        }
    }

    private void derumble() {
        if(optionalGamepadManager != null) {
            optionalGamepadManager.functionOneGamepad().stopRumble();
            optionalGamepadManager.functionTwoGamepad().stopRumble();
            optionalGamepadManager.functionThreeGamepad().stopRumble();
            optionalGamepadManager.functionFourGamepad().stopRumble();
            optionalGamepadManager.functionFiveGamepad().stopRumble();
            optionalGamepadManager.functionSixGamepad().stopRumble();
        }
    }

    /*
    * GETTERS
    * */

    /**
     * This method determines whether the elevator is ready to do another action because it is stable. When it is stable, it is at its default position and not moving in any form.
     * @return The robot's state; true if stable and false if unstable
     */
    public boolean isStable() {
        return isStable && step == 0 && !isPosIntake && !isPosLowBall && ! isPosMedBall && !isPosTopBall && !isPosLowBlock && !isPosMedBlock && !isPosTopBlock;
    }

    public StandardMotor getRightMotor() {
        return RIGHT_MOTOR;
    }

    public StandardMotor getLeftMotor() {
        return LEFT_MOTOR;
    }

    public StandardServo getRightServo() {
        return RIGHT_SERVO;
    }

    public StandardServo getLeftServo() {
        return LEFT_SERVO;
    }

    public StandardServo getHandSpinner() {
        return HAND_SPINNER;
    }

    public StandardDistanceSensor getDistance() {
        return DISTANCE;
    }

    public int getHandGrabbingPositionRight() {
        return handGrabbingPositionRight;
    }

    public int getHandGrabbingPositionLeft() {
        return handGrabbingPositionLeft;
    }

    public int getHandReleasingPositionRight() {
        return handReleasingPositionRight;
    }

    public int getHandReleasingPositionLeft() {
        return handReleasingPositionLeft;
    }

    public int getDistanceSensorDistance() {
        return distanceSensorDistance;
    }

    public int getHandTurningGrabbingPosition() {
        return handTurningGrabbingPosition;
    }

    public int getHandTurningDefaultPosition() {
        return handTurningDefaultPosition;
    }

    public int getHandTurningBottomBallPosition() {
        return handTurningBottomBallPosition;
    }

    public int getHandTurningMiddleBallPosition() {
        return handTurningMiddleBallPosition;
    }

    public int getHandTurningTopBallPosition() {
        return handTurningTopBallPosition;
    }

    public int getHandTurningBottomBlockPosition() {
        return handTurningBottomBlockPosition;
    }

    public int getHandTurningMediumBlockPosition() {
        return handTurningMediumBlockPosition;
    }

    public int getHandTurningTopBlockPosition() {
        return handTurningTopBlockPosition;
    }

    public int getHandTurningSafePosition() {
        return handTurningSafePosition;
    }

    public int getElevatorSafePosition() {
        return elevatorSafePosition;
    }

    public int getElevatorLowerBallPosition() {
        return elevatorLowerBallPosition;
    }

    public int getElevatorMiddleBallPosition() {
        return elevatorMiddleBallPosition;
    }

    public int getElevatorTopBallPosition() {
        return elevatorTopBallPosition;
    }

    public int getElevatorLowerBlockPosition() {
        return elevatorLowerBlockPosition;
    }

    public int getElevatorMiddleBlockPosition() {
        return elevatorMiddleBlockPosition;
    }

    public int getElevatorTopBlockPosition() {
        return elevatorTopBlockPosition;
    }

    public int getStep() {
        return step;
    }

    public boolean isPosIntake() {
        return isPosIntake;
    }

    public boolean isPosLowBall() {
        return isPosLowBall;
    }

    public boolean isPosMedBall() {
        return isPosMedBall;
    }

    public boolean isPosTopBall() {
        return isPosTopBall;
    }

    public boolean isPosLowBlock() {
        return isPosLowBlock;
    }

    public boolean isPosMedBlock() {
        return isPosMedBlock;
    }

    public boolean isPosTopBlock() {
        return isPosTopBlock;
    }

    public LinearOpMode getOpMode() {
        return OP_MODE;
    }

    public double getOpModeTime() {
        return OP_MODE.time;
    }

    public double getInternalTime() {
        return time;
    }

    public double getTimeOfLastRumble() {
        return rumbleTracker;
    }

    public GamepadManager getOptionalGamepadManager() {
        return optionalGamepadManager;
    }

    public boolean isFeedbackEnabled() {
        return optionalGamepadManager != null;
    }

    /*
     * LOGIC
     * */

    private void doPosIntake() {
        if(step == 0) {
            HAND_SPINNER.setPosition(23);
            updateTime();
            step++;
        }
        // after moving the hand, move the elevator to the base position
        if(step == 1) {
            if(time + 1.5 <= getOpModeTime()) {
                if(LIMIT.isPressed()) {
                    step++;
                }else{
                    LEFT_MOTOR.driveWithEncoder(40);
                    RIGHT_MOTOR.driveWithEncoder(40);
                    step++;
                }
            }
        }
        // once the elevator is at the bottom, reset it
        if(step == 2 && LIMIT.isPressed()) {
            LEFT_MOTOR.driveWithEncoder(0);
            RIGHT_MOTOR.driveWithEncoder(0);
            LEFT_MOTOR.reset();
            RIGHT_MOTOR.reset();
            step++;
        }
        // once at base, move the hand to the intake position
        if(step == 3) {
            HAND_SPINNER.setPosition(20);
            LEFT_SERVO.setPosition(30);
            RIGHT_SERVO.setPosition(60);
            step++;
        }
        if(step == 4 && DISTANCE.getDistance(DistanceUnit.MM) <= 120) {
            updateTime();
            step++;
        }
        if(step == 5 && time + 0.5 <= getOpModeTime()) {
            LEFT_SERVO.setPosition(55);
            RIGHT_SERVO.setPosition(30);
            HAND_SPINNER.setPosition(23);
            updateTime();
            step++;
        }
        if(step == 6) {
            unsetFromIntakePosition();
        }
    }

    private void doPosLowBall() {
        if(step == 0) {
            LEFT_MOTOR.driveToPosition(-500, 50);
            RIGHT_MOTOR.driveToPosition(-500, 50);
            updateTime();
            step++;
        }
        // turn hand to safest position once elevator reaches its position
        if(step == 1 && LEFT_MOTOR.getDcMotor().getCurrentPosition() <= -500) {
            HAND_SPINNER.setPosition(33);
            updateTime();
            step++;
        }
        // move elevator down to position
        if(step == 2 && time + 0.25 <= getOpModeTime()) {
            LEFT_SERVO.setPosition(30);
            RIGHT_SERVO.setPosition(30);
            LEFT_MOTOR.driveToPosition(0, 50);
            RIGHT_MOTOR.driveToPosition(0, 50);
            step++;
        }
        // turn hand to the position to dispense the ball
        if(step == 3 && LEFT_MOTOR.getDcMotor().getCurrentPosition() >= -20) {
            updateTime();
            HAND_SPINNER.setPosition(36);
            step++;
        }
        // turn hand back to a safe position and move elevator to turning point position
        if(step == 4 && time + 2 <= getOpModeTime()) {
            HAND_SPINNER.setPosition(31);
            LEFT_MOTOR.driveToPosition(-500, 50);
            RIGHT_MOTOR.driveToPosition(-500, 50);
            step++;
        }
        // tell hand/elevator to reset once in a safe position to do so
        if(step == 5 && LEFT_MOTOR.getDcMotor().getCurrentPosition() <= -500) {
            LEFT_SERVO.setPosition(55);
            RIGHT_SERVO.setPosition(30);
            HAND_SPINNER.setPosition(23);
            step++;
        }
        if(step == 6) {
            if(time + 1.5 <= getOpModeTime()) {
                if(!LIMIT.isPressed()) {
                    LEFT_MOTOR.driveWithEncoder(40);
                    RIGHT_MOTOR.driveWithEncoder(40);
                }
                step++;
            }
        }
        // once the elevator is at the bottom, reset it
        if(step == 7 && LIMIT.isPressed()) {
            LEFT_MOTOR.driveWithEncoder(0);
            RIGHT_MOTOR.driveWithEncoder(0);
            LEFT_MOTOR.reset();
            RIGHT_MOTOR.reset();
            unsetFromLowerBallPosition();
        }
    }

    private void doPosMedBall() {
        if(step == 0) {
            LEFT_MOTOR.driveToPosition(-500, 50);
            RIGHT_MOTOR.driveToPosition(-500, 50);
            updateTime();
            step++;
        }
        // once at that position, turn hand to safe position
        if(step == 1 && LEFT_MOTOR.getDcMotor().getCurrentPosition() <= -500) {
            HAND_SPINNER.setPosition(33);
            updateTime();
            step++;
        }
        // move hand down to dispensing position
        if(step == 2 && time <= getOpModeTime()) {
            LEFT_SERVO.setPosition(30);
            RIGHT_SERVO.setPosition(60);
            LEFT_MOTOR.driveToPosition(-350, 50);
            RIGHT_MOTOR.driveToPosition(-350, 50);
            step++;
        }
        // turn hand to dispensing position
        if(step == 3 && LEFT_MOTOR.getDcMotor().getCurrentPosition() >= -350) {
            updateTime();
            HAND_SPINNER.setPosition(36);
            step++;
        }
        // after ball rolls out, move to safe turning position
        if(step == 4 && time + 2 <= getOpModeTime()) {
            HAND_SPINNER.setPosition(31);
            LEFT_MOTOR.driveToPosition(-500, 50);
            RIGHT_MOTOR.driveToPosition(-500, 50);
            step++;
        }
        // reset once safe to do so
        if(step == 5 && LEFT_MOTOR.getDcMotor().getCurrentPosition() <= -500) {
            LEFT_SERVO.setPosition(55);
            RIGHT_SERVO.setPosition(30);
            HAND_SPINNER.setPosition(23);
            step++;
        }
        if(step == 6) {
            if(time + 1.5 <= getOpModeTime()) {
                if(!LIMIT.isPressed()) {
                    LEFT_MOTOR.driveWithEncoder(40);
                    RIGHT_MOTOR.driveWithEncoder(40);
                }
                step++;
            }
        }
        // once the elevator is at the bottom, reset it
        if(step == 7 && LIMIT.isPressed()) {
            LEFT_MOTOR.driveWithEncoder(0);
            RIGHT_MOTOR.driveWithEncoder(0);
            LEFT_MOTOR.reset();
            RIGHT_MOTOR.reset();
            unsetFromMediumBallPosition();
        }
    }

    private void doPosTopBall() {
        // move to dispensing position, doesnt need to worry about safe position because its higher up
        if(step == 0) {
            LEFT_MOTOR.driveToPosition(-700, 50);
            RIGHT_MOTOR.driveToPosition(-700, 50);
            updateTime();
            step++;
        }
        // turn to dispensing position once position reached
        if(step == 5 && LEFT_MOTOR.getDcMotor().getCurrentPosition() <= -700) {
            LEFT_SERVO.setPosition(55);
            RIGHT_SERVO.setPosition(30);
            HAND_SPINNER.setPosition(23);
            step++;
        }
        if(step == 6) {
            if(time + 1.5 <= getOpModeTime()) {
                if(!LIMIT.isPressed()) {
                    LEFT_MOTOR.driveWithEncoder(40);
                    RIGHT_MOTOR.driveWithEncoder(40);
                }
                step++;
            }
        }
        // once the elevator is at the bottom, reset it
        if(step == 7 && LIMIT.isPressed()) {
            LEFT_MOTOR.driveWithEncoder(0);
            RIGHT_MOTOR.driveWithEncoder(0);
            LEFT_MOTOR.reset();
            RIGHT_MOTOR.reset();
            unsetFromTopBallPosition();
        }
    }

    private void doPosLowBlock() {
        if(step == 0) {
            LEFT_MOTOR.driveToPosition(-500, 50);
            RIGHT_MOTOR.driveToPosition(-500, 50);
            updateTime();
            step++;
        }
        // turn hand to safest position once elevator reaches its position
        if(step == 1 && LEFT_MOTOR.getDcMotor().getCurrentPosition() <= -500) {
            HAND_SPINNER.setPosition(33);
            updateTime();
            step++;
        }
        // move elevator down to position
        if(step == 2 && time + 0.25 <= getOpModeTime()) {
            LEFT_SERVO.setPosition(30);
            RIGHT_SERVO.setPosition(60);
            LEFT_MOTOR.driveToPosition(-150, 50);
            RIGHT_MOTOR.driveToPosition(-150, 50);
            step++;
        }
        // turn hand to the position to dispense the ball
        if(step == 3 && LEFT_MOTOR.getDcMotor().getCurrentPosition() >= -150) {
            HAND_SPINNER.setPosition(38);
            updateTime();
            step++;
        }
        // turn hand back to a safe position and move elevator to turning point position
        if(step == 4 && time + 2 <= getOpMode().time) {
            HAND_SPINNER.setPosition(31);
            LEFT_MOTOR.driveToPosition(-500, 50);
            RIGHT_MOTOR.driveToPosition(-500, 50);
            step++;
        }
        // tell hand/elevator to reset once in a safe position to do so
        if(step == 5 && LEFT_MOTOR.getDcMotor().getCurrentPosition() <= -700) {
            LEFT_SERVO.setPosition(55);
            RIGHT_SERVO.setPosition(30);
            HAND_SPINNER.setPosition(23);
            step++;
        }
        if(step == 6) {
            if(time + 1.5 <= getOpModeTime()) {
                if(!LIMIT.isPressed()) {
                    LEFT_MOTOR.driveWithEncoder(40);
                    RIGHT_MOTOR.driveWithEncoder(40);
                }
                step++;
            }
        }
        // once the elevator is at the bottom, reset it
        if(step == 7 && LIMIT.isPressed()) {
            LEFT_MOTOR.driveWithEncoder(0);
            RIGHT_MOTOR.driveWithEncoder(0);
            LEFT_MOTOR.reset();
            RIGHT_MOTOR.reset();
            unsetFromLowerBlockPosition();
        }
    }

    private void doPosMedBlock() {
        if(step == 0) {
            LEFT_MOTOR.driveToPosition(-575, 50);
            RIGHT_MOTOR.driveToPosition(-575, 50);
            updateTime();
            step++;
        }
        // turn hand to down position once elevator reaches its position
        if(step == 1 && LEFT_MOTOR.getDcMotor().getCurrentPosition() <= -575) {
            LEFT_SERVO.setPosition(30);
            RIGHT_SERVO.setPosition(60);
            HAND_SPINNER.setPosition(40);
            updateTime();
            step++;
        }
        // tell hand/elevator to reset after block is dispensed
        if(step == 2 && time + 4 <= getOpModeTime()) {
            step = 0;
            LEFT_SERVO.setPosition(55);
            RIGHT_SERVO.setPosition(30);
            HAND_SPINNER.setPosition(23);
            step++;
        }
        if(step == 3) {
            if(time + 1.5 <= getOpModeTime()) {
                if(!LIMIT.isPressed()) {
                    LEFT_MOTOR.driveWithEncoder(40);
                    RIGHT_MOTOR.driveWithEncoder(40);
                }
                step++;
            }
        }
        // once the elevator is at the bottom, reset it
        if(step == 4 && LIMIT.isPressed()) {
            LEFT_MOTOR.driveWithEncoder(0);
            RIGHT_MOTOR.driveWithEncoder(0);
            LEFT_MOTOR.reset();
            RIGHT_MOTOR.reset();
            unsetFromMediumBlockPosition();
        }
    }

    private void doPosTopBlock() {
        // move the elevator to dropping position
        if(step == 0) {
            LEFT_MOTOR.driveToPosition(-1000, 50);
            RIGHT_MOTOR.driveToPosition(-1000, 50);
            step++;
        }
        // turn hand to down position once elevator reaches its position
        if(step == 1 && LEFT_MOTOR.getDcMotor().getCurrentPosition() <= -1000) {
            LEFT_SERVO.setPosition(30);
            RIGHT_SERVO.setPosition(60);
            HAND_SPINNER.setPosition(40);
            updateTime();
            step++;
        }
        // tell hand/elevator to reset after block is dispensed
        if(step == 2 && time + 4 <= getOpModeTime()) {
            step = 0;
            LEFT_SERVO.setPosition(55);
            RIGHT_SERVO.setPosition(30);
            HAND_SPINNER.setPosition(23);
            step++;
        }
        if(step == 3) {
            if(time + 1.5 <= getOpModeTime()) {
                if(!LIMIT.isPressed()) {
                    LEFT_MOTOR.driveWithEncoder(40);
                    RIGHT_MOTOR.driveWithEncoder(40);
                }
                step++;
            }
        }
        // once the elevator is at the bottom, reset it
        if(step == 4 && LIMIT.isPressed()) {
            LEFT_MOTOR.driveWithEncoder(0);
            RIGHT_MOTOR.driveWithEncoder(0);
            LEFT_MOTOR.reset();
            RIGHT_MOTOR.reset();
            unsetFromTopBlockPosition();
        }
    }

    // TODO: replace hardcoded values with fields

}
