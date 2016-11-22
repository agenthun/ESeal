package com.agenthun.eseal.connectivity.nfc;

import android.annotation.TargetApi;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/7/21 21:47.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class NfcUtility implements NfcAdapter.ReaderCallback {
    private static final String TAG = "NfcUtility";

    public static int NFC_TAG_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

    private static final String TAG_AID = "F222222222";
    private static final String SELECT_APDU_HEADER = "00A40400";
    private static final byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};

    private WeakReference<TagCallback> mTagCallback;

    public NfcUtility(TagCallback tagCallback) {
        mTagCallback = new WeakReference<TagCallback>(tagCallback);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        String tagId = ByteArrayToHexString(tag.getId());
        Log.d(TAG, "onTagDiscovered() returned: " + tagId);
        mTagCallback.get().onTagReceived(tagId);
/*        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep != null) {
            try {
                isoDep.connect();
                byte[] command = BuildSelectApdu(TAG_AID);
                byte[] result = isoDep.transceive(command);
                int resultLength = result.length;
                byte[] statusWord = {result[resultLength - 2], result[resultLength - 1]};
                byte[] payload = Arrays.copyOf(result, resultLength - 2);
                if (Arrays.equals(SELECT_OK_SW, statusWord)) {
                    String number = new String(payload, "UTF-8");
                    mTagCallback.get().onTagReceived(number);
                    Log.d(TAG, "onTagDiscovered() number: " + number);
                }
            } catch (IOException e) {
                Log.d(TAG, "onTagDiscovered: " + e.toString());
            }
        }*/

        mTagCallback.get().onTagRemoved();
    }

    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X", aid.length() / 2) + aid);
    }

    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public interface TagCallback {
        public void onTagReceived(String tag);

        public void onTagRemoved();
    }
}
