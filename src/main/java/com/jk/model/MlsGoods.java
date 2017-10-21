package com.jk.model;

import java.io.Serializable;

import org.apache.solr.client.solrj.beans.Field;

public class MlsGoods implements Serializable{
    private static final long serialVersionUID = 7693885957747150412L;
    @Field
	private Long id;
	@Field("subject")
    private String goodname;
	
	@Field("title")
    private String goodinfo;
    
	@Field("price")
    private Float goodprice;
    
	@Field("core0")
    private String goodimgurl;
    
	@Field("flag")
    private Long goodsave;
    
	@Field("status")
    private Long goodstate;
    
	
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoodname() {
        return goodname;
    }

    public void setGoodname(String goodname) {
        this.goodname = goodname == null ? null : goodname.trim();
    }

    public String getGoodinfo() {
        return goodinfo;
    }

    public void setGoodinfo(String goodinfo) {
        this.goodinfo = goodinfo == null ? null : goodinfo.trim();
    }

    public Float getGoodprice() {
        return goodprice;
    }

    public void setGoodprice(Float goodprice) {
        this.goodprice = goodprice;
    }

    public String getGoodimgurl() {
        return goodimgurl;
    }

    public void setGoodimgurl(String goodimgurl) {
        this.goodimgurl = goodimgurl == null ? null : goodimgurl.trim();
    }

    public Long getGoodsave() {
        return goodsave;
    }

    public void setGoodsave(Long goodsave) {
        this.goodsave = goodsave;
    }

    public Long getGoodstate() {
        return goodstate;
    }

    public void setGoodstate(Long goodstate) {
        this.goodstate = goodstate;
    }

	@Override
	public String toString() {
		return "MlsGoods [id=" + id + ", goodname=" + goodname + ", goodinfo=" + goodinfo + ", goodprice=" + goodprice
				+ ", goodimgurl=" + goodimgurl + ", goodsave=" + goodsave + ", goodstate=" + goodstate + "]";
	}

    
    
}