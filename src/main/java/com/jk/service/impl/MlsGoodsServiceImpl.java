package com.jk.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.T;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jk.controller.DaoChuExcel;
import com.jk.controller.DaoChuExcel2;
import com.jk.controller.GoodList;
import com.jk.dao.MlsGoodsMapper;
import com.jk.model.MlsGoods;
import com.jk.service.MlsGoodsService;
import com.jk.util.Page;

import jxl.Workbook;
import jxl.write.WritableWorkbook;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class MlsGoodsServiceImpl implements MlsGoodsService {
	@Autowired
	private MlsGoodsMapper mlsGoodsMapper;
	
	public HttpSolrServer init() {
		String url = "http://localhost:8080/solr/mycore";
        HttpSolrServer core = new HttpSolrServer(url);
        core.setMaxRetries(1);
        core.setConnectionTimeout(50000);
        core.setParser(new XMLResponseParser()); // binary parser is used by default
        core.setSoTimeout(10000); // socket read timeout
        core.setDefaultMaxConnectionsPerHost(100);
        core.setMaxTotalConnections(100);
        core.setFollowRedirects(false); // defaults to false
        core.setAllowCompression(true);
        return core;
	}
	
	@Override
	public void addGoods(MlsGoods goods) {
		HttpSolrServer core = init();
		int i = 0;
		try {
			i = mlsGoodsMapper.insertSelective(goods);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("成功新增"+i+"条");
	}

	@Override
	public List<MlsGoods> query(MlsGoods goods, Page page) {
		HttpSolrServer core = init();
		List<MlsGoods> list = null;
		try {
			core.deleteByQuery("*:*");
			list = mlsGoodsMapper.selectByPrimaryAll();
			for (MlsGoods mlsGoods : list) {
					core.addBean(mlsGoods);
			}
			core.commit();
			SolrQuery query = new SolrQuery();
			if (goods.getGoodname()==null||"".equals(goods.getGoodname())) {
				query.setQuery("title:*");
				query.setQuery("subject:*");
			}else{
				query.setQuery("title:"+goods.getGoodname());
				query.setQuery("subject:"+goods.getGoodname());
			}
			int start=(page.getThisPage()-1)*page.getThisPageCount();
			int rows=page.getThisPageCount();
			query.setStart(start);    // query的开始行数(分页使用)
	        query.setRows(rows); 
	        query.addSort(new SortClause("id", ORDER.asc));
	        QueryResponse response = core.query(query);
	        list= response.getBeans(MlsGoods.class);
	        SolrDocumentList docs = response.getResults();  
	        int count = (int) docs.getNumFound();
	        page.setAllCount(count);
	        page.setPageAllCount((int)(Math.floor((float)count/rows))+1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}

	@Override
	public void addGoods(List<MlsGoods> list) {
		ExecutorService threadPool = Executors.newFixedThreadPool(200);
		int count=(int)((float)100000/200);
		for (int i = 0; i < count; i++) {
			final GoodList goodList = new GoodList(mlsGoodsMapper,i);
			threadPool.execute(goodList);
		}
				
	}

	@Override
	public void getAllGood(HttpServletResponse response) {
		LinkedHashMap<String, String> fieldMap = new LinkedHashMap<String, String>();
        fieldMap.put("id", "id");
        fieldMap.put("goodname", "名称");
        fieldMap.put("goodinfo", "介绍");
        fieldMap.put("goodprice", "价格");
        fieldMap.put("goodimgurl", "图片链接");
        fieldMap.put("goodsave", "收藏数");
        fieldMap.put("goodstate", "状态");
		try {
			int counts = mlsGoodsMapper.selectAllCount();
			System.err.println(counts);
			final int threadSize=1000;
			int count=(int)((float)counts/threadSize);
			ExecutorService ThreadPool = Executors.newFixedThreadPool(100);
			final CountDownLatch doneSignal = new CountDownLatch(count);
			//设置默认文件名为当前时间：年月日时分秒
	        String fileName=new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString();
	        response.reset();          
	        response.setContentType("application/vnd.ms-excel");        //改成输出excel文件
	        response.setHeader("Content-disposition","attachment; filename="+fileName+".xls" );
			//HSSFWorkbook workbook = new HSSFWorkbook();
			//final DaoChuExcel2<T> daoChuExcel2 = new DaoChuExcel2<T>(fieldMap, counts, 30000, workbook, "qwe", mlsGoodsMapper);
	        WritableWorkbook workbook = Workbook.createWorkbook(response.getOutputStream());
	        final DaoChuExcel<Object> daoChuExcel = new DaoChuExcel<Object>(fieldMap, counts, 30000, workbook, "q", mlsGoodsMapper);
	        for (int i = 0; i < count; i++) {
				final int j=i;
				ThreadPool.execute(new Runnable() {
					public void run() {
						daoChuExcel.getPage(j, threadSize);
						doneSignal.countDown();
					}
				});
			}
			doneSignal.await();
			ThreadPool.shutdown();
			System.out.println("ok 了！！！");
			//workbook.write(response.getOutputStream());  
			//response.getOutputStream().close();
			workbook.write();
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void addexcel(HttpServletRequest request,String msg) throws Exception {
		LinkedHashMap<String, String> fieldMap = new LinkedHashMap<String, String>();
        fieldMap.put("id", "id");
        fieldMap.put("名称","goodname");
        fieldMap.put( "介绍","goodinfo");
        fieldMap.put( "价格","goodprice");
        fieldMap.put( "图片链接","goodimgurl");
        fieldMap.put( "收藏数","goodsave");
        fieldMap.put( "状态","goodstate");
		String[] uniqueFields={"id"};
		String sc = request.getSession().getServletContext().getRealPath(msg);
		File file = new File(sc);
		InputStream in = new FileInputStream(sc);
		final DaoChuExcel2<MlsGoods> chuExcel2 = new DaoChuExcel2<MlsGoods>(fieldMap,in, mlsGoodsMapper, MlsGoods.class, uniqueFields);
		int allData = chuExcel2.getAllData();
		System.err.println(chuExcel2.getAllData());
		final int threadSize=200;
		int count=(int)((float)allData/threadSize);
		ExecutorService ThreadPool = Executors.newFixedThreadPool(100);
		final CountDownLatch doneSignal = new CountDownLatch(count);
		for (int i = 0; i < count; i++) {
			final int j=i;
			ThreadPool.execute(new Runnable() {
				public void run() {
					try {
						chuExcel2.addData(j,threadSize);
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.err.println("线程"+Thread.currentThread().getName()+"导入成功------------------");
					doneSignal.countDown();
				}
			});
		}
		doneSignal.await();
		ThreadPool.shutdown();
		System.out.println("ok 了！！！");
	}

	@Override
	public List<MlsGoods> queryAll() throws Exception {
		List<MlsGoods> list = mlsGoodsMapper.selectByPrimaryAll();
		return list;
	}

}
