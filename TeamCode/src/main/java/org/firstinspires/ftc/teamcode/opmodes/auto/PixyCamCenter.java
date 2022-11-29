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
    private static final double HEADING_P = 0.005;
    private static final double HEADING_I = 0.000;
    private static final double HEADING_D = 0.000;

    private static final double DISTANCE_P = 0.008;
    private static final double DISTANCE_I = 0.00;
    private static final double DISTANCE_D = 0.0;

    protected PixyCam pixycam;
    private boolean pidReset = false;
    protected MiniPID headingPID;
    protected MiniPID distancePID;
    protected double distancePIDOutput = 0;
    protected double headingPIDOutput = 0;
    protected int cycleCount = 0;

    protected double debounce;
    protected Ramp ramp;
    protected double rampStart;

    private double hp;
    private double hi;
    private double hd;

    private double dp;
    private double di;
    private double dd;

    private ElapsedTime elapsedTime;

    private int color;

    /** Initialization */
    public void init(){
        color = PixyCam.BLUE;
        super.init();
        // Timeouts to determine if stuck in loop
        // Initialize motors
        pixycam = hardwareMap.get(PixyCam.class, "pixy");

        hp = HEADING_P;
        hi = HEADING_I;
        hd = HEADING_D;
        headingPID = new MiniPID(hp,hi,hd);
        headingPID.setSetpoint(0);
        headingPID.setOutputLimits(0.25,1.0);

        dp = DISTANCE_P;
        di = DISTANCE_I;
        dd = DISTANCE_D;
        distancePID = new MiniPID(dp, di, dd);
        distancePID.setSetpoint(0);
        distancePID.setOutputLimits(0.25,1.0);
        elapsedTime = new ElapsedTime();
        ramp = new ExponentialRamp(new Point(0,.1), new Point(500,1));

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
                distancePID = new MiniPID(dp,di,dd);
                headingPID = new MiniPID(hp,hi,hd);
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
        cycleCount++;
        if (cycleCount == PixyCam.SAMPLE_SIZE) {
            cycleCount = 0;
            alignHeading(colorSignature);
            //alignDistance(colorSignature, desiredWidth);
        }
      //  ecoMode();
        driveSystem.drive(rightX, 0, leftY);
        return Math.abs(rightX) < .1 && Math.abs(leftY) < .1;
    }



    private void telemetryData() {
        telemetry.addData("Color: ", color == PixyCam.BLUE ? "Blue" : "Yellow");

        if (color == PixyCam.BLUE) {
            Integer blueDistanceOffset = pixycam.distanceOffset(PixyCam.BLUE, CONE_WIDTH);
            Integer blueRotationOffset = pixycam.headingOffset(PixyCam.BLUE);
            telemetry.addData("Blue Distance Offset", blueDistanceOffset);
            telemetry.addData("Blue Rotation Offset", blueRotationOffset);
        } else {
            Integer yellowDistanceOffset = pixycam.distanceOffset(PixyCam.YELLOW, POLE_WIDTH);
            Integer yellowRotationOffset = pixycam.headingOffset(PixyCam.YELLOW);
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