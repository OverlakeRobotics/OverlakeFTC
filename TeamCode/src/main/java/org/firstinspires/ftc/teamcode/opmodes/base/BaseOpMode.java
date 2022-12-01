package org.firstinspires.ftc.teamcode.opmodes.base;

import static org.firstinspires.ftc.teamcode.opmodes.auto.CompetitionAutonomous.POLE_WIDTH;

import android.util.Log;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.I2cDevice;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.components.ArmSystem;
import org.firstinspires.ftc.teamcode.components.DriveSystem;
import org.firstinspires.ftc.teamcode.components.Lidar;
import org.firstinspires.ftc.teamcode.components.PixyCam;
import org.firstinspires.ftc.teamcode.opmodes.auto.CompetitionAutonomous;
import org.firstinspires.ftc.teamcode.params.DriveParams;
import org.firstinspires.ftc.teamcode.utilities.MiniPID;

import java.util.EnumMap;

/**
 * Basic OpMode template
 */
public abstract class BaseOpMode extends OpMode {

    protected DriveSystem driveSystem;
    protected PixyCam pixycam;
    protected ArmSystem armSystem;
    protected DigitalChannel poleBeam;

    private static final String TAG = "BaseOpMode";
    protected static final double HEADING_P = 0.009;
    protected static final double HEADING_I = 0.00;
    protected static final double HEADING_D = 0.000;

    protected static final double DISTANCE_P = 0.02;
    protected static final double DISTANCE_I = 0.00;
    protected static final double DISTANCE_D = 0.0;

    protected int step = 0;
    protected float leftY;
    protected float rightX;
    protected MiniPID headingPID;
    protected MiniPID distancePID;

    @Override
    public void init(){
        setDriveSystem();
        poleBeam = hardwareMap.get(DigitalChannel.class, "pole beam");
        poleBeam.setMode(DigitalChannel.Mode.INPUT);
        pixycam = hardwareMap.get(PixyCam.class, "pixy");
        armSystem = new ArmSystem(
                hardwareMap.get(DcMotor.class, "arm_right"),
                hardwareMap.get(DcMotor.class, "arm_left"),
                hardwareMap.get(DcMotor.class, "intake"),
                hardwareMap.get(DigitalChannel.class, "beam")
        );

        headingPID = new MiniPID(HEADING_P,HEADING_I,HEADING_D);
        headingPID.setSetpoint(0);
        headingPID.setOutputLimits(-.5,.5);
        distancePID = new MiniPID(DISTANCE_P, DISTANCE_I, DISTANCE_D);
        distancePID.setSetpoint(0);
        distancePID.setOutputLimits(-1,1);
    }

    private void setDriveSystem() {
        // Instantiate Drive System motor map
        EnumMap<DriveSystem.MotorNames, DcMotor> driveMap = new EnumMap<>(DriveSystem.MotorNames.class);
        for(DriveSystem.MotorNames name : DriveSystem.MotorNames.values()){
            driveMap.put(name,hardwareMap.get(DcMotor.class, name.toString()));
        }

        // Instantiate IMU
        BNO055IMU imu = hardwareMap.get(BNO055IMU.class, DriveParams.IMU);

        // Instantiate DriveSystem
        driveSystem = new DriveSystem(driveMap, imu);
    }

    protected boolean alignHeading(int colorSignature) {
        Integer headingOffset = pixycam.headingOffset(colorSignature); // find actual desired width
        Log.d(TAG, "heading offset: " + headingOffset);
        if (headingOffset == null) return false;
        double headingPIDOutput = headingPID.getOutput(headingOffset);
        Log.d(TAG, "heading PID output: " + headingPIDOutput);
        if (headingOffset > 3 || headingOffset < -3) {
            rightX = (float)-headingPIDOutput;
        } else {
            Log.d(TAG, "heading aligned - incoming power:  " + rightX);
            rightX = 0;
            headingPID.reset();
            return true;
        }
        Log.d(TAG, "adjusted heading Power: " + rightX);
        return false;
    }


    protected boolean alignDistance(int colorSignature, int desiredWidth) {
        Integer distanceOffset = pixycam.distanceOffset(colorSignature, desiredWidth);// find actual desired width
        Log.d(TAG, "distance offset: " + distanceOffset);
        if(distanceOffset == null) return false;
        double distancePIDOutput = distancePID.getOutput(distanceOffset);
        Log.d(TAG, "distance PID output: " + distancePIDOutput);
        if (distanceOffset > 3 || distanceOffset < -3) {
            leftY = (float)distancePIDOutput;
        } else {
            Log.d(TAG, "distance aligned - incoming power:  " + leftY);
            leftY = 0;
            distancePID.reset();
            return true;
        }
        Log.d(TAG, "adjusted leftYPower: " + leftY);
        return false;
    }

    protected boolean alignConeDistance(int distance){

        return false;
    }



    protected boolean beamAlign(boolean cone, int mm){
        driveSystem.drive(0,0,-0.2f);
        if(!poleBeam.getState()){
            if(cone){
                if(driveSystem.driveToPosition(mm, DriveSystem.Direction.FORWARD, 0.4)){
                    driveSystem.setMotorPower(0);
                    return true;
                }
            }
            else{
                driveSystem.setMotorPower(0);
                return true;
            }
        }
        return false;
    }



    protected boolean align(int colorSignature, int desiredWidth){

        if (step == 0) {
            if (alignHeading(colorSignature)) {
                step++;
            }
        }
        if (step == 1) {
            if (alignDistance(colorSignature, desiredWidth)) {
                step++;
            }
        }
        if(step == 2){
            headingPID.reset();
            if(alignHeading(colorSignature)){
                step = 0;
                return true;
            }
        }
        driveSystem.drive(rightX, 0, leftY);

        return false;
    }

    public boolean scoreDaCone(int level, boolean park){
        if(step == 0){
            if (armSystem.driveToLevel(level - 200, .7) && align(PixyCam.YELLOW,POLE_WIDTH)) {
                step += 2;
            }
         }

        if(step == 2){
            if(armSystem.driveToLevel(level, 0.7)){
                step++;
            }
        }

        if(step == 3){
            if(beamAlign(park, 45)){
                step++;
            }
            //drive forward
        }

        if(step == 4){
            if(armSystem.outtake()){
                step = 0;
                return true;
            }
        }

//        if(step == 6){
//            if(armSystem.driveToLevel(level, 0.3)){
//                step = 0;
//                return true;
//            }
//        }
        return false;
    }

    public boolean revertArm(double pow){
        if(armSystem.driveToLevel(ArmSystem.FLOOR, pow)){
            armSystem.armLeft.setPower(0);
            armSystem.armRight.setPower(0);
            return true;
        }
        return false;
    }
    public boolean intake_cone(ArmSystem.Cone cone) {
        //TODO make applicable for multiple cones and add ternary
        if (step == 0 && armSystem.driveToLevel(cone.approach(), 0.6)) {
            step++;
        }
        if (step == 1 &&
                beamAlign(true, 0)){
            step++;
        }
        if (step == 2 && (armSystem.intake() || armSystem.driveToLevel(cone.grab(), 0.6))) {
            step++;
        }
        if (step == 3) {
            if (armSystem.intake()) { // Complete the intake process -- i.e. stop
                step++;
            }
        }
        if (step == 4 && armSystem.driveToLevel(cone.clear(), 0.6)) {
            step = 0;
            return true;
        }
        return false;
    }
}
