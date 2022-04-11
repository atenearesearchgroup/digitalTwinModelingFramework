package taco;
import car.Car;
import lejos.nxt.Motor;
import lejos.robotics.RegulatedMotor;

public class TachoPoseProvider {

    private final RegulatedMotor _left;
    private final RegulatedMotor _right;
    private final byte _parity = 1;
    private final int _leftTC;
    private final int _rightTC;
    private TachoPose lastTachoPose;
    private final float _leftDegPerDistance;
    private final float _rightDegPerDistance;
    private final float _rightTurnRatio;
    private final float _leftTurnRatio;

    public TachoPoseProvider(TachoPose tachoPose, Car c, RegulatedMotor left, RegulatedMotor right) {
        double wd = Double.parseDouble(c.WHEEL_DIAMETER);
        double tw = Double.parseDouble(c.TRACK_WIDTH);
        this._left = left;
        this._right = right;
        this._left.resetTachoCount();
        this._right.resetTachoCount();
        lastTachoPose = tachoPose;
        _leftTC = tachoPose.getLeftTC();
        _rightTC = tachoPose.getRightTC();
        _leftDegPerDistance = (float) (360 / (Math.PI * wd)); // 360 degrees / wheel longitude
        _rightDegPerDistance = (float) (360 / (Math.PI * wd));
        _leftTurnRatio = (float) (tw / wd);
        _rightTurnRatio = (float) (tw / wd);
    }

    public TachoPose getPose() {
        float angleIncrement = getAngleIncrement();
        float movementIncrement = getMovementIncrement();

        float angle = angleIncrement - lastTachoPose.getHeading();
        float distance = movementIncrement - lastTachoPose.getDistance();
        float dx = 0, dy = 0;
        float headingRad = (float) (Math.toRadians(normalize(angleIncrement)));

        dx = (distance) * (float) Math.cos(headingRad);
        dy = (distance) * (float) Math.sin(headingRad);

        lastTachoPose.setX(lastTachoPose.getX() + dx);
        lastTachoPose.setY(lastTachoPose.getY() + dy);
        lastTachoPose.setDistance(movementIncrement);
        lastTachoPose.setHeading(normalize(angleIncrement));
        lastTachoPose.setLeftTC(getLeftCount());
        lastTachoPose.setRightTC(getRightCount());

        return lastTachoPose;
    }

    /*
     * returns equivalent angle between -180 and +180
     */
    private float normalize(float angle) {
        float a = angle;
        while (a > 180)
            a -= 360;
        while (a < -180)
            a += 360;
        return a;
    }

    public void setPose(TachoPose aPose) {
        lastTachoPose = aPose;
    }

    private int getLeftCount() {
        return _parity * _left.getTachoCount();
    }

    private int getRightCount() {
        return _parity * _right.getTachoCount();
    }

    public float getMovementIncrement() {
        float left = (getLeftCount() - _leftTC) / _leftDegPerDistance;
        float right = (getRightCount() - _rightTC) / _rightDegPerDistance;
        return (left + right) / 2.0f;
    }

    public float getAngleIncrement() {
        return (((getRightCount() - _rightTC) / _rightTurnRatio) - ((getLeftCount() - _leftTC) / _leftTurnRatio)) / 2.0f;
    }

}