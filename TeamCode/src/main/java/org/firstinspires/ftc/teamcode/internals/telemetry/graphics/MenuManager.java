package org.firstinspires.ftc.teamcode.internals.telemetry.graphics;

import org.firstinspires.ftc.teamcode.internals.hardware.HardwareGetter;
import org.firstinspires.ftc.teamcode.internals.hardware.accessors.Gamepad;
import org.firstinspires.ftc.teamcode.internals.telemetry.Logging;

/**
 * A MenuManager is a little MVC framework which allows a driver to have simple interactions with the robot via a GUI.
 */
public class MenuManager {

    private final Menu MENU;
    private final Gamepad GAMEPAD;
    private long[] timestamps = new long[5];
    private boolean up = false;
    private boolean down = false;
    private boolean a = false;
    private Item result = null;

    public MenuManager(Menu menu, Gamepad gamepad) {
        MENU = menu;
        GAMEPAD = gamepad;
        long time = System.currentTimeMillis();
        timestamps[0] = time;
        timestamps[1] = time;
        timestamps[2] = time;
        timestamps[3] = time;
        timestamps[4] = time;
    }

    /**
     * Processes menu input.
     */
    private void input() {
        if(up && !GAMEPAD.getDpadUp()) {
            MENU.selectItem(true);
            up = false;
        }else if(GAMEPAD.getDpadUp()) {
            up = true;
        }
        if(down && !GAMEPAD.getDpadDown()) {
            MENU.selectItem(false);
            down = false;
        }else if(GAMEPAD.getDpadDown()) {
            down = true;
        }
        if(a && !GAMEPAD.getA()) {
            result = MENU.clickSelectedItem();
            a = false;
        }else if(GAMEPAD.getA()) {
            a = true;
        }
    }

    /**
     * Draws a new frame.
     */
    private void frame() {
        String frame = MENU.draw();
        Logging.clear();
        Logging.logText(frame);
        Logging.updateLog();
    }

    /**
     * Displays the menu until the OpMode has been stopped or a choice has been made.
     * @return The choice of the user.
     */
    public Item run() {
        while(!HardwareGetter.getOpMode().isStopRequested()) {
            frame();
            input();
            if(result != null) {
                Logging.clear();
                Logging.logText("");
                Logging.updateLog();
                return result;
            }
        }
        return null;
    }

}