package helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class UTF8Util {
    public static final String OK = "+OK";
    public static final String ERR = "-ERR";
    public static final String SEP = "\r\n"; // CR LF - Seperator
    private UTF8Util() {}

    /**
     * Liest von dem InputStream. Erwartet ein UTF8Util.OK in der Message.
     * @param inputStream Stream aus dem gelesen wird.
     * @param errorMsg Message die ausgegeben werden soll wenn die Antwort nicht OK ist.
     * @return Antwort die empfangen wurde.
     * @throws Exception
     */
    public static String leseAssertOK(InputStream inputStream, String errorMsg) throws Exception {
        String antwort = lese(inputStream);
        if (!antwort.startsWith(OK))
            throw new Exception(String.format(errorMsg, antwort));
        if (antwort.length() > OK.length())
            return antwort.substring(OK.length() + 1); // Strip UTF8Util.OK and Space-Character.
        else
            return "";
    }

    private static String bytesToString(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, "UTF-8");
    }

    private static void schreibeBytes(OutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    public static void schreibe(OutputStream outputStream, String msg) throws IOException {
        schreibeBytes(outputStream, stringToBytes(msg + SEP));
    }

    public static String lese(InputStream inputStream) throws IOException {
        String read = "";
        while (!read.endsWith(SEP)) {
            int available_size = inputStream.available();
            if (available_size > 0) {
                byte daten[] = new byte[available_size];
                int readSize = inputStream.read(daten);
                if (available_size > readSize) {
                    daten = Arrays.copyOfRange(daten, 0, readSize);
                }
                read += bytesToString(daten);
            }
        }
        int msgEnd = read.length() - 2;
        return read.substring(0, msgEnd);
    }

    private static byte[] stringToBytes(String string) throws UnsupportedEncodingException {
        return string.getBytes("UTF-8");
    }
}
