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
import org.firstinspires.ftc.teamcode.params.DriveParams;

import java.util.EnumMap;

/**
 * Basic OpMode template
 */
public abstract class BaseOpMode extends OpMode {

    protected DriveSystem driveSystem;
    protected PixyCam pixycam;
    protected ArmSystem armSystem;
    protected DigitalChannel poleBeam;
    protected DistanceSensor sonic;
    protected int step = 0;
    boolean b = false;
    int distanceOffset;


    @Override
    public void init(){
        setDriveSystem();
        poleBeam = hardwareMap.get(DigitalChannel.class, "pole beam");
        poleBeam.setMode(DigitalChannel.Mode.INPUT);
        sonic = hardwareMap.get(DistanceSensor.class, "dist sensor");
        pixycam = hardwareMap.get(PixyCam.class, "pixy");
        armSystem = new ArmSystem(
                hardwareMap.get(DcMotor.class, "arm_right"),
                hardwareMap.get(DcMotor.class, "arm_left"),
                hardwareMap.get(DcMotor.class, "intake"),
                hardwareMap.get(DigitalChannel.class, "beam")
        );
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

    protected boolean alignHeading(int signature) {
        int headingOffset = pixycam.headingOffset(signature);
        telemetry.addData("offset", headingOffset);
        Log.d("degrees", headingOffset + " ");
        if (headingOffset > 1) {
            driveSystem.drive(0.6f, 0, 0);
        } else if (headingOffset < -1) {
            driveSystem.drive(-0.6f, 0, 0);
        } else {
            driveSystem.setMotorPower(0);
            return true;
        }
        return false;
    }

    protected boolean alignDistance(int colorSignature, int desiredWidth){
        distanceOffset = pixycam.distanceOffset(colorSignature, desiredWidth);// find actual desired width
        telemetry.addData("offset", distanceOffset);
        Log.d("seeing", distanceOffset + " " + pixycam.GetBiggestBlock().width);
        if (distanceOffset > 5) {
            telemetry.addData("driving forward", 0);
            driveSystem.drive(0, 0, -0.3f);
        } else if (distanceOffset < -5) {
            telemetry.addData("driving backwards", 0.3f);
            driveSystem.drive(0, 0, 0.3f);
        } else {
            telemetry.addData("stopping", 0);
            driveSystem.setMotorPower(0);
            return true;
        }
        return false;
    }

    protected boolean alignConeDistance(int distance){

        return false;
    }

    protected boolean beamAlign(boolean cone){
        driveSystem.drive(0,0,-0.2f);
        if(!poleBeam.getState()){
            if(cone){
                if(driveSystem.driveToPosition(50, DriveSystem.Direction.FORWARD, 0.2)){
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
        if(step == 0){
            if(alignHeading(colorSignature)){
                step++;
            }
        }
        if(step == 1){
            if(alignDistance(colorSignature, desiredWidth)){
                step = 0;
                return true;
            }
        }
        return false;
    }

    public boolean scoreDaCone(int level, boolean park){
        if(step == 0){
            if(armSystem.driveToLevel(level-300, 0.7)){
                step +=2;
            }
        }

        if(step == 1){
            if(align(PixyCam.YELLOW, POLE_WIDTH)){
                step++;
            }
        }

        if(step == 2){
            if(armSystem.driveToLevel(level, 0.7)){
                step++;
            }
        }

        if(step == 3){
            if(beamAlign(park)){
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
        return false;
    }

    public boolean revertArm(double pow){
        if(armSystem.driveToLevel(ArmSystem.FLOOR,pow)){
            armSystem.armLeft.setPower(0);
            armSystem.armRight.setPower(0);
            return true;
        }
        return false;
    }
}
