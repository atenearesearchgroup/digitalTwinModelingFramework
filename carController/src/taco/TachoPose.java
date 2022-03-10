package taco;

public class TachoPose {

    private int _leftTC = 0;
    private int _rightTC = 0;
    private float x = 0;
    private float y = 0;
    private float heading = 0;
    private float distance = 0;

    public int getLeftTC() {
        return _leftTC;
    }
    public void setLeftTC(int _leftTC) {
        this._leftTC = _leftTC;
    }
    public int getRightTC() {
        return _rightTC;
    }
    public void setRightTC(int _rightTC) {
        this._rightTC = _rightTC;
    }
    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }
    public float getHeading() {
        return heading;
    }
    public void setHeading(float heading) {
        this.heading = heading;
    }
    public float getDistance() {
        return distance;
    }
    public void setDistance(float distance) {
        this.distance = distance;
    }


}
