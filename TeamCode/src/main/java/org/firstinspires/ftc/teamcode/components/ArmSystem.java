package org.firstinspires.ftc.teamcode.components;

import android.util.Log;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.params.DriveParams;

public class ArmSystem {

    //fill in constants
    public static final int LOW = 500;
    public static final int MEDIUM = 800;
    public static final int HIGH = 1100;
    public static final int FLOOR = 10;
    public final DcMotor armLeft; //arm left is motor1
    public final DcMotor armRight;
    private Intake intake;


    public ArmSystem(DcMotor motor1, DcMotor motor2, DcMotor intakeMotor, DigitalChannel beam){
        armLeft = motor1;
        armRight = motor2;
        initMotors();
        intake = new Intake(intakeMotor, beam);
    }

    public Intake.State getState(){
        return intake.getState();
    }

    public boolean intake(){
        return intake.intake();
    }

    public boolean outtake(){
        return intake.outtake();
    }

    public void initMotors() {
        armLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armLeft.setPower(0);
        armLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armRight.setPower(0);
        armRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public boolean driveToLevel(int targetPosition, double power){
        armLeft.setTargetPosition(targetPosition);
        armLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armLeft.setPower(power);
        armRight.setTargetPosition(targetPosition);
        armRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armRight.setPower(power);
        //add code for second motor (armRight)
        int offsetLeft = Math.abs(armLeft.getCurrentPosition() - targetPosition);
        int offsetRight = Math.abs(armRight.getCurrentPosition() - targetPosition);
        Log.d("what is happening", offsetLeft + " " + offsetRight + " " + armLeft.getCurrentPosition() + " " + armRight.getCurrentPosition() + " power " + armLeft.getPower() + " " + armRight.getPower());
        if(targetPosition != 0 && offsetLeft < 20 && offsetRight < 20 ){
            Log.d("reached", armLeft.getCurrentPosition() + " " + armRight.getCurrentPosition() + " power " + armLeft.getPower() + " " + armRight.getPower() );
            return true;
        }
        else if (targetPosition > 0 && offsetLeft < 15 && offsetRight < 20){
            return true;
        }
        return false;
    }

    public void toDaGround(){

        armRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        armLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        DcMotorEx armLeftEx = (DcMotorEx) armLeft;
        DcMotorEx armRightEx = (DcMotorEx) armRight;


        //slow death
        if (driveToLevel(ArmSystem.FLOOR, 0.2)) {
            armLeftEx.setVelocity(50);
            armRightEx.setVelocity(50);
            if (driveToLevel(ArmSystem.LOW, 0.8)) {
                armRight.setPower(0);
                armLeft.setPower(0);
            }
        }
    }


    public static class Intake {
        private final DigitalChannel beamBreaker;
        private final DcMotorSimple coneTake;
        private final ElapsedTime elapsedTime;
        public enum State { IDLE, INTAKING, OUTTAKING }
        private State state;

        public Intake(DcMotorSimple intake, DigitalChannel beam){
            beamBreaker = beam;
            beamBreaker.setMode(DigitalChannel.Mode.INPUT);
            coneTake = intake;
            state = State.IDLE;
            elapsedTime = new ElapsedTime();
        }

        public boolean isBeamBroken(){
            return !beamBreaker.getState();
        }

        public boolean intake(){
            if (state != State.INTAKING) {
                state = State.INTAKING;
                coneTake.setDirection(DcMotor.Direction.REVERSE);
                coneTake.setPower(0.75);
            }

            if (isBeamBroken()) {
                coneTake.setPower(0.0);
                state = State.IDLE;
            }

            return state == State.IDLE;
        }

        public boolean outtake(){
            if (state != State.OUTTAKING) {
                state = State.OUTTAKING;
                coneTake.setDirection(DcMotorSimple.Direction.FORWARD);
                coneTake.setPower(0.75);
                elapsedTime.reset();
            }

            if (elapsedTime.milliseconds() > 100) {
                state = State.IDLE;
                coneTake.setPower(0);
            }

            return state == State.IDLE;
        }


        public State getState() {
            return state;
        }

    }
}