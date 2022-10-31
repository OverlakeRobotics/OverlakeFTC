package org.firstinspires.ftc.teamcode.opmodes.auto;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.components.PixyCam;
import org.firstinspires.ftc.teamcode.opmodes.base.BaseOpMode;

@Autonomous (name = "pixycam testing", group = "Autonomous")
public class PixyCamCenter extends BaseOpMode {

    protected PixyCam pixycam;
    private PixyCam.Block block;
    public static final int CONE_WIDTH = 150;
    private static final String TAG = "PixyCamCenter";

    /** Initialization */
    public void init(){
        super.init();
        pixycam = hardwareMap.get(PixyCam.class, "pixy");
    }


    public void loop(){
        block = pixycam.GetBiggestBlock(PixyCam.BLUE);
        String s = block.width + " " + block.height;
        String coords = block.x + ", " + block.y;
        int rotationOffset = pixycam.headingOffset(PixyCam.BLUE);
        int distanceOffset = pixycam.distanceOffset(100);
        telemetry.addData("rotationOffset", rotationOffset);
        telemetry.addData("distanceOffset", distanceOffset);
        telemetry.addData("block", s);
        telemetry.addData("coords", coords);
        if (align(PixyCam.BLUE,CONE_WIDTH)) {
            driveSystem.stopAndReset();
        }
        telemetry.update();

    }

}