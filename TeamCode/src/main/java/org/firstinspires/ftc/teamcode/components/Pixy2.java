package org.firstinspires.ftc.teamcode.components;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@I2cDeviceType
@DeviceProperties(name = "Pixycam2 ", description = "Pixycam 2 color sensor", xmlTag = "pixycam2")
public class Pixy2 extends I2cDeviceSynchDevice<I2cDeviceSynch> {
    public static final int YELLOW = 3;
    public static final int BLUE = 1;
    public static final int RED = 2;
    private Integer headingOffset;
    private Integer distanceOffset;

    private static final String TAG = "Pixy2";
    public static final short REQUEST = (short) 0xc1ae;
    protected ElapsedTime elapsedTime;
    protected double lastPoll;

    public enum Type {

        GET_BLOCKS(REQUEST,32, 2),
        SET_RGB(REQUEST, 20, 3),
        SET_LAMP(REQUEST, 22, 2);
        public final short checksum;
        public final byte type;
        public final byte payloadLength;

        Type(short checksum, int type, int payloadLength) {
            this.checksum = checksum;
            this.type = (byte)type;
            this.payloadLength = (byte)payloadLength;
        }

        public byte[] request(int ...data) {
            ByteBuffer requestBuffer = ByteBuffer.allocate(4 + payloadLength)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putShort(checksum)
                    .put(type)
                    .put(payloadLength);
            for (int i : data) {
                requestBuffer.put((byte) i);
            }
            return requestBuffer.array();
        }

    }

    public final static I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create7bit(0x54);

    public Pixy2(I2cDeviceSynch deviceClient) {
        super(deviceClient, true);
        this.elapsedTime = new ElapsedTime();
        this.deviceClient.setI2cAddress(ADDRESS_I2C_DEFAULT);
        // Deals with USB cables getting unplugged
        super.registerArmingStateCallback(false);
        // Sensor starts off disengaged so we can change things like I2C address. Need to engage
        this.deviceClient.engage();
    }

    public void setLED(int red, int green, int blue) {
        byte[] request = Type.SET_RGB.request(red,green,blue);
        this.deviceClient.write(request);
    }

    public void lampOn() {
        setLamp(1,0);
    }

    public void lampOff() {
        setLamp(0,0);
    }

    public void setLamp(int upper, int lower) {
        byte[] request = Type.SET_LAMP.request(upper, lower);
        this.deviceClient.write(request);
    }

    public Block getBlock(int signature) {
        if (elapsedTime.milliseconds() > lastPoll + 50) {
            lastPoll = elapsedTime.milliseconds();
            byte[] request = Type.GET_BLOCKS.request(signature,1);
            this.deviceClient.write(request,I2cWaitControl.NONE);
            byte[] response = this.deviceClient.read(0, 20);
            int responseSignature = getSignature(response);
            if (responseSignature == signature) {
                return new Block(signature, response);
            }
        }
        return null;

    }

    @Override
    protected synchronized boolean doInitialize() {
        return true;
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "Pixycam 2";
    }

    private int getSignature(byte[] response) {
        return bytesToInt(response[7], response[6]);
    }
    private int bytesToInt(byte ms, byte ls) {
        return (ms << 8) + (ls & 0xff);
    }

    /**
     * Block describes the signature, location, and size of a detected block.
     */
    //returns the offset from the x direction
    public int headingOffset(int signature) {
        return getBlock(signature).x - 138;
        //a negative value means rotate left a positive value means rotate right
    }

    //aligns the robot with the pole using pixycam and distances
    public int distanceOffset(int signature, int desiredWidth) {
        return desiredWidth - getBlock(signature).width;
    }
    public class Block  {
        /**
         * A number from 1 through 7 corresponding to the color trained into the PixyCam,
         * or a sequence of octal digits corresponding to the multiple colors of a color code.
         */
        public final int signature;

        /**
         * The x, y location of the center of the detected block.
         * x is in the range (0, 255)
         * 0 is the left side of the field of view and 255 is the right.
         * y is in the range (0, 199)
         * 0 is the top of the field of view and 199 is the bottom.
         */
        public final int x, y;

        /**
         * The size of the detected block.
         * width or height of zero indicates no block detected.
         * maximum width is 255.
         * maximum height is 199.
         */
        public final int width, height;

        public Block(int signature, byte[] response) {
            this.signature = signature;
            this.x = bytesToInt(response[9], response[8]);
            this.y = bytesToInt(response[11], response[10]);
            this.width = bytesToInt(response[13], response[12]);
            this.height = bytesToInt(response[15], response[14]);
        }

        @Override
        public String toString() {
            return String.format("sig: %d, x: %d, y: %d, w: %d, h: %d", this.signature, this.x, this.y, this.width, this.height);
        }

    }

}
