package org.firstinspires.ftc.teamcode.other.opmodes.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name="TemplateOpMode", group="Iterative")
@Disabled
public class IterativeTemplate extends OpMode {

    private final ElapsedTime TIME = new ElapsedTime();

    /**
     * Code to run once when the OpMode is initialized.
     */
    @Override
    public void init() {
        telemetry.addData("Status", "Initialized");
    }

    /*
     * Code to loop between the end of init() and beginning of start().
     */
    @Override
    public void init_loop() {

    }

    /*
     * Code to run after hitting play.
     */
    @Override
    public void start() {

    }

    /*
     * Code to run after start() ends.
     */
    @Override
    public void loop() {

    }

    /*
     * Code to run once stop is pressed, or once the time runs out.
     */
    @Override
    public void stop() {
    }

}
