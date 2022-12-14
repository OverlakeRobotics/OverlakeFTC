package org.firstinspires.ftc.teamcode.tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.components.Pixy2;

@TeleOp(name="Read Pixy")
public class ReadPixy extends OpMode
{
    private static final String TAG = "ReadPixy";
    private Pixy2 pixy;

    @Override
    public void init() {
        pixy  = hardwareMap.get(Pixy2.class, "pixy");
        pixy.lampOn();
        pixy.setLED(255,255,0);
    }

    @Override
    public void loop() {
        Pixy2.Block block = pixy.getBlock(3);
        if (block != null) {
            telemetry.addData("x: ", block.x);
            telemetry.addData("y: ", block.y);
            telemetry.addData("w: ", block.width);
            telemetry.addData("h: ", block.height);
        }

        telemetry.update();
    }

    @Override
    public void stop() {
        super.stop();
        pixy.setLED(0,0,0);
        pixy.lampOff();
    }
}
