package org.firstinspires.ftc.teamcode.tests;

import static org.firstinspires.ftc.teamcode.opmodes.auto.CompetitionAutonomous.CONE_WIDTH;
import static org.firstinspires.ftc.teamcode.opmodes.auto.CompetitionAutonomous.POLE_WIDTH;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.components.ArmSystem;
import org.firstinspires.ftc.teamcode.components.Light;
import org.firstinspires.ftc.teamcode.components.Pixy2;
import org.firstinspires.ftc.teamcode.opmodes.base.BaseOpMode;

@TeleOp(name = "pixy lights", group = "Tests")
public class PixyCamLightTest extends OpMode {

    int targetColor;
    Pixy2 pixycam;
    Integer distance;

    /** Initialization */
    public void init(){
        pixycam = hardwareMap.get(Pixy2.class, "pixy");

    }

    @Override
    public void start() {
        super.start();
        pixycam.lampOn();
    }

    @Override
    public void loop() {

        if (gamepad1.b) {
            setColor(Pixy2.RED);
        }

        if (gamepad1.x) {
            setColor(Pixy2.BLUE);
        }

        if (gamepad1.y) {
            setColor(Pixy2.YELLOW);
        }

        if(targetColor != 0){
            telemetryData();
        }

    }

    private void setColor(int color) {
        if (targetColor != color) {
            targetColor = color;
            setLED();
        }
    }

    private void setLED() {
        int red = 0;
        int green =  0;
        int blue = 0;
        switch (targetColor) {
            case Pixy2.BLUE:
                blue = 1;
                break;
            case Pixy2.RED:
                red = 1;
                break;
            case Pixy2.YELLOW:
               red = 1;
               green = 1;
               break;
        }
        pixycam.setLED(red, green, blue);
    }

    private void telemetryData() {
        String color = getColor();
        telemetry.addData("Target Color: ", getColor());
        Integer distanceOffset = pixycam.distanceOffset(targetColor, targetColor == Pixy2.YELLOW ? POLE_WIDTH : CONE_WIDTH);
        if(distanceOffset != null){
            distance = distanceOffset;
        }
        //Integer headingOffset = pixycam.headingOffset(targetColor);
        telemetry.addData(color + " Distance Offset", distance);
        //telemetry.addData(color + " Rotation Offset", headingOffset);
        telemetry.update();
    }

    private String getColor() {
        switch (targetColor) {
            case Pixy2.BLUE:
                return "Blue";
            case Pixy2.RED:
                return "Red";
            case Pixy2.YELLOW:
                return "Yellow";
        }
        return "";
    }

    @Override
    public void stop() {
        super.stop();
        pixycam.lampOff();
    }
}