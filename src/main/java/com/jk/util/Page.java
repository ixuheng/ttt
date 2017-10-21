package com.jk.util;

import java.io.Serializable;

public class Page implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3343990366846411357L;
	//第几页
	private Integer thisPage=1;
	//每页条数
	private Integer thisPageCount=3;
	//总条数
	private Integer allCount;
	//总页数
	private Integer pageAllCount;
	
	
	public Page() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Integer getThisPage() {
		return thisPage;
	}
	public void setThisPage(Integer thisPage) {
		if (thisPage!=null) {
			this.thisPage = thisPage;
		}
	}
	public Integer getThisPageCount() {
		return thisPageCount;
	}
	public void setThisPageCount(Integer thisPageCount) {
		if (thisPageCount!=null) {
			this.thisPageCount = thisPageCount;
		}
	}
	
	public Integer getPageAllCount() {
		return pageAllCount;
	}
	public void setPageAllCount(Integer pageAllCount) {
		this.pageAllCount = pageAllCount;
	}

	public Integer getAllCount() {
		return allCount;
	}

	public void setAllCount(Integer allCount) {
		this.allCount = allCount;
	}

	@Override
	public String toString() {
		return "Page [thisPage=" + thisPage + ", thisPageCount="
				+ thisPageCount + ", allCount=" + allCount + ", pageAllCount="
				+ pageAllCount + "]";
	}
	
}
