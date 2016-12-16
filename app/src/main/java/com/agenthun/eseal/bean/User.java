package com.agenthun.eseal.bean;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/2 下午4:56.
 */
public class User {
    private Integer EFFECTIVETOKEN;
    private String EMAIL;
    private String ENTERPRISE;
    private String ERRORINFO;
    private String FREIGHTOWNER;
    private String IMGURL;
    private String ISEMAIL;
    private String ISSMS;
    private String MOBILE;
    private String REALNAME;
    private Integer RESULT;
    private String ROLEID;
    private String TOKEN;
    private String USERID;
    private String USERNAME;

    public User() {

    }

    public User(String EMAIL, String ENTERPRISE,
                String FREIGHTOWNER, String IMGURL,
                String MOBILE, String REALNAME,
                String ROLEID, String TOKEN,
                String USERID, String USERNAME) {
        this(1, EMAIL,
                ENTERPRISE, "",
                FREIGHTOWNER, IMGURL,
                "1", "1",
                MOBILE, REALNAME,
                1, ROLEID,
                TOKEN, USERID,
                USERNAME);
    }

    public User(Integer EFFECTIVETOKEN, String EMAIL,
                String ENTERPRISE, String ERRORINFO,
                String FREIGHTOWNER, String IMGURL,
                String ISEMAIL, String ISSMS,
                String MOBILE, String REALNAME,
                Integer RESULT, String ROLEID,
                String TOKEN, String USERID,
                String USERNAME) {
        this.EFFECTIVETOKEN = EFFECTIVETOKEN;
        this.EMAIL = EMAIL;
        this.ENTERPRISE = ENTERPRISE;
        this.ERRORINFO = ERRORINFO;
        this.FREIGHTOWNER = FREIGHTOWNER;
        this.IMGURL = IMGURL;
        this.ISEMAIL = ISEMAIL;
        this.ISSMS = ISSMS;
        this.MOBILE = MOBILE;
        this.REALNAME = REALNAME;
        this.RESULT = RESULT;
        this.ROLEID = ROLEID;
        this.TOKEN = TOKEN;
        this.USERID = USERID;
        this.USERNAME = USERNAME;
    }

    public Integer getEFFECTIVETOKEN() {
        return EFFECTIVETOKEN;
    }

    public void setEFFECTIVETOKEN(Integer EFFECTIVETOKEN) {
        this.EFFECTIVETOKEN = EFFECTIVETOKEN;
    }

    public String getEMAIL() {
        return EMAIL;
    }

    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    public String getENTERPRISE() {
        return ENTERPRISE;
    }

    public void setENTERPRISE(String ENTERPRISE) {
        this.ENTERPRISE = ENTERPRISE;
    }

    public String getERRORINFO() {
        return ERRORINFO;
    }

    public void setERRORINFO(String ERRORINFO) {
        this.ERRORINFO = ERRORINFO;
    }

    public String getFREIGHTOWNER() {
        return FREIGHTOWNER;
    }

    public void setFREIGHTOWNER(String FREIGHTOWNER) {
        this.FREIGHTOWNER = FREIGHTOWNER;
    }

    public String getIMGURL() {
        return IMGURL;
    }

    public void setIMGURL(String IMGURL) {
        this.IMGURL = IMGURL;
    }

    public String getISEMAIL() {
        return ISEMAIL;
    }

    public void setISEMAIL(String ISEMAIL) {
        this.ISEMAIL = ISEMAIL;
    }

    public String getISSMS() {
        return ISSMS;
    }

    public void setISSMS(String ISSMS) {
        this.ISSMS = ISSMS;
    }

    public String getMOBILE() {
        return MOBILE;
    }

    public void setMOBILE(String MOBILE) {
        this.MOBILE = MOBILE;
    }

    public String getREALNAME() {
        return REALNAME;
    }

    public void setREALNAME(String REALNAME) {
        this.REALNAME = REALNAME;
    }

    public Integer getRESULT() {
        return RESULT;
    }

    public void setRESULT(Integer RESULT) {
        this.RESULT = RESULT;
    }

    public String getROLEID() {
        return ROLEID;
    }

    public void setROLEID(String ROLEID) {
        this.ROLEID = ROLEID;
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public void setTOKEN(String TOKEN) {
        this.TOKEN = TOKEN;
    }

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    @Override
    public String toString() {
        return "User{" +
                "EFFECTIVETOKEN=" + EFFECTIVETOKEN +
                ", EMAIL='" + EMAIL + '\'' +
                ", ENTERPRISE='" + ENTERPRISE + '\'' +
                ", ERRORINFO='" + ERRORINFO + '\'' +
                ", FREIGHTOWNER='" + FREIGHTOWNER + '\'' +
                ", IMGURL='" + IMGURL + '\'' +
                ", ISEMAIL='" + ISEMAIL + '\'' +
                ", ISSMS='" + ISSMS + '\'' +
                ", MOBILE='" + MOBILE + '\'' +
                ", REALNAME='" + REALNAME + '\'' +
                ", RESULT=" + RESULT +
                ", ROLEID='" + ROLEID + '\'' +
                ", TOKEN='" + TOKEN + '\'' +
                ", USERID='" + USERID + '\'' +
                ", USERNAME='" + USERNAME + '\'' +
                '}';
    }
}
