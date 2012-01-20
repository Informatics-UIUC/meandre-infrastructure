package org.meandre.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionFormatter {

    public static String formatException(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        ps.close();

        return baos.toString();
    }
}
