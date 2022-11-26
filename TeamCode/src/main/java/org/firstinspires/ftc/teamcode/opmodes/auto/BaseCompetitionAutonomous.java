package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.teamcode.components.DriveSystem;
import org.firstinspires.ftc.teamcode.components.PixyCam;
import org.firstinspires.ftc.teamcode.components.Vuforia;
import org.firstinspires.ftc.teamcode.opmodes.base.BaseOpMode;

@Autonomous(name = "Base Competition Autonomous", group = "Autonomous")
public class BaseCompetitionAutonomous extends BaseOpMode {

    // List of all states the robot could be in
    private enum State {
        IDENTIFY_TARGET,
        PARK,
        END_STATE
    }

    public enum Sleeve {
        DAVID,
        BRIAN,
        TEAM
    }

    protected Sleeve teamAsset;
    protected Vuforia vuforia;
    protected DistanceSensor lidar;
    protected int currentPos;

    protected final String TAG = getClass().getSimpleName();
    private State mCurrentState;                         // Current State Machine State.

    /**
     * Initializes State Machine
     */
    public void init() {
        super.init();
        vuforia = new Vuforia(hardwareMap, Vuforia.CameraChoice.WEBCAM1);
        mCurrentState = State.IDENTIFY_TARGET;
    }

    @Override
    public void init_loop() {
        if (vuforia == null) {
            return;
        }
        telemetry.addData("signal sleeve?: ", vuforia.identifyTeamAsset());
        telemetry.addData("heading", driveSystem.imuSystem.getHeading());
        telemetry.update();

        identifySleeve();

    }

    protected void identifySleeve() {
        int i = vuforia.identifyTeamAsset();
        if (i >= 0) {
            teamAsset = Sleeve.values()[i];
        }
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
                telemetry.addData("team asset ", teamAsset.toString());
                if (teamAsset == null) {
                    //drive forward slowly/10 inches and identify again
                    if (driveSystem.driveToPosition(100, DriveSystem.Direction.FORWARD, 0.4)) {
                        currentPos += 100;
                        identifySleeve();
                        teamAsset = Sleeve.BRIAN;
                    }
                    telemetry.addData("signal sleeve?: ", vuforia.identifyTeamAsset());

                } else {
                    mCurrentState = State.PARK;
                }
                break;
            case PARK:
                park();
                break;
            case END_STATE:


        }
        telemetry.update();
    }

    private void park() {
        if (step == 0) {
            if (driveSystem.driveToPosition(560, DriveSystem.Direction.FORWARD, 0.4)) {
                step++;
            }
        }
        if (step == 1) {
            if (teamAsset == Sleeve.BRIAN ||
                    (teamAsset == Sleeve.TEAM && driveSystem.driveToPosition(600, DriveSystem.Direction.RIGHT, 0.4)) ||
                    (teamAsset == Sleeve.DAVID && driveSystem.driveToPosition(600, DriveSystem.Direction.LEFT, 0.4))) {
                mCurrentState = State.END_STATE;
            }
        }
    }




}