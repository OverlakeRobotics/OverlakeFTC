package org.firstinspires.ftc.teamcode.utilities.scale;

/*
    The exponential ramp increases slowly for low values and speeds up as it approaches the higher values.

    It is of the form f(x) = J * exp(K*x).

    Where J and K are constants computed such that
    f(x1) = y1 and f(x2) = y2 for the given (x1, y1) and (x2, y2).

    f(x1) = y1:                y1 = J*exp(K*x1)
    f(x2) = y2:                y2 = J*exp(K*x2)

    Divide the two equation:   y1/y2 = J*exp(K*x1)/J*exp(K*x2)    where y2 != 0

    Factor out J:              y1/y2 = exp(K*x1)/exp(K*x2)
    Combine the exponents:     y1/y2 = exp(K*x1 - K*x2)
    Log both sides:            log(y1/y2) = log(exp(K*x1 - K*x2))
    log and exp cancel:        log(y1/y2) = K*x1 - K*x2
    Factor out K:              log(y1/y2) = K*(x1 - x2)
    So:                        K = log(y1/y2) / (x1 - x2)

    Now solve for J in terms of K and (x1, y1)

    f(x1) = y1:                y1 = J*exp(R*x1)
    So:                        J = y1/exp(R*x1)

    The inverse of y = f(x) = J*exp(K*x) is:

    Log both sides:                 log(J*exp(K*x)) = log(y)
    Log of product is sum of logs:  log(J) + log(exp(K*x)) = log(y)
    Log and exp cancel:             log(J) + K*x = log(y)
    Rearrange:                      K*x = log(y) - log(J)
    Divide both sides by K:         f'(y) = x = (log(y) - log(J)) / K
*/
public class ExponentialRamp extends Ramp
{
    private double J;
    private double K;

    public ExponentialRamp(Point point1, Point point2)
    {
        super(point1, point2);
        K = Math.log(point1.getY() / point2.getY()) / (point1.getX() - point2.getX());
        J = point1.getY() / Math.exp(K * point1.getX());
    }

    @Override
    protected double scale(double x)
    {
        return J * Math.exp(K * x);
    }

    @Override
    protected double inverse(double y)
    {
        return (Math.log(y / J)) / K;
    }
}
