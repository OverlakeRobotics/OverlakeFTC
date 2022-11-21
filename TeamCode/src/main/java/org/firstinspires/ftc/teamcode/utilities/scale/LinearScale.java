package org.firstinspires.ftc.teamcode.utilities.scale;

/**
 * Created by EvanCoulson on 9/1/18.
 */

public class LinearScale implements IScale
{
    private double scaleFactor;
    private double scaleOffset;

    public LinearScale(double scaleFactor, double scaleOffset)
    {
        this.scaleFactor = scaleFactor;
        this.scaleOffset = scaleOffset;
    }

    @Override
    public double scaleX(double x)
    {
        return scaleFactor * x + scaleOffset;
    }

    @Override
    public double scaleY(double y)
    {
        return (y - scaleOffset) / scaleFactor;
    }
}
