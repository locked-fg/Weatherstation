package de.locked.weatherstation;

public class Daemon extends Thread {

    public Daemon(Runnable r) {
        super(r);
        setDaemon(true);
    }

}
