package com.agenthun.eseal.bean.updateByRetrofit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/22 15:08.
 */

public class UpdateResponse {
    /**
     * Entity : {"ApkSize":12537361,"Force":false,"UpdateContent":"1.谷歌地图更新\\n2.UI更新","UpdateUrl":"http://www.freight-track.com/files/ESealLite_V2.apk","VersionCode":2,"VersionName":"1.0.1 "}
     * error : {"errorInfo":"","result":1}
     */

    private Entity Entity;
    private Error error;

    public Entity getEntity() {
        return Entity;
    }

    public void setEntity(Entity Entity) {
        this.Entity = Entity;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public static class Entity implements Parcelable {
        /**
         * ApkSize : 12537361
         * Force : false
         * UpdateContent : 1.谷歌地图更新\n2.UI更新
         * UpdateUrl : http://www.freight-track.com/files/ESealLite_V2.apk
         * VersionCode : 2
         * VersionName : 1.0.1
         */

        private int ApkSize;
        private boolean Force;
        private String UpdateContent;
        private String UpdateUrl;
        private int VersionCode;
        private String VersionName;

        public int getApkSize() {
            return ApkSize;
        }

        public void setApkSize(int ApkSize) {
            this.ApkSize = ApkSize;
        }

        public boolean isForce() {
            return Force;
        }

        public void setForce(boolean Force) {
            this.Force = Force;
        }

        public String getUpdateContent() {
            return UpdateContent;
        }

        public void setUpdateContent(String UpdateContent) {
            this.UpdateContent = UpdateContent;
        }

        public String getUpdateUrl() {
            return UpdateUrl;
        }

        public void setUpdateUrl(String UpdateUrl) {
            this.UpdateUrl = UpdateUrl;
        }

        public int getVersionCode() {
            return VersionCode;
        }

        public void setVersionCode(int VersionCode) {
            this.VersionCode = VersionCode;
        }

        public String getVersionName() {
            return VersionName;
        }

        public void setVersionName(String VersionName) {
            this.VersionName = VersionName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.ApkSize);
            dest.writeByte(this.Force ? (byte) 1 : (byte) 0);
            dest.writeString(this.UpdateContent);
            dest.writeString(this.UpdateUrl);
            dest.writeInt(this.VersionCode);
            dest.writeString(this.VersionName);
        }

        public Entity() {
        }

        public Entity(int apkSize, boolean force, String updateContent, String updateUrl, int versionCode, String versionName) {
            ApkSize = apkSize;
            Force = force;
            UpdateContent = updateContent;
            UpdateUrl = updateUrl;
            VersionCode = versionCode;
            VersionName = versionName;
        }

        protected Entity(Parcel in) {
            this.ApkSize = in.readInt();
            this.Force = in.readByte() != 0;
            this.UpdateContent = in.readString();
            this.UpdateUrl = in.readString();
            this.VersionCode = in.readInt();
            this.VersionName = in.readString();
        }

        public static final Parcelable.Creator<Entity> CREATOR = new Parcelable.Creator<Entity>() {
            @Override
            public Entity createFromParcel(Parcel source) {
                return new Entity(source);
            }

            @Override
            public Entity[] newArray(int size) {
                return new Entity[size];
            }
        };

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entity entity = (Entity) o;

            if (ApkSize != entity.ApkSize) return false;
            if (VersionCode != entity.VersionCode) return false;
            return VersionName != null ? VersionName.equals(entity.VersionName) : entity.VersionName == null;

        }

        @Override
        public int hashCode() {
            int result = ApkSize;
            result = 31 * result + VersionCode;
            result = 31 * result + (VersionName != null ? VersionName.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "ApkSize=" + ApkSize +
                    ", Force=" + Force +
                    ", UpdateContent='" + UpdateContent + '\'' +
                    ", UpdateUrl='" + UpdateUrl + '\'' +
                    ", VersionCode=" + VersionCode +
                    ", VersionName='" + VersionName + '\'' +
                    '}';
        }
    }

    public static class Error implements Parcelable {
        /**
         * errorInfo :
         * result : 1
         */

        private String errorInfo;
        private int result;

        public String getErrorInfo() {
            return errorInfo;
        }

        public void setErrorInfo(String errorInfo) {
            this.errorInfo = errorInfo;
        }

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.errorInfo);
            dest.writeInt(this.result);
        }

        public Error() {
        }

        protected Error(Parcel in) {
            this.errorInfo = in.readString();
            this.result = in.readInt();
        }

        public static final Parcelable.Creator<Error> CREATOR = new Parcelable.Creator<Error>() {
            @Override
            public Error createFromParcel(Parcel source) {
                return new Error(source);
            }

            @Override
            public Error[] newArray(int size) {
                return new Error[size];
            }
        };

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Error error = (Error) o;

            if (result != error.result) return false;
            return errorInfo != null ? errorInfo.equals(error.errorInfo) : error.errorInfo == null;

        }

        @Override
        public int hashCode() {
            int result1 = errorInfo != null ? errorInfo.hashCode() : 0;
            result1 = 31 * result1 + result;
            return result1;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "errorInfo='" + errorInfo + '\'' +
                    ", result=" + result +
                    '}';
        }
    }
}
