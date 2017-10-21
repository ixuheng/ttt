package com.jk.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.jk.dao.MlsGoodsMapper;
import com.jk.model.MlsGoods;

public class GoodList implements Runnable{
	private MlsGoodsMapper mlsGoodsMapper;
	private List<MlsGoods> list = new ArrayList<MlsGoods>();
	
	public GoodList(MlsGoodsMapper mlsGoodsMapper, int i) {
		super();
		this.mlsGoodsMapper = mlsGoodsMapper;
		this.i = i;
	}
	@Override
	public void run() {
		getList(i);
	}
	public void getList(int i) {
		add(i);//往集合中添加对象
	}
	public void add(int i) {
		for (int j = 0; j < 201; j++) {
			if (getTrue()) {
				try {
					mlsGoodsMapper.batchAddCompany(this.list);
					del();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			int key = i*200;
			this.list.add(getDemo(key+j));
		}
	}
	public void del() {
		this.list.clear();
	}
	public void sys() {
		System.out.println(this.list.size()+"	"+this.list);
	}
	public boolean getTrue() {
		boolean a=this.list.size()%201==200;
		return a;
	}
	public  MlsGoods getDemo(int i) {
		int s1 = random.nextInt(max1)%(max1-min1+1) + min1;
		int s3 = random.nextInt(max3)%(max3-min3+1) + min3;
		int s4 = random.nextInt(max4)%(max4-min4+1) + min4;
		String goodname="";
		for (int j = 0; j < s1; j++) {
			int s2 = random.nextInt(max)%(max-min+1) + min;
			goodname+=name[s2];
		}
		mlsGoods = new MlsGoods();
		mlsGoods.setId((long)i);
		mlsGoods.setGoodimgurl("img-a/"+list3.get(i%322).getName());
		mlsGoods.setGoodname(goodname);
		mlsGoods.setGoodprice((float)(i+1)*100/s1);
		mlsGoods.setGoodsave((long)s4);
		mlsGoods.setGoodstate((long)s3);
		return mlsGoods;
	}
	int i=0;
	int max=14;
    int min=0;
    Random random = new Random();
    int max1=8;
    int min1=4;
    int max3=4;
    int min3=0;
    int max4=42342;
    int min4=1;
	final String[] name = {"衬衫 ","灯笼袖 ","宽松 ","百搭显瘦 ","字母卫衣 ","韩版 ","两件套装 ","复古 ","牛仔半身裙 ","萝卜裤 ","粉嫩 ","针织t恤衫 ","棉服外套 ","沙滩裙 ","毛边 "};
	final List<File> list3 = getFileList("G://img-a");
	MlsGoods mlsGoods =null;
	public List<File> getFileList(String strPath) {
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
