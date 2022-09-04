package uk.ac.ed.inf;

public class LngLat {
    private double longitude;
    private double latitude;
    public LngLat(double longitude, double latitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean inCentralArea(){
        return true;
    }

    public double distanceTo(LngLat source){
        return 0.1;
    }

    public boolean closeTo(LngLat source){
        return false;
    }

    public LngLat nextPosition(int direction){
        return new LngLat(-3.1912869215011597, 55.945535152517735);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
