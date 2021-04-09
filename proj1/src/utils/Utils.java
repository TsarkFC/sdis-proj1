package utils;

public class Utils {
    public static int generateRandomDelay() {
        int random = (int) (Math.random() * (400+1));
        System.out.println("Random delay: " + random);
        return random ;
    }
}
