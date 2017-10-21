package com.jk.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

import com.alibaba.fastjson.JSON;
import com.jk.model.Json;


public class BaseController {
	
	 public Json uploadify(MultipartFile fileName,String folderPath ,HttpServletRequest request, HttpServletResponse response) throws Exception{
	        ServletContext sc = request.getSession().getServletContext();
	        String dir = sc.getRealPath(folderPath);//附件存放服务器的路径
	        File file = new File(dir);
	        if(!file.exists()){
	            file.mkdirs();
	        }
	        //源文件全名
	        String filename = fileName.getOriginalFilename();
	        //文件类型
	        //String fileType = filename.substring(filename.lastIndexOf("."));
	        //唯一的文件名UUID+原文件名
	        String newFileName=UUID.randomUUID().toString()+"_"+filename;
	        //绝对路径
	        //String absolutePath = request.getContextPath()+""+dir+"/"+newFileName;
	        //相对路径
	        String relativePath =folderPath+"/"+newFileName;
	        Json json = new Json();
	        try {
	            FileUtils.writeByteArrayToFile(new File(dir, newFileName), fileName.getBytes());
	            json.setSuccess(true);
	            json.setMsg(relativePath);
	            return json;
	        } catch (Exception e) {
	            e.printStackTrace();
	            json.setSuccess(false);
	            json.setMsg("上传失败");
	            return json;
	        }
	    }

	public void writeJson(Object object, HttpServletResponse response){
		try {
			String json = JSON.toJSONStringWithDateFormat(object, "yyyy-MM-dd HH:mm:ss");
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write(json);
			response.getWriter().flush();
			response.getWriter().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * <pre>outString(打印字符串) jsonp讲解时，使用response.setContentType("application/json");
	 * 创建人：杨志超 yangzhichao150@126.com    
	 * 创建时间：2016年3月14日 上午10:07:28    
	 * 修改人：杨志超 yangzhichao150@126.com     
	 * 修改时间：2016年3月14日 上午10:07:28    
	 * 修改备注： 
	 * @param json</pre>
	 */
	protected void outString(String json, HttpServletResponse response) {
		PrintWriter writer = null;
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		try {
			writer = response.getWriter();
			writer.write(json);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.close();
				writer = null;
			}
		}
	}
	
	protected void writeJson2(Object obj, HttpServletResponse response){
		JsonConfig jsonConfig = new JsonConfig();
//        jsonConfig.setExcludes(new String[]{"memberRank","hibernateLazyInitializer"});      
        String jsonStr = JSONSerializer.toJSON(obj, jsonConfig).toString();
        System.out.println(jsonStr);
        ajaxOut(jsonStr, response);
	}
	
	protected void ajaxOut(String jsonStr, HttpServletResponse response){
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().write(jsonStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
