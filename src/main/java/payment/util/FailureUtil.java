package payment.util;

public class FailureUtil {
    public static boolean simulate(){
        double randomValue = Math.random() * 100;

        if (randomValue <= 10){
            return true;
        }
        return false;
    }
}
