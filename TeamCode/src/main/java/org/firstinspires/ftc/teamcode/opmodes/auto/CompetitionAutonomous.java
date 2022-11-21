package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.components.DriveSystem;
import org.firstinspires.ftc.teamcode.components.PixyCam;
import org.firstinspires.ftc.teamcode.opmodes.base.BaseOpMode;

@Autonomous(name = "Competition Autonomous", group = "Autonomous")
public class CompetitionAutonomous extends BaseCompetitionAutonomous {

    public static final int POLE_WIDTH = 45;
    public static final int CONE_WIDTH = 150;
    private boolean park = false;

    // List of all states the robot could be in
    private enum State {
        IDENTIFY_TARGET,
        DRIVE_TO_JUNCTION,
        ALIGN_WITH_POLE,
        PLACE_CONE,
        DRIVE_TO_CONE,
        ALIGN_WITH_CONE,
        INTAKE_CONE,
        DRIVE_BACK_TO_POLE,
        PARK,
        REVERSE_JUNCTION,
        END_STATE
    }

    public enum From {
        START, CONE_STACK
    }

    private From startPosition;
    private State mCurrentState;                         // Current State Machine State.


    /**
     * Initializes State Machine
     */
    public void init() {
        super.init();
        step = 0;
        startPosition = From.START;
        newState(State.ALIGN_WITH_POLE);
    }

    /**
     * State machine loop
     */
    @Override
    public void loop() {
        // Update telemetry each time through loop
        telemetry.addData("State", mCurrentState);

        // Execute state machine
        switch (mCurrentState) {
            case IDENTIFY_TARGET:
                if (teamAsset == null) {
                    //drive forward slowly/10 cms and identify again
                    //backwards is forwards
                    if (driveSystem.driveToPosition(100, DriveSystem.Direction.BACKWARD, 0.2)) {
                        currentPos += 100;
                        teamAsset = Sleeve.BRIAN;
                    }
                    identifySleeve();
                    telemetry.addData("signal sleeve?: ", vuforia.identifyTeamAsset());
                } else {
                    // drive to tallest pole
                    newState(State.DRIVE_TO_JUNCTION);
                }
                break;
            case DRIVE_TO_JUNCTION:
                // after finding pole, align with pole
                if (drive_to_junction()) {
                    newState(State.ALIGN_WITH_POLE);
                }
                break;
            case ALIGN_WITH_POLE:
                // after aligning with pole, place cone
                if (align(PixyCam.YELLOW, POLE_WIDTH)) {
                    newState(State.PLACE_CONE);
                }
                break;
            case PLACE_CONE:
                // after placing cone, either get another or park
                if (!park) { // TODO: either get another or park
                    newState(State.DRIVE_TO_CONE);
                } else {
                    newState(State.REVERSE_JUNCTION);
                }
                break;
            case DRIVE_TO_CONE:
                // after driving to cone, align with cone
                if (drive_to_cone()) { //dependent on team color
                    newState(State.ALIGN_WITH_CONE);
                }
                break;
            case ALIGN_WITH_CONE:
                // after aligning with cone, intake cone
                if (align(PixyCam.BLUE, CONE_WIDTH)) {
                    newState(State.END_STATE); //TODO: should be intake cone instead of end state
                }
                break;
            case INTAKE_CONE:
                // after intaking cone, drive back to pole
                if (intake_cone()) {
                    newState(State.DRIVE_BACK_TO_POLE);
                }
                break;
            case DRIVE_BACK_TO_POLE:
                // after reaching pole, align with pole
                if(drive_back_to_pole()){
                    newState(State.ALIGN_WITH_POLE);
                }
                break;
            case REVERSE_JUNCTION:
                // go backwards and prepare to park wooo
                if(reverseJunction())
                    newState(State.PARK);
                break;
            case PARK:
                // park and end state
                if (park()) {
                    newState(State.END_STATE);
                }
                break;
            case END_STATE:
                //Log.d("parked", teamAsset.toString());
                //"david" left two squares, "brain" center two, "7330" right two squares

        }
        telemetry.update();
    }

    private boolean park() {
        if (step == 0) {
            if (teamAsset == Sleeve.BRIAN ||
                    (teamAsset == Sleeve.TEAM && driveSystem.driveToPosition(500, DriveSystem.Direction.LEFT, 0.3)) ||
                    (teamAsset == Sleeve.DAVID && driveSystem.driveToPosition(500, DriveSystem.Direction.RIGHT, 0.3))) {
                return true;
            }
        }
        return false;
    }

    private boolean drive_to_junction() {
        switch (startPosition) {
            case START:
                if (step == 0) {
                    if (driveSystem.driveToPosition(950 - currentPos, DriveSystem.Direction.BACKWARD, 0.4)) {
                        step++;
                    }
                }
                if (step == 1) {
                    if (driveSystem.turn(-45, 0.2)) {
                        return true;
                    }
                }
                break;
            case CONE_STACK:

        }
        return false;

    }

    private boolean reverseJunction() {
        if(step == 0){
            if (driveSystem.driveToPosition(40, DriveSystem.Direction.FORWARD, 0.4)){
                step++;
            }
        }
        if (step == 1) {
            if (driveSystem.turn(45, 0.2)) {
                step = 0;
                return true;
            }
        }
        return false;
    }



    private boolean drive_to_cone(){

        // Back up
        if(step == 0){
            if(driveSystem.driveToPosition(60, DriveSystem.Direction.FORWARD, 0.3)){
                step++;
            }
        }

        // Rotate
        if(step == 1){
            if(driveSystem.turnAbsolute(90, 0.3)){
                step++;
            }

        }

        // Drive to cone
        if(step == 2) {
            if (driveSystem.driveToPosition(360, DriveSystem.Direction.BACKWARD, 0.5)) {
                return true;
            }
        }
        return false;
    }

    private boolean intake_cone() {
        return false;
    }

    private boolean drive_back_to_pole(){
        if(step == 0){
            if(driveSystem.driveToPosition(350, DriveSystem.Direction.FORWARD, 0.3)){
                step++;
            }
        }

        if(step == 1){
            if(driveSystem.turn(-135, 0.3)){
                step++;
            }
        }
        if(step == 2){
            if(driveSystem.driveToPosition(30, DriveSystem.Direction.BACKWARD, 0.3)){
                step = 0;
                park = true;
                return true;
            }
        }
        return false;
    }
    /**
     * Changes state to given state
     *
     * @param newState state to change to
     */
    protected void newState(State newState) {
        mCurrentState = newState;
        step = 0;
    }
}