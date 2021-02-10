package org.misio.config;

public class CurveEncryptUtil {

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] bytes = new byte[len / 2];

        for (int i = 0; i < bytes.length; ++i) {
            int j = i * 2;
            int t = Integer.parseInt(s.substring(j, j + 2), 16);
            byte b = (byte) (t & 0xFF);
            bytes[i] = b;
        }
        return bytes;
    }

}
