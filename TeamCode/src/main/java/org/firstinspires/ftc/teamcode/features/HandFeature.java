package org.firstinspires.ftc.teamcode.features;

import org.firstinspires.ftc.teamcode.internals.features.Buildable;
import org.firstinspires.ftc.teamcode.internals.features.Feature;
import org.firstinspires.ftc.teamcode.internals.hardware.Devices;

public class HandFeature extends Feature implements Buildable {

    @Override
    public void build() {
        Devices.initializeHandMotors();
    }

    @Override
    public void loop() {
        if(Devices.controller1.getA()) {
            Devices.expansion_motor2.setPower(-100);
        }else{
//            Devices.expansion_motor2.setPower(25);
        }
    }
}
