package utils;

public class Timer {
    private long startMillis = 0;
    public double getElapsedTimeInSeconds() {
        if(startMillis == 0)
            return -1;
        return (System.currentTimeMillis() - this.startMillis) / 1000.0;
    }
    public void reset() {
        this.startMillis = System.currentTimeMillis();
    }


}