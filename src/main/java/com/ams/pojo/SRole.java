package com.ams.pojo;

import java.io.Serializable;

public class SRole implements Serializable {
    private static final long serialVersionUID = -403890272340849181L;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column S_ROLE.DID
     *
     * @mbggenerated
     */
    private Integer did;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column S_ROLE.JSMC
     *
     * @mbggenerated
     */
    private String jsmc;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column S_ROLE.ISXTMR
     *
     * @mbggenerated
     */
    private Integer isxtmr;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column S_ROLE.BZ
     *
     * @mbggenerated
     */
    private String bz;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column S_ROLE.DID
     *
     * @return the value of S_ROLE.DID
     * @mbggenerated
     */
    public Integer getDid() {
        return did;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column S_ROLE.DID
     *
     * @param did the value for S_ROLE.DID
     * @mbggenerated
     */
    public void setDid(Integer did) {
        this.did = did;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column S_ROLE.JSMC
     *
     * @return the value of S_ROLE.JSMC
     * @mbggenerated
     */
    public String getJsmc() {
        return jsmc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column S_ROLE.JSMC
     *
     * @param jsmc the value for S_ROLE.JSMC
     * @mbggenerated
     */
    public void setJsmc(String jsmc) {
        this.jsmc = jsmc == null ? null : jsmc.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column S_ROLE.ISXTMR
     *
     * @return the value of S_ROLE.ISXTMR
     * @mbggenerated
     */
    public Integer getIsxtmr() {
        return isxtmr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column S_ROLE.ISXTMR
     *
     * @param isxtmr the value for S_ROLE.ISXTMR
     * @mbggenerated
     */
    public void setIsxtmr(Integer isxtmr) {
        this.isxtmr = isxtmr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column S_ROLE.BZ
     *
     * @return the value of S_ROLE.BZ
     * @mbggenerated
     */
    public String getBz() {
        return bz;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column S_ROLE.BZ
     *
     * @param bz the value for S_ROLE.BZ
     * @mbggenerated
     */
    public void setBz(String bz) {
        this.bz = bz == null ? null : bz.trim();
    }
}