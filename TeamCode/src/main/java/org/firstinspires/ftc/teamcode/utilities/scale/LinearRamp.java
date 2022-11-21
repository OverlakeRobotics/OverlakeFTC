package org.firstinspires.ftc.teamcode.utilities.scale;

/*
The linear ramp maps from the domain to the range at a constant rate.
 */

public class LinearRamp extends Ramp
{
    private double slope;
    private double yIntercept;

    public LinearRamp(Point point1, Point point2)
    {
        super(point1, point2);
        slope = (point2.getY() - point1.getY()) / (point2.getX() - point1.getY());
        yIntercept = point1.getY() - slope * point1.getX();
    }

    @Override
    public double scale(double x)
    {
        return slope * x + yIntercept;
    }

    @Override
    public double inverse(double y)
    {
        return (y - yIntercept) / slope;
    }
}
