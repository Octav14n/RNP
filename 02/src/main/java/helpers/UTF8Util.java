package helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by octavian on 06.05.15.
 */
public class UTF8Util {
    private UTF8Util() {}


    public static String bytesToString(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, "UTF-8");
    }

    public static void schreibeBytes(OutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    public static byte[] stringToBytes(String string) throws UnsupportedEncodingException {
        return string.getBytes("UTF-8");
    }
}
