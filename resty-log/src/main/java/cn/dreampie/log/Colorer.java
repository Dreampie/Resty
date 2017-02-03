package cn.dreampie.log;

import org.fusesource.jansi.Ansi;

/**
 * Created by t-baby on 17/2/3.
 */
public class Colorer {

    private static boolean devEnable = true;

    public static String black(String value) {
        return diy("black", value);
    }

    public static String white(String value) {
        return diy("white", value);
    }

    public static String green(String value) {
        return diy("green", value);
    }

    public static String yellow(String value) {
        return diy("yellow", value);
    }

    public static String magenta(String value) {
        return diy("magenta", value);
    }

    public static String red(String value) {
        return diy("red", value);
    }

    public static String cyan(String value) {
        return diy("cyan", value);
    }

    public static String blue(String value) {
        return diy("blue", value);
    }

    public static void devEnable(boolean dev) {
        devEnable = dev;
    }

    private static String diy(String color, String value) {
        if (devEnable) {
            return String.valueOf(Ansi.ansi().eraseScreen().render("@|" + color + " " + value + "|@"));
        } else {
            return value;
        }

    }


}
