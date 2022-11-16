package org.firstinspires.ftc.teamcode.internals.features

import org.firstinspires.ftc.teamcode.internals.hardware.Devices.Companion.controller1
import org.firstinspires.ftc.teamcode.internals.telemetry.Logging.Companion.telemetry

/*
 * This allows for the driver station to remind the driver of the next best objective.
 * The data will be displayed via telemetry
 * And the data to display will be obtained from https://github.com/XaverianTeamRobotics/scoring-simulator (made by Michael L)
 * This tool is essentially our strategic AI, and it uses a Monte Carlo method to determine the best moves.
 * Sort of like a chess bot, but better.
 */
class TeleOpNextObjectiveReminder: Feature(){
    val objectives = arrayListOf("ground", "ground", "ground", "high", "high", "low", "low", "low", "low", "low", "low", "medium", "medium", "medium", "medium", "medium", "medium", "medium", "medium", "medium", "medium", "medium", "medium", "medium", "medium")

    // Create two hashmaps for the points each objective scores and the time each takes
    val objectivePoints = hashMapOf("terminal" to 1, "ground" to 2, "low" to 3, "medium" to 4, "high" to 5)
    val objectiveTimes = hashMapOf("terminal" to 3, "ground" to 3, "low" to 4, "medium" to 5, "high" to 8)

    // Create a empty hashmap to contain the count of each objective
    val objectiveCounts = hashMapOf("terminal" to 0, "ground" to 0, "low" to 0, "medium" to 0, "high" to 0)

    init {
        // Count each time an objective appears in the objectives array and add it to the objectiveCounts hashmap
        for (objective in objectives) {
            objectiveCounts[objective] = objectiveCounts[objective]!! + 1
        }
    }

    var isButtonHeld = false
    var currentObjective: String? = null

    override fun loop() {
        // Check to see if the driver has pressed the OPTIONS button on gamepad 2. We also need to check if its been held
        // so that the driver can see the next objective

        if (controller1.options && !isButtonHeld) {
            // Set the isButtonHeld variable to true so that the driver can see the next objective
            isButtonHeld = true

            // If the button is held, then we need to clear the currentObjective variable and deduct the count of the objective
            // from the objectiveCounts hashmap
            if (currentObjective != null) {
                objectiveCounts[currentObjective!!] = objectiveCounts[currentObjective!!]!! - 1
                currentObjective = null
            }
        } else if (!controller1.options && isButtonHeld) {
            // Set the isButtonHeld variable to false so that the driver can see the next objective
            isButtonHeld = false
        }

        if (currentObjective == null) {
            // If the currentObjective variable is null, then we need to find the next objective
            // We will do this by finding the objective with the highest count in the objectiveCounts hashmap
            var highestCount = 0
            var highestCountObjective: String? = null
            for (objective in objectiveCounts.keys) {
                if (objectiveCounts[objective]!! > highestCount) {
                    highestCount = objectiveCounts[objective]!!
                    highestCountObjective = objective
                }
            }

            // Set the currentObjective variable to the objective with the highest count
            currentObjective = highestCountObjective
        }
        // Display the current objective on the driver station. We should always do this for code cleanliness
        telemetry.addData("Next Objective", currentObjective)
        telemetry.update()
    }
}