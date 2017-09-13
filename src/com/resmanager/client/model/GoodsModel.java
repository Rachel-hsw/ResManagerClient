package com.resmanager.client.model;

import java.io.Serializable;

public class GoodsModel implements Serializable {
	/** 
	*/
	private static final long serialVersionUID = 1L;
	private String GoodsnameID;// 产品ID
	private String GoodsName;// 产品名字
	private String Packagetype;// 包装物名称
	private String PackagetypeID;
	private String fmodel;
	private String ID;
	private int recycle;
	public int getRecycle() {
		return recycle;
	}

	public void setRecycle(int recycle) {
		this.recycle = recycle;
	}

	public int getP_ID() {
		return P_ID;
	}

	public void setP_ID(int p_ID) {
		P_ID = p_ID;
	}

	private int P_ID;
	public String getGoodsnameID() {
		return GoodsnameID;
	}

	public void setGoodsnameID(String goodsnameID) {
		GoodsnameID = goodsnameID;
	}

	public String getGoodsName() {
		return GoodsName;
	}

	public void setGoodsName(String goodsName) {
		GoodsName = goodsName;
	}

	public String getPackagetype() {
		return Packagetype;
	}

	public void setPackagetype(String packagetype) {
		Packagetype = packagetype;
	}

	public String getPackagetypeID() {
		return PackagetypeID;
	}

	public void setPackagetypeID(String packagetypeID) {
		PackagetypeID = packagetypeID;
	}

	public String getFmodel() {
		return fmodel;
	}

	public void setFmodel(String fmodel) {
		this.fmodel = fmodel;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

}
