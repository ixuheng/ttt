package com.jk.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jk.model.Json;
import com.jk.model.MlsGoods;
import com.jk.service.MlsGoodsService;
import com.jk.util.BaseController;
import com.jk.util.ExcelUtil;
import com.jk.util.Page;
@RestController
public class MlsGoodsController extends BaseController{
	@Autowired
	private MlsGoodsService mlsGoodsService;

	/*@RequestMapping("/userJsonPList")
	public void userJsonPList(HttpServletRequest request, String callback, HttpServletResponse response) {
		String result =  "{\"ret\":true}";
		//加上返回参数
		result=callback+"("+result+")";
		super.outString(result, response);
	}*/


	public static void main(String[] args) {
	}
	/**
	 * 查询所有
	 */
	@RequestMapping("/queryAll")
	public void queryall(HttpServletRequest request, String callback, HttpServletResponse response){
		List<MlsGoods> list=null;


		try {
			list = mlsGoodsService.queryAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
        String jsonString = JSON.toJSONString(list);
        //加上返回参数
        String result=callback+"("+jsonString+")";
        super.outString(result, response);
	}
	/** <pre>getAll(获取商品信息)
	 * 创建人： 徐 恒  492723592@qq.com
	 * 创建时间：2017年9月6日 下午3:10:13    
	 * 修改人： 徐 恒  492723592@qq.com    
	 * 修改时间：2017年9月6日 下午3:10:13    
	 * 修改备注： 
	 * @param mlsGoods
	 * @param request
	 * @param response</pre>    
	 */
	@RequestMapping("/getAll")
	public void getAll(MlsGoods mlsGoods,Page page,HttpServletRequest request,HttpServletResponse response) {
		List<MlsGoods> list = mlsGoodsService.query(mlsGoods,page);
		for (MlsGoods mlsGoods2 : list) {
			System.out.println(mlsGoods2);
		}
		System.err.println("---------------------------------------------");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("page", page);
		map.put("data", list);
		super.writeJson(map, response);
	}
	@RequestMapping("/addGood")
	public void addGood(HttpServletRequest request,HttpServletResponse response) {
		List<MlsGoods> list = new ArrayList<MlsGoods>();
		mlsGoodsService.addGoods(list);
	}
	@RequestMapping("/getAllGood")
	public void getAllGood(HttpServletRequest request,HttpServletResponse response) {
		mlsGoodsService.getAllGood(response);
	}
	/** <pre>uploadExcel(这里用一句话描述这个方法的作用)   
	 * 创建人： 徐 恒  492723592@qq.com
	 * 创建时间：2017年9月9日 下午11:35:12    
	 * 修改人： 徐 恒  492723592@qq.com    
	 * 修改时间：2017年9月9日 下午11:35:12    
	 * 修改备注： 
	 * @param asd
	 * @param request
	 *
	 * @param response
	 * 我今天的任务
	 * @return</pre>    
	 */
	@RequestMapping("/sendAllGood")
	public void uploadExcel(@RequestParam("myfile")MultipartFile asd,HttpServletRequest request,HttpServletResponse response) {
		Json json = new Json();
		if (asd==null) {
			json.setMsg("请选择文件");
			super.writeJson(json, response);
		}
		try {
			Json json2 = super.uploadify(asd, "/image",request, response);
			String type = json2.getMsg().substring(json2.getMsg().lastIndexOf("."));
			if (!".xls".equals(type)) {
				json.setMsg("请选择xls文件");
				super.writeJson(json, response);
			}
			mlsGoodsService.addexcel(request,json2.getMsg());
			json.setSuccess(true);
			super.writeJson(json, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void addGoo(HttpServletRequest request,HttpServletResponse response) {
		int max=14;
        int min=0;
        Random random = new Random();
        //int s2 = random.nextInt(max)%(max-min+1) + min;
        int max1=8;
        int min1=4;
        int max3=4;
        int min3=0;
        int max4=42342;
        int min4=1;
		String[] name = {"衬衫 ","灯笼袖 ","宽松 ","百搭显瘦 ","字母卫衣 ","韩版 ","两件套装 ","复古 ","牛仔半身裙 ","萝卜裤 ","粉嫩 ","针织t恤衫 ","棉服外套 ","沙滩裙 ","毛边 "};
		List<File> list = getFileList("G://img-a");
		MlsGoods mlsGoods =null;
		for (int i = 0; i < 322; i++) {
			int s1 = random.nextInt(max1)%(max1-min1+1) + min1;
			int s3 = random.nextInt(max3)%(max3-min3+1) + min3;
			int s4 = random.nextInt(max4)%(max4-min4+1) + min4;
			String goodname="";
			for (int j = 0; j < s1; j++) {
				int s2 = random.nextInt(max)%(max-min+1) + min;
				goodname+=name[s2];
			}
			mlsGoods = new MlsGoods();
			//mlsGoods.setId((long)i);
			mlsGoods.setGoodimgurl("img-a/"+list.get(i).getName());
			mlsGoods.setGoodname(goodname);
			mlsGoods.setGoodprice((float)(i+1)*100/s1);
			mlsGoods.setGoodsave((long)s4);
			mlsGoods.setGoodstate((long)s3);
			mlsGoodsService.addGoods(mlsGoods);
		}
	}
	/**
	 * <pre>getFileList(获取文件夹下所有文件)   
	 * 创建人： 徐 恒  492723592@qq.com
	 * 创建时间：2017年9月9日 下午11:28:34    
	 * 修改人： 徐 恒  492723592@qq.com    
	 * 修改时间：2017年9月9日 下午11:28:34    
	 * 修改备注： 
	 * @param strPath
	 * @return</pre>
	 */
	public static List<File> getFileList(String strPath) {
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        List<File> filelist = new ArrayList<File>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith("jpg")) { // 判断文件名是否以.avi结尾
                    String strFileName = files[i].getAbsolutePath();
                    System.out.println("---" + strFileName);
                    filelist.add(files[i]);
                } else {
                    continue;
                }
            }

        }
        return filelist;
    }
}
