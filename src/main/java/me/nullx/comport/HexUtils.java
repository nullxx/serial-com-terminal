package me.nullx.comport;

public class HexUtils {

    public static byte[] hexStringToByteArray(String text) {
        // text = 5 255 155 2 4
        // each byte is separated by a space
        if (text.length() == 0) {
            return new byte[0];
        }

        String[] split = text.split(" ");
        byte[] bytes = new byte[split.length];

        for (int i = 0; i < split.length; i++) {
            String s = split[i].replace("0x", "").replace("0X", "");
            Integer integer = 0;
            try {
                integer = Integer.parseInt(s, 16);
            } catch (Exception e) {}

            if (integer > 255) {
                integer = 255;
            }

            bytes[i] = integer.byteValue();
        }

        return bytes;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {

            String s = Integer.toHexString(bytes[i] & 0xFF);
            // add a 0 if the string is only 1 character long
            if (s.length() == 1) {
                s = "0" + s;
            }

            sb.append(s);

            if (i != bytes.length - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

}
