package it.unibo.lam.roadsosecurity;
import java.util.Date;

public class Anomaly {

    private double latitude;
    private double longitude;
    private double trust;
    private boolean notified;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getTrust() {
        return trust;
    }

    public void setTrust(double trust) {
        this.trust = trust;
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
