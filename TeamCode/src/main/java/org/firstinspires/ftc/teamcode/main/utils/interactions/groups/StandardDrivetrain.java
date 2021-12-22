package org.firstinspires.ftc.teamcode.main.utils.interactions.groups;

import org.firstinspires.ftc.teamcode.main.utils.interactions.InteractionSurface;

public abstract class StandardDrivetrain extends InteractionSurface {
    public abstract void driveDistance(int rightDistance, int leftDistance, int speed);
    public abstract void driveDistance(int distance, int speed);
    public abstract void driveWithEncoder(int rightSpeed, int leftSpeed);
    public abstract void driveWithEncoder(int speed);
    public abstract void driveWithoutEncoder(int rightPower, int leftPower);
    public abstract void driveWithoutEncoder(int power);
    public abstract void stop();
    public abstract void brake();
    public abstract void reset();
}