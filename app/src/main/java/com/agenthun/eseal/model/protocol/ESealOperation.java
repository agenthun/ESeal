package com.agenthun.eseal.model.protocol;

import com.agenthun.eseal.model.utils.Crc;
import com.agenthun.eseal.model.utils.Encrypt;
import com.agenthun.eseal.model.utils.SensorType;

import java.nio.ByteBuffer;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/11 下午10:18.
 */
public class ESealOperation {
    public static final int ESEALBD_OPERATION_PORT = 0xA002;
    private static final short ESEALBD_OPERATION_CMD_MAX_SIZE = 256;

    public static final short ESEALBD_OPERATION_REQUEST_SIZE_QUERY = (2 + 2 + 2 + 4);
    public static final short ESEALBD_OPERATION_REQUEST_SIZE_CONFIG = (2 + 2 + 2 + 4 + 2 + 1 + 1 + 9);
    public static final short ESEALBD_OPERATION_REQUEST_SIZE_OPERATION = (2 + 2 + 2 + 4 + 1 + 1);
    public static final short ESEALBD_OPERATION_REQUEST_SIZE_WRITE_DATA_WITHOUT_DLEN = (2 + 2 + 2 + 4 + 2);
    public static final short ESEALBD_OPERATION_REQUEST_SIZE_READ_DATA = (2 + 2 + 2 + 4 + 2);
    public static final short ESEALBD_OPERATION_REQUEST_SIZE_CLEAR = (2 + 2 + 2 + 4 + 2);
    public static final short ESEALBD_OPERATION_REQUEST_SIZE_INFO = (2 + 2 + 2 + 4);

    private static final short ESEALBD_OPERATION_TYPE_QUERY = 0x2F00;
    private static final short ESEALBD_OPERATION_TYPE_CONFIG = 0x2F01;
    private static final short ESEALBD_OPERATION_TYPE_OPERATION = 0x2F0C;
    private static final short ESEALBD_OPERATION_TYPE_WRITE_DATA = 0x2FD0;
    private static final short ESEALBD_OPERATION_TYPE_READ_DATA = 0x2FD1;
    private static final short ESEALBD_OPERATION_TYPE_CLEAR = 0x2FD3;
    private static final short ESEALBD_OPERATION_TYPE_INFO = 0x2FD2;

    private static final short ESEALBD_OPERATION_TYPE_REPLAY_ERROR = 0x1F0F;
    private static final short ESEALBD_OPERATION_TYPE_REPLAY_QUERY = 0x1F00;
    private static final short ESEALBD_OPERATION_TYPE_REPLAY_READ_DATA = 0x1FD1;
    private static final short ESEALBD_OPERATION_TYPE_REPLAY_INFO = 0x1FD0;

    public static final byte POWER_OFF = 0;
    public static final byte POWER_ON = 1;
    public static final byte SAFE_LOCK = 0;
    public static final byte SAFE_UNLOCK = 1;

    public static final int PERIOD_DEFAULT = 60;
    public static final short WINDOW_DEFAULT = 30;
    public static final byte CHANNEL_DEFAULT = 1;

    public ESealOperation() {
    }

    //查询状态报文-加密
    public static byte[] operationQuery(int id, int rn, int key) {
        ByteBuffer buffer = ByteBuffer.allocate(ESEALBD_OPERATION_REQUEST_SIZE_QUERY);
        buffer.putShort(ESEALBD_OPERATION_TYPE_QUERY);
        buffer.putShort((short) 4);
        buffer.putInt(id);
        byte[] temp = buffer.array();
        short crc = Crc.getCRC16(temp, ESEALBD_OPERATION_REQUEST_SIZE_QUERY - 2);
        buffer.clear();

        buffer.putShort(crc);
        buffer.putShort(ESEALBD_OPERATION_TYPE_QUERY);
        buffer.putShort((short) 4);
        buffer.putInt(id);
        temp = buffer.array();

        Encrypt.encrypt(id, rn, key, temp, ESEALBD_OPERATION_REQUEST_SIZE_QUERY);
        return temp;
    }

    /**
     * 配置报文-加密
     * period->上传周期(60-65535 s), (0-59 s)停止上传
     * window->开窗宽度(5-255 s), (0-4 s)一直开窗
     * channel->通道模式 00 自动, 01 GPRS, 02 北斗, 03 GPRS/北斗, 04 D+, 05 GPRS/D+
     * sensorType->温度/湿度/振动传感器-使能及临界值
     */
    public static byte[] operationConfig(int id, int rn, int key, int period, short window, byte channel, SensorType sensorType) {
        ByteBuffer buffer = ByteBuffer.allocate(ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
        buffer.putShort(ESEALBD_OPERATION_TYPE_CONFIG);
        buffer.putShort((short) 0x11);
        buffer.putInt(id);
        buffer.putShort((short) (period & 0xffff));
        buffer.put((byte) (window & 0xff));
        buffer.put(channel);

        buffer.put(sensorType.getTemperatureEn());
        buffer.putShort(sensorType.getTemperature());
        buffer.put(sensorType.getHumidityEn());
        buffer.putShort(sensorType.getHumidity());
        buffer.put(sensorType.getShakeEn());
        buffer.putShort(sensorType.getShake());
        byte[] temp = buffer.array();
        short crc = Crc.getCRC16(temp, ESEALBD_OPERATION_REQUEST_SIZE_CONFIG - 2);
        buffer.clear();

        buffer.putShort(crc);
        buffer.putShort(ESEALBD_OPERATION_TYPE_CONFIG);
        buffer.putShort((short) 0x11);
        buffer.putInt(id);
        buffer.putShort((short) (period & 0xffff));
        buffer.put((byte) (window & 0xff));
        buffer.put(channel);

        buffer.put(sensorType.getTemperatureEn());
        buffer.putShort(sensorType.getTemperature());
        buffer.put(sensorType.getHumidityEn());
        buffer.putShort(sensorType.getHumidity());
        buffer.put(sensorType.getShakeEn());
        buffer.putShort(sensorType.getShake());
        temp = buffer.array();

        Encrypt.encrypt(id, rn, key, temp, ESEALBD_OPERATION_REQUEST_SIZE_CONFIG);
        return temp;
    }

    /**
     * 操作报文-加密
     * power->00 关机, 01 开机
     * safe->00 上封, 01 解封
     */
    public static byte[] operationOperation(int id, int rn, int key, byte power, byte safe) {
        ByteBuffer buffer = ByteBuffer.allocate(ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        buffer.putShort(ESEALBD_OPERATION_TYPE_OPERATION);
        buffer.putShort((short) 6);
        buffer.putInt(id);
        buffer.put(power);
        buffer.put(safe);
        byte[] temp = buffer.array();
        short crc = Crc.getCRC16(temp, ESEALBD_OPERATION_REQUEST_SIZE_OPERATION - 2);
        buffer.clear();

        buffer.putShort(crc);
        buffer.putShort(ESEALBD_OPERATION_TYPE_OPERATION);
        buffer.putShort((short) 6);
        buffer.putInt(id);
        buffer.put(power);
        buffer.put(safe);
        temp = buffer.array();

        Encrypt.encrypt(id, rn, key, temp, ESEALBD_OPERATION_REQUEST_SIZE_OPERATION);
        return temp;
    }

    //写数据报文-加密
    public static byte[] operationWriteData(int id, int rn, int key, byte[] writeData, short writeLen) {
        ByteBuffer buffer = ByteBuffer.allocate(ESEALBD_OPERATION_REQUEST_SIZE_WRITE_DATA_WITHOUT_DLEN + writeLen);
        buffer.putShort(ESEALBD_OPERATION_TYPE_WRITE_DATA);
        buffer.putShort((short) (6 + writeLen));
        buffer.putInt(id);
        buffer.putShort(writeLen);
        buffer.put(writeData, 0, writeLen);
        byte[] temp = buffer.array();
        short crc = Crc.getCRC16(temp, ESEALBD_OPERATION_REQUEST_SIZE_WRITE_DATA_WITHOUT_DLEN + writeLen - 2);
        buffer.clear();

        buffer.putShort(crc);
        buffer.putShort(ESEALBD_OPERATION_TYPE_WRITE_DATA);
        buffer.putShort((short) (6 + writeLen));
        buffer.putInt(id);
        buffer.putShort(writeLen);
        buffer.put(writeData, 0, writeLen);
        temp = buffer.array();

        Encrypt.encrypt(id, rn, key, temp, ESEALBD_OPERATION_REQUEST_SIZE_WRITE_DATA_WITHOUT_DLEN + writeLen);
        return temp;
    }

    //读数据报文-加密
    public static byte[] operationReadData(int id, int rn, int key, short readLen) {
        ByteBuffer buffer = ByteBuffer.allocate(ESEALBD_OPERATION_REQUEST_SIZE_READ_DATA);
        buffer.putShort(ESEALBD_OPERATION_TYPE_READ_DATA);
        buffer.putShort((short) 6);
        buffer.putInt(id);
        buffer.putShort(readLen);
        byte[] temp = buffer.array();
        short crc = Crc.getCRC16(temp, ESEALBD_OPERATION_REQUEST_SIZE_READ_DATA - 2);
        buffer.clear();

        buffer.putShort(crc);
        buffer.putShort(ESEALBD_OPERATION_TYPE_READ_DATA);
        buffer.putShort((short) 6);
        buffer.putInt(id);
        buffer.putShort(readLen);
        temp = buffer.array();

        Encrypt.encrypt(id, rn, key, temp, ESEALBD_OPERATION_REQUEST_SIZE_READ_DATA);
        return temp;
    }

    //擦除数据报文-加密
    public static byte[] operationClear(int id, int rn, int key) {
        ByteBuffer buffer = ByteBuffer.allocate(ESEALBD_OPERATION_REQUEST_SIZE_CLEAR);
        buffer.putShort(ESEALBD_OPERATION_TYPE_CLEAR);
        buffer.putShort((short) 6);
        buffer.putInt(id);
        byte[] temp = buffer.array();
        short crc = Crc.getCRC16(temp, ESEALBD_OPERATION_REQUEST_SIZE_CLEAR - 2);
        buffer.clear();

        buffer.putShort(crc);
        buffer.putShort(ESEALBD_OPERATION_TYPE_CLEAR);
        buffer.putShort((short) 6);
        buffer.putInt(id);
        temp = buffer.array();

        Encrypt.encrypt(id, rn, key, temp, ESEALBD_OPERATION_REQUEST_SIZE_CLEAR);
        return temp;
    }

    //请求信息报文-加密
    public static byte[] operationInfo(int id, int rn, int key) {
        ByteBuffer buffer = ByteBuffer.allocate(ESEALBD_OPERATION_REQUEST_SIZE_INFO);
        buffer.putShort(ESEALBD_OPERATION_TYPE_INFO);
        buffer.putShort((short) 4);
        buffer.putInt(id);
        byte[] temp = buffer.array();
        short crc = Crc.getCRC16(temp, ESEALBD_OPERATION_REQUEST_SIZE_INFO - 2);
        buffer.clear();

        buffer.putShort(crc);
        buffer.putShort(ESEALBD_OPERATION_TYPE_INFO);
        buffer.putShort((short) 4);
        buffer.putInt(id);
        temp = buffer.array();

        Encrypt.encrypt(id, rn, key, temp, ESEALBD_OPERATION_REQUEST_SIZE_INFO);
        return temp;
    }
}
