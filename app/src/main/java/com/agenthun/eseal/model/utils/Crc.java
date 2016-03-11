package com.agenthun.eseal.model.utils;

import java.nio.ByteBuffer;

/**
 * Created by agenthun on 16/3/11.
 */
public class Crc {
    private static final int[] CRC16Table = {
            0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50A5, 0x60C6, 0x70E7,
            0x8108, 0x9129, 0xA14A, 0xB16B, 0xC18C, 0xD1AD, 0xE1CE, 0xF1EF
    };

    public Crc() {
    }

    public static short getCRC16(byte[] data, int len) {
        int val = 0xffff;
        for (int i = 0; i < len; i++) {
            byte tmp = (byte) (val >> 12);
            val = (val << 4) & 0xffff;
            val ^= CRC16Table[tmp ^ ((data[i] & 0xff) >> 4)];

            tmp = (byte) (val >> 12);
            val = (val << 4) & 0xffff;
            val ^= CRC16Table[tmp ^ (data[i] & 0x0f)];
        }
        return (short) (val & 0xffff);
    }

    public static short getCRC16(short data1, short data2) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putShort(data1);
        buffer.putShort(data2);
        return getCRC16(buffer.array(), 4);
    }
}
