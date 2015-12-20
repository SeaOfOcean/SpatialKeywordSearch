package util;

public class MoreMath {

    final public static boolean approximately_equal(double a, double b,
                                                    double epsilon) {
        return (Math.abs(a - b) <= epsilon);
    }
}