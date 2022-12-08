package org.firstinspires.ftc.teamcode.tests;

import static org.firstinspires.ftc.teamcode.opmodes.auto.CompetitionAutonomous.CONE_WIDTH;
import static org.firstinspires.ftc.teamcode.opmodes.auto.CompetitionAutonomous.POLE_WIDTH;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.components.ArmSystem;
import org.firstinspires.ftc.teamcode.components.Light;
import org.firstinspires.ftc.teamcode.components.PixyCam;
import org.firstinspires.ftc.teamcode.opmodes.base.BaseOpMode;

@TeleOp(name = "pixy lights", group = "Tests")
public class PixyCamLightTest extends BaseOpMode {

    Light color;
    Light red;
    Light green;
    Light blue;
    int targetColor;

    /** Initialization */
    public void init(){
        super.init();
        red = new Light(hardwareMap.get(DigitalChannel.class,"red"));
        green = new Light(hardwareMap.get(DigitalChannel.class,"green"));
        blue = new Light(hardwareMap.get(DigitalChannel.class,"blue"));
    }

    @Override
    public void init_loop() {
        if (gamepad1.b) {
            if (gamepad1.left_bumper) {
                targetColor = PixyCam.RED;
            } else {
                color = red;
            }
        }

        if (gamepad1.a) {
            color = green;
        }

        if (gamepad1.x) {
            if (gamepad1.left_bumper) {
                targetColor = PixyCam.BLUE;
            } else {
                color = blue;
            }
        }

        if (gamepad1.y) {
            if (gamepad1.left_bumper) {
                targetColor = PixyCam.YELLOW;
            } else {
                color.off();
                color = null;
            }
        }

        if (color != null) {
            color.on();
        }
        telemetryData();
    }

    @Override
    public void loop() { }

    private void telemetryData() {
        String color = getColor();
        telemetry.addData("Target Color: ", color);
        int distanceOffset = pixycam.distanceOffset(targetColor, targetColor == PixyCam.YELLOW ? POLE_WIDTH : CONE_WIDTH);
        int headingOffset = pixycam.headingOffset(targetColor);
        telemetry.addData(color + " Distance Offset", distanceOffset);
        telemetry.addData(color + " Rotation Offset", headingOffset);
        telemetry.update();
    }

    private String getColor() {
        switch (targetColor) {
            case PixyCam.BLUE:
                return "Blue";
            case PixyCam.RED:
                return "Red";
            case PixyCam.YELLOW:
                return "Yellow";
        }
        return "";
    }

}