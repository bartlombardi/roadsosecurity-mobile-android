package it.unibo.lam.roadsosecurity;

public class Anomaly {

    private double latitude;
    private double longitude;
    private double trust;
    private boolean notified;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getTrust() {
        return trust;
    }

    public void setNotified (boolean notified) { this.notified = notified; }

    public boolean getNotified() {return notified;}

    public Anomaly(double latitude,double longitude,double trust){

        this.latitude = latitude;
        this.longitude = longitude;
        this.trust = trust;
        this.notified = false;
    }

    public Anomaly(double latitude,double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
        this.trust = 50;
        this.notified = true;
    }
}