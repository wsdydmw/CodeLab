package com.jerry.lab.charset;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

/**
 * @author helong06
 * @version 1.0.0
 * @date 2020/9/18 18:10
 */
public class CharsetTest {
    public static void main(String[] args) throws UnsupportedEncodingException {
        final String TEST_STRING = "淘！我喜欢！";
        final String[] types = new String[]{"GBK", "UTF-8", "ISO8859-1", "Unicode"};

        for (String encodeType : types) {
            for (String decodeType : types) {
                testAutomatic(TEST_STRING, encodeType, decodeType);
            }
        }
    }


    public static void testAutomatic(String srcString, String encodeType,
                                     String decodeType) throws UnsupportedEncodingException {
        System.out.println(String.format("源字符(Unicode编码值) -> 按 %s 编码 -> 按 %s 解码", encodeType, decodeType));
        for (int index = 0; index < srcString.length(); index++) {
            char srcChar = srcString.charAt(index);
            String srcCharHex = Integer.toHexString(srcChar);
            byte[] encodeDes = String.valueOf(srcChar).getBytes(encodeType);
            StringBuilder encodeDesHexBuilder = new StringBuilder();
            IntStream.range(0, encodeDes.length).forEach(encodeDesIndex -> {
                encodeDesHexBuilder.append(Integer.toHexString(encodeDes[encodeDesIndex] & 0xFF) + " ");
            });
            String decodeDes = new String(encodeDes, decodeType);
            System.out.println(String.format("%2s(%-4s) -> %-5s -> %-5s", srcChar, srcCharHex, encodeDesHexBuilder.toString(), decodeDes));
        }
        System.out.println(String.format("最终结果 -> %s", new String(srcString.getBytes(encodeType), decodeType)));
        System.out.println();
    }


    public static void testSingle() throws UnsupportedEncodingException {
        String encodeSrc = "淘";
        // 编码
        /**
         * 淘 ISO8859-1 ->3f
         * 왜 GBK ->3f
         * */
        byte[] encodeDes = encodeSrc.getBytes("ISO8859-1");
        for (int i = 0; i < encodeDes.length; i++) {
            System.out.println(Integer.toHexString(encodeDes[i]));
        }


        // 解码
        /**
         * 137 ISO8859-1 -> (编码之外无显示)
         * 52,57 GBK ->49 （只当做单个字符解码）
         * 170,58 GBK ->�（编码之外）
         * 255,254 Unicode -> (编码之外无显示)
         * 255,254 UTF-8 ->�� (编码之外)
         */
        byte[] decodeSrc = {(byte) 255, (byte) 254};
        String decodeDes = new String(decodeSrc, StandardCharsets.UTF_8);
        System.out.println("解码结果为：" + decodeDes);
    }

}
