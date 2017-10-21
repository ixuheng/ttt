package com.jk.util.aop;

import org.springframework.util.Assert;

public class DataSourceSwitcher {
	 @SuppressWarnings("rawtypes")  
	    private static final ThreadLocal contextHolder = new ThreadLocal();  
	  	//ThreadLocal创建线程    在线程里不断的开启主从库进行操作	
	    @SuppressWarnings("unchecked")  
	    public static void setDataSource(String dataSource) {  
	        Assert.notNull(dataSource, "dataSource cannot be null");//断言  是否相等（相当于if判断）
	        contextHolder.set(dataSource);  
	    }  
	  
	    public static void setMaster(){  
	        clearDataSource();  
	    }  
	      
	    public static void setSlave() {  
	        setDataSource("slave");  
	    }  
	      
	    public static String getDataSource() { 
	    	//从线程里获取信息  （就是走那个库的信息）
	        return (String) contextHolder.get();  
	    }  
	  
	    public static void clearDataSource() {  
	        //线程移除
	    	contextHolder.remove();  
	    }  
}