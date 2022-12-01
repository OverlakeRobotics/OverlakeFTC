package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.firstinspires.ftc.teamcode.opmodes.auto.CompetitionAutonomous.CONE_WIDTH;
import static org.firstinspires.ftc.teamcode.opmodes.auto.CompetitionAutonomous.POLE_WIDTH;

import android.annotation.SuppressLint;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.components.PixyCam;
import org.firstinspires.ftc.teamcode.opmodes.base.BaseOpMode;
import org.firstinspires.ftc.teamcode.utilities.MiniPID;
import org.firstinspires.ftc.teamcode.utilities.scale.ExponentialRamp;
import org.firstinspires.ftc.teamcode.utilities.scale.Point;
import org.firstinspires.ftc.teamcode.utilities.scale.Ramp;

@TeleOp(name = "pixy cam", group = "Tests")
public class PixyCamCenter extends BaseOpMode {

    private static final String TAG = "PixyCamCenter";

    private boolean pidReset = false;
    
    protected double debounce;

    private double hp = HEADING_P;
    private double hi = HEADING_I;
    private double hd = HEADING_D;

    private double dp = DISTANCE_P;
    private double di = DISTANCE_I;
    private double dd = DISTANCE_D;

    private ElapsedTime elapsedTime;

    private int color;

    /** Initialization */
    public void init(){
        super.init();
        color = PixyCam.BLUE;
        elapsedTime = new ElapsedTime();
    }

    @Override
    public void init_loop() {
        super.init_loop();
        telemetryData();
    }

    public void loop(){

        if (gamepad1.x) {
            color = PixyCam.BLUE;
        }

        if (gamepad1.y) {
            color = PixyCam.YELLOW;
        }


        if(gamepad1.a){
            if (!pidReset) {
                distancePID.setPID(dp, di, dd);
                headingPID.setPID(hp,hi,hd);
                distancePID.reset();
                headingPID.reset();
                pidReset = true;
            }
            int width = color == PixyCam.BLUE ? CONE_WIDTH : POLE_WIDTH;
            align(color, width);
        } else {
            pidReset = false;
            driveSystem.setMotorPower(0.0);
        }

        // Adjust PID for distance
        if (gamepad1.left_stick_y != 0) {
            if(elapsedTime.milliseconds() > 500 + debounce) {
                if (gamepad1.left_bumper) {
                    di -= gamepad1.left_stick_y/100;
                } else if (gamepad1.right_bumper) {
                    dd -= gamepad1.left_stick_y/100;
                } else {
                    dp -= gamepad1.left_stick_y/100;
                }
                debounce = elapsedTime.milliseconds();
            }
        }

        // Adjust PID for heading
        if (gamepad1.right_stick_y != 0) {
            if(elapsedTime.milliseconds() > 500 + debounce) {
                if (gamepad1.left_bumper) {
                    hi -= gamepad1.right_stick_y/100;
                } else if (gamepad1.right_bumper) {
                    hd -= gamepad1.right_stick_y/100;
                } else {
                    hp -= gamepad1.right_stick_y/100;
                }
                debounce = elapsedTime.milliseconds();
            }
        }

        telemetryData();

    }

    protected boolean align(int colorSignature, int desiredWidth){
        //  alignHeading(colorSignature);
        alignDistance(colorSignature, desiredWidth);
        driveSystem.drive(rightX, 0, leftY);
        return Math.abs(rightX) < .1 && Math.abs(leftY) < .1;
    }



    private void telemetryData() {
        telemetry.addData("Color: ", color == PixyCam.BLUE ? "Blue" : "Yellow");

        if (color == PixyCam.BLUE) {
            int blueDistanceOffset = pixycam.distanceOffset(PixyCam.BLUE, CONE_WIDTH);
            int blueRotationOffset = pixycam.headingOffset(PixyCam.BLUE);
            telemetry.addData("Blue Distance Offset", blueDistanceOffset);
            telemetry.addData("Blue Rotation Offset", blueRotationOffset);
        } else {
            int yellowDistanceOffset = pixycam.distanceOffset(PixyCam.YELLOW, POLE_WIDTH);
            int yellowRotationOffset = pixycam.headingOffset(PixyCam.YELLOW);
            telemetry.addData("Yellow Distance Offset", yellowDistanceOffset);
            telemetry.addData("Yellow Rotation Offset", yellowRotationOffset);
        }


        telemetry.addData("Distance P Value: ", String.format("%.3f",dp));
        telemetry.addData("Distance I Value: ", String.format("%.3f",di));
        telemetry.addData("Distance D Value: ", String.format("%.3f",dd));
        telemetry.addData("Heading P Value: ", String.format("%.3f",hp));
        telemetry.addData("Heading I Value: ", String.format("%.3f",hi));
        telemetry.addData("Heading D Value: ", String.format  ("%.3f",hd));
        telemetry.update();
    }


}