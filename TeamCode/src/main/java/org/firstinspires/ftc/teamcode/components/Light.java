package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.DigitalChannel;

public class Light {

    private DigitalChannel redLed;
    private DigitalChannel greenLed;
    private DigitalChannel blueLed;


    public Light(DigitalChannel led) {
        this.redLed = led;
        led.setMode(DigitalChannel.Mode.OUTPUT);
    }

    public void on() {
        redLed.setState(true);
    }

    public void off() {
        redLed.setState(false);
    }

    public void allOff(){
        redLed.setState(false);
        greenLed.setState(false);
        blueLed.setState(false);
    }

    public void yellowOn(){
        redLed.setState(true);
        greenLed.setState(true);
    }
}