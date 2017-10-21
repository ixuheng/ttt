package com.jk.service;

import java.io.FileNotFoundException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jk.model.MlsGoods;
import com.jk.util.Page;
public interface MlsGoodsService {
	void addGoods(MlsGoods goods);
	void addGoods(List<MlsGoods> list);
	List<MlsGoods> query(MlsGoods goods, Page page);
	void getAllGood(HttpServletResponse response);
	void addexcel(HttpServletRequest request, String msg) throws Exception;
	List<MlsGoods> queryAll() throws Exception;
}
