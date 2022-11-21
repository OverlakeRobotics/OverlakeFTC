package org.firstinspires.ftc.teamcode.utilities.scale;

/*
    The logarithmic ramp increases quickly for low values and slows down as it approaches the higher values.

    It is of the form g(x) = Q*log(R*x)

    Where Q and R are constants computed such that
    g(x1) = y1 and f(x2) = y2 for the given (x1, y1) and (x2, y2).

    g(x1) = y1 so:                  y1 = Q*log(R*x1)
    g(x2) = y2 so:                  y2 = Q*log(R*x2)
    Subtract to two equation:       y2 - y1 = Q*log(R*x2) - Q*log(R*x1)
    Factor out Q:                   y2 - y1 = Q*(log(R*x2) - (log(R*x1)))
    Log of product is sum of logs:  y2 - y1 = Q*((log(R) + log(x2)) - ((log(R) + log(x1))))
    Rearrange:                      y2 - y1 = Q*(log(R) - log(R) + log(x2) - log(x1))
    Reduce:                         y2 - y1 = Q*(log(x2) - log(x1))
    Combine the logs:               y2 - y1 = Q*log(x2/x1)
    Result:                         Q = (y2 - y1)/(log(x2/x1)

    Substitute the above expression for Q and (x1, y1) in g(x) = Q*log(R*x) and solve for R

    y1 = Q*log(R*x1)
    Divide both sides by Q:         y1/Q = log(R*x1)
    Exp both sides:                 exp(y1/Q) = exp(log(R*x1))
    Log and exp cancel:             exp(y1/Q) = R*x1
    Divide both sides by x1:        R = exp(y1/Q)/x1              where x1 != 0

    The inverse of y = g(x) = Q*log(R*x) is:

    Exp both side:                  exp(Q*log(R*x)) = exp(y)
    Exp of product is sum of exps:  exp(Q) + exp(log(R*x)) = exp(y)
    Exp and log cancel:             R*x = exp(y) - exp(Q)
    Divide both sides by R:         g'(y) = x = (exp(y) - exp(Q))/R
 */

public class LogarithmicRamp extends Ramp
{
    private double Q;
    private double R;

    public LogarithmicRamp(Point point1, Point point2)
    {
        super(point1, point2);
        if (point1.getX() == 0 || point2.getX() == 0) {
            throw new IllegalArgumentException("Logarithmic scales can start at x=0");
        }

        Q = (point2.getY() - point1.getY()) / Math.log(point2.getX() / point1.getX());
        R = Math.exp(point1.getY() / Q) / point1.getX();
    }

    @Override
    protected double scale(double x)
    {
        return Q * Math.log(R * x);
    }

    @Override
    protected double inverse(double y)
    {
        return Math.exp(y / Q) / R;
    }
}
