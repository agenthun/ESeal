package com.agenthun.eseal.model.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.agenthun.eseal.R;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/8/7 14:46.
 */
public class SettingType implements Parcelable {
    public static final String EXTRA_DEVICE = "SettingTypeParcelable";

    private String containerNumber;
    private String owner;
    private String freightName;
    private String origin;
    private String destination;
    private String vessel;
    private String voyage;
    private String frequency;
    private String NfcTagId;

    public String getContainerNumber() {
        return containerNumber;
    }

    public void setContainerNumber(String containerNumber) {
        this.containerNumber = containerNumber;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getFreightName() {
        return freightName;
    }

    public void setFreightName(String freightName) {
        this.freightName = freightName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getVessel() {
        return vessel;
    }

    public void setVessel(String vessel) {
        this.vessel = vessel;
    }

    public String getVoyage() {
        return voyage;
    }

    public void setVoyage(String voyage) {
        this.voyage = voyage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getNfcTagId() {
        return NfcTagId;
    }

    public void setNfcTagId(String nfcTagId) {
        NfcTagId = nfcTagId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static Creator<SettingType> getCREATOR() {
        return CREATOR;
    }

    public static final Creator<SettingType> CREATOR = new Creator<SettingType>() {

        @Override
        public SettingType createFromParcel(Parcel source) {
            SettingType setting = new SettingType();

            setting.containerNumber = source.readString();
            setting.owner = source.readString();
            setting.freightName = source.readString();
            setting.origin = source.readString();
            setting.destination = source.readString();
            setting.vessel = source.readString();
            setting.voyage = source.readString();
            setting.frequency = source.readString();
            setting.NfcTagId = source.readString();

            return setting;
        }

        @Override
        public SettingType[] newArray(int size) {
            return new SettingType[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(containerNumber);
        dest.writeString(owner);
        dest.writeString(freightName);
        dest.writeString(origin);
        dest.writeString(destination);
        dest.writeString(vessel);
        dest.writeString(voyage);
        dest.writeString(frequency);
        dest.writeString(NfcTagId);
    }

    @Override
    public String toString() {
        return "箱号 " + containerNumber + "\r\n\r\n" +
                "货主 " + owner + "\r\n\r\n" +
                "货物 " + freightName + "\r\n\r\n" +
                "起始地 " + origin + "\r\n\r\n" +
                "目的地 " + destination + "\r\n\r\n" +
                "航班 " + vessel + "\r\n\r\n" +
                "航次 " + voyage + "\r\n\r\n" +
                "上报周期(s) " + frequency + "\r\n\r\n" +
                "NFC封条ID " + NfcTagId;
    }

    public String getSettingTypeString() {
        return containerNumber + "\r\n" +
                owner + "\r\n" +
                freightName + "\r\n" +
                origin + "\r\n" +
                destination + "\r\n" +
                vessel + "\r\n" +
                voyage + "\r\n" +
                frequency + "\r\n" +
                NfcTagId;
    }
}
