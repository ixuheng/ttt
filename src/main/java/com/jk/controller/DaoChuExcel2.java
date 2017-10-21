package com.jk.controller;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import com.jk.dao.MlsGoodsMapper;
import com.jk.model.MlsGoods;
import com.jk.util.ExcelException;
public class DaoChuExcel2<T>{
	private MlsGoodsMapper mlsGoodsMapper;
	private HSSFWorkbook workbook;
	//导出excel
	private HSSFSheet sheet;
	private String sheetName;
	private int sheetSize; 
	private String[] enFields;
	private String[] cnFields;
	/**
	 * 导出：类的英文属性和Excel中的中文列名的对应关系
	 * 导入：Excel中的中文列头和类的英文属性的对应关系Map
	 */
	LinkedHashMap<String,String> fieldMap;
	private int sheetNumStart=0;
	private int sheetNumEnd;
	private int firstIndex;
	private int lastIndex;
	//导入excel
	private Class<T> entityClass;
	private String[] uniqueFields;
	private InputStream in;
	private String[] excelFieldNames={};
	private int realRows;
	private LinkedHashMap<String, Integer> colMap=new LinkedHashMap<String, Integer>();
	/**    
	 * <pre>创建一个导入实例 DaoChuExcel.    
	 *    
	 * @param mlsGoodsMapper
	 * @param entityClass
	 * @param uniqueFields</pre>    
	 * @throws ExcelException 
	 * @throws IOException 
	 * @throws BiffException 
	 */
	public DaoChuExcel2(LinkedHashMap<String,String> fieldMap,InputStream in, MlsGoodsMapper mlsGoodsMapper, Class<T> entityClass,
			String[] uniqueFields) throws Exception {
		super();
		this.fieldMap=fieldMap;
		this.in=in;
		this.mlsGoodsMapper = mlsGoodsMapper;
		this.entityClass = entityClass;
		this.uniqueFields = uniqueFields;
		this.firstIndex=1;
		initexcel();
	}
	/**    
	 * <pre>创建一个导出实例 DaoChuExcel.    
	 *    
	 * @param fieldMap
	 * @param allCount
	 * @param sheetSize
	 * @param workbook
	 * @param sheetName
	 * @param mlsGoodsMapper</pre>    
	 */
	public DaoChuExcel2(LinkedHashMap<String,String> fieldMap,int allCount,int sheetSize,HSSFWorkbook workbook,String sheetName,MlsGoodsMapper mlsGoodsMapper) {
		super();
		this.fieldMap=fieldMap;
		this.workbook=workbook;
		this.sheetName=sheetName;
		this.sheetSize=sheetSize;
		this.mlsGoodsMapper=mlsGoodsMapper;
		if(sheetSize>65535 || sheetSize<1){
            //sheetSize=65535;
			this.sheetSize=65535;
        }
		//因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
        //所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
        //初始化enFields、cnFields
		initenFieldsAndcnFields();
		//初始化一个sheet表
        createNewSheet();
	}
	public void initexcel() throws Exception {
		//根据Excel数据源创建WorkBook
		workbook=new HSSFWorkbook(in);
        //获取所有工作表
		this.sheetNumEnd=workbook.getNumberOfSheets();
		this.lastIndex = workbook.getSheetAt(0).getLastRowNum();
		//1.获取uniqueFields指定的列
        Cell[][] uniqueCells=new Cell[uniqueFields.length][];
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			checkExcelSheets(i,workbook.getSheetAt(i),uniqueCells);
		}
        //判断需要的字段在Excel中是否都存在
        boolean isExist=true;
        List<String> excelFieldList=Arrays.asList(excelFieldNames);
        for(String cnName : fieldMap.keySet()){
            if(!excelFieldList.contains(cnName)){
                isExist=false;
                break;
            }
        }
        //如果有列名不存在，则抛出异常，提示错误
        if(!isExist){
            throw new ExcelException("Excel中缺少必要的字段，或字段名称有误");
        }
	}
	
	
	
	/** <pre>addData(导入)   
	 * 创建人： 徐 恒  492723592@qq.com
	 * 创建时间：2017年9月10日 上午5:41:54    
	 * 修改人： 徐 恒  492723592@qq.com    
	 * 修改时间：2017年9月10日 上午5:41:54    
	 * 修改备注： </pre>    
	 */
	public int getAllData() {
		return this.realRows;
	}
	public void addData(int threadindex,int threadsize) throws Exception {
		System.out.println("线程"+Thread.currentThread().getName()+"准备读取数据------------------");
		//将sheet转换为list
		List<T> list = new ArrayList<T>();
        for(int i=0;i<threadsize;i++){
        	//确定线程读取哪一行数据
        	int[] rowData = getRowIndex();
        	if (rowData[1]==0) {
				return;
			}
        	int sheetIndex = rowData[0];
        	int rowIndex = rowData[1];
            //新建要转换的对象
            T entity=entityClass.newInstance();
            //给对象中的字段赋值
            for(Entry<String, String> entry : fieldMap.entrySet()){
                //获取中文字段名
                String cnNormalName=entry.getKey();
                //获取英文字段名
                String enNormalName=entry.getValue();
                //根据中文字段名获取列号
                int col=colMap.get(cnNormalName);
                //获取当前单元格中的内容
                String content=workbook.getSheetAt(sheetIndex).getRow(rowIndex).getCell(col).getStringCellValue().trim();
                //给对象赋值
                setFieldValueByName(enNormalName, content, entity);
            }
            list.add(entity);
        }
        mlsGoodsMapper.batchAddCompany(list);
        System.err.println("线程"+Thread.currentThread().getName()+"开始导入数据库-----------共"+list.size()+"条数据");
	}
	private synchronized int[] getRowIndex() {
		int[] sheetAndRow = {0,0};
		if (this.firstIndex>this.lastIndex) {
			if (this.sheetNumStart==this.sheetNumEnd-1) {
				return sheetAndRow;
			}
			HSSFSheet sheetAt = workbook.getSheetAt(this.sheetNumStart+1);
			this.firstIndex=1;
			sheetAndRow[0]=this.sheetNumStart;
			sheetAndRow[1]=this.firstIndex;
			this.lastIndex=sheetAt.getLastRowNum();
			this.sheetNumStart++;
		}else{
			sheetAndRow[0]=this.sheetNumStart;
			sheetAndRow[1]=this.firstIndex;
			this.firstIndex++;
		}
		return sheetAndRow;
	}
	/** <pre>getPage(导出)   
	 * 创建人： 徐 恒  492723592@qq.com
	 * 创建时间：2017年9月10日 上午5:42:09    
	 * 修改人： 徐 恒  492723592@qq.com    
	 * 修改时间：2017年9月10日 上午5:42:09    
	 * 修改备注： 
	 * @param threadIndex
	 * @param count</pre>
	 */
	public void getPage(int threadIndex,int count) {
		System.out.println("线程"+Thread.currentThread().getName()+"开始执行。。。。。。。。。。。。。");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start", threadIndex*count);
		map.put("offset", count);
		List<MlsGoods> list;
		try {
			list= mlsGoodsMapper.selectPage(map);
			loadFillSheet(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void loadFillSheet(List<MlsGoods> list) {
		try {
				fillSheet(enFields,cnFields,sheet, list, fieldMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*<-------------------------辅助的私有方法----------------------------------------------->*/
    /**
     * @MethodName  : getFieldValueByName
     * @Description : 根据字段名获取字段值
     * @param fieldName 字段名
     * @param o 对象
     * @return  字段值
     */
    private static  Object getFieldValueByName(String fieldName, Object o) throws Exception{

        Object value=null;
        Field field=getFieldByName(fieldName, o.getClass());

        if(field !=null){
            field.setAccessible(true);
            value=field.get(o);
        }else{
            throw new ExcelException(o.getClass().getSimpleName() + "类不存在字段名 "+fieldName);
        }

        return value;
    }

    /**
     * @MethodName  : getFieldByName
     * @Description : 根据字段名获取字段
     * @param fieldName 字段名
     * @param clazz 包含该字段的类
     * @return 字段
     */
    private static Field getFieldByName(String fieldName, Class<?>  clazz){
        //拿到本类的所有字段
        Field[] selfFields=clazz.getDeclaredFields();

        //如果本类中存在该字段，则返回
        for(Field field : selfFields){
            if(field.getName().equals(fieldName)){
                return field;
            }
        }
        //否则，查看父类中是否存在此字段，如果有则返回
        Class<?> superClazz=clazz.getSuperclass();
        if(superClazz!=null  &&  superClazz !=Object.class){
            return getFieldByName(fieldName, superClazz);
        }

        //如果本类和父类都没有，则返回空
        return null;
    }

    /**
     * @MethodName  : getFieldValueByNameSequence
     * @Description : 
     * 根据带路径或不带路径的属性名获取属性值
     * 即接受简单属性名，如userName等，又接受带路径的属性名，如student.department.name等
     * 
     * @param fieldNameSequence  带路径的属性名或简单属性名
     * @param o 对象
     * @return  属性值
     * @throws Exception
     */
    private static  Object getFieldValueByNameSequence(String fieldNameSequence, Object o) throws Exception{

        Object value=null;

        //将fieldNameSequence进行拆分
        String[] attributes=fieldNameSequence.split("\\.");
        if(attributes.length==1){
            value=getFieldValueByName(fieldNameSequence, o);
        }else{
            //根据属性名获取属性对象
            Object fieldObj=getFieldValueByName(attributes[0], o);
            String subFieldNameSequence=fieldNameSequence.substring(fieldNameSequence.indexOf(".")+1);
            value=getFieldValueByNameSequence(subFieldNameSequence, fieldObj);
        }
        return value; 

    } 


    /**
     * @MethodName  : setFieldValueByName
     * @Description : 根据字段名给对象的字段赋值
     * @param fieldName  字段名
     * @param fieldValue    字段值
     * @param o 对象
     */
    private void setFieldValueByName(String fieldName,Object fieldValue,Object o) throws Exception{

            Field field=getFieldByName(fieldName, o.getClass());
            if(field!=null){
                field.setAccessible(true);
                //获取字段类型
                Class<?> fieldType = field.getType();  

                //根据字段类型给字段赋值
                if (String.class == fieldType) {  
                    field.set(o, String.valueOf(fieldValue));  
                } else if ((Integer.TYPE == fieldType)  
                        || (Integer.class == fieldType)) {  
                    field.set(o, Integer.parseInt(fieldValue.toString()));  
                } else if ((Long.TYPE == fieldType)  
                        || (Long.class == fieldType)) {  
                    field.set(o, Long.valueOf(fieldValue.toString()));  
                } else if ((Float.TYPE == fieldType)  
                        || (Float.class == fieldType)) {  
                    field.set(o, Float.valueOf(fieldValue.toString()));  
                } else if ((Short.TYPE == fieldType)  
                        || (Short.class == fieldType)) {  
                    field.set(o, Short.valueOf(fieldValue.toString()));  
                } else if ((Double.TYPE == fieldType)  
                        || (Double.class == fieldType)) {  
                    field.set(o, Double.valueOf(fieldValue.toString()));  
                } else if (Character.TYPE == fieldType) {  
                    if ((fieldValue!= null) && (fieldValue.toString().length() > 0)) {  
                        field.set(o, Character  
                                .valueOf(fieldValue.toString().charAt(0)));  
                    }  
                }else if(Date.class==fieldType){
                    field.set(o, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fieldValue.toString()));
                }else if(BigDecimal.class==fieldType){
                	field.set(o,new BigDecimal(fieldValue.toString()));
                }else{
                    field.set(o, fieldValue);
                }
            }else{
                throw new ExcelException(o.getClass().getSimpleName() + "类不存在字段名 "+fieldName);
            }
    }

    /**
     * @MethodName  : fillSheet
     * @Description : 向工作表中填充数据
     * @param sheet2     工作表 
     * @param list  数据源
     * @param fieldMap 中英文字段对应关系的Map
     * @param cnFields    中文索引
     * @param enFields 英文索引
     */
    private <T> void fillSheet(
    		String[] enFields,
    		String[] cnFields,
            HSSFSheet sheet2,
            List<MlsGoods> list,
            LinkedHashMap<String,String> fieldMap
            )throws Exception{
        
        //填充内容
    	for (int j = 0; j < list.size(); j++) {
    		setSheetLastCellInfo(list,j);
		}
        //setColumnAutoSize(sheet, 5);
    }
    public void setCellValue(HSSFRow threadRow,int i,String fieldValue) {
    	threadRow.createCell(i).setCellValue(fieldValue);
	}
    public synchronized void setSheetLastCellInfo(List<MlsGoods> list,int j) throws Exception {
    	Object[] sheetAndIndex = getSheetAndIndex();
		//获取单个对象
    	Object item=list.get(j);
		HSSFSheet threadSheet = (HSSFSheet) sheetAndIndex[0];
		int rowNo=(Integer) sheetAndIndex[1];
		HSSFRow threadRow = threadSheet.createRow(rowNo);
		for(int i=0;i<enFields.length;i++){
			Object objValue=getFieldValueByNameSequence(enFields[i], item);
			String fieldValue=objValue==null ? "" : objValue.toString();
			setCellValue(threadRow,i,fieldValue);
		}
	}
    public synchronized Object[] getSheetAndIndex() {
    	if (firstIndex>lastIndex) {
			createNewSheet();
		}
    	Object[] sheetAndIndex={this.sheet,this.firstIndex};    	
    	this.firstIndex++;
    	return sheetAndIndex;
	}
   
    public  int setRow() {
    	int rowNo=this.firstIndex;
    	this.firstIndex++;
    	return rowNo;
	}
    
    public void createNewSheet() {
    	if (this.sheetNumStart==0) {
    		this.sheet=workbook.createSheet(sheetName);
    	}else{
    		//setColumnAutoSize(sheet, 5);
    		autosetcolumnwidth();
    		this.sheet=workbook.createSheet(sheetName+"("+(sheetNumStart)+")");
    	}
    	this.firstIndex=0;
    	this.lastIndex=sheetSize;
    	this.sheetNumStart++;
    	setSheetInfo();
    }
    public void autosetcolumnwidth() {
    	for (int j = 0; j < cnFields.length; j++) {
    		this.sheet.autoSizeColumn(j);
		}
	}
    public void initenFieldsAndcnFields() {
		//定义存放英文字段名和中文字段名的数组
        this.enFields=new String[fieldMap.size()];
        this.cnFields=new String[fieldMap.size()];

        //填充数组
        int count=0;
        for(Entry<String,String> entry:fieldMap.entrySet()){
            enFields[count]=entry.getKey();
            cnFields[count]=entry.getValue();
            count++;
        }
	}
	//填充表头
	public void setSheetInfo() {
		HSSFRow createRow = sheet.createRow(firstIndex);
    	for(int i=0;i<cnFields.length;i++){
    		createRow.createCell(i).setCellValue(cnFields[i]);
    	}
    	this.firstIndex++;
	}
	
	//检查所有工作表
	public void checkExcelSheets(int sheetIndex,HSSFSheet sheet,Cell[][] uniqueCells) throws ExcelException {
		HSSFRow firstRow=sheet.getRow(0);
		int numberOfCells = firstRow.getPhysicalNumberOfCells();
		String[] newExcelFieldNames=new String[numberOfCells];
		for(int i=0;i<sheet.getLastRowNum();i++){
            int nullCols=0;
            for(int j=0;j<numberOfCells;j++){
                HSSFCell currentCell = sheet.getRow(i).getCell(j);
                if(currentCell==null || "".equals(currentCell.getStringCellValue())){
                    nullCols++;
                }
            }

            if(nullCols==numberOfCells){
                break;
            }else{
                this.realRows++;
            }
        }
		//如果Excel中没有数据则提示错误
        if(realRows<=1){
        	throw new ExcelException("Excel文件中没有任何数据");
        }
		//获取Excel中的列名
		if (sheetIndex==0) {
			for(int i=0;i<numberOfCells;i++){
				newExcelFieldNames[i]=firstRow.getCell(i).getStringCellValue();
				this.excelFieldNames=newExcelFieldNames;
			}
			//将列名和列号放入Map中,这样通过列名就可以拿到列号
			for(int i=0;i<this.excelFieldNames.length;i++){
				this.colMap.put(this.excelFieldNames[i], firstRow.getCell(i).getColumnIndex());
			}
			for(int i=0;i<uniqueFields.length;i++){
                int col=colMap.get(uniqueFields[i]);
                Cell[] allcellvalue=new Cell[sheet.getLastRowNum()];
                for (int j = 1; j <sheet.getLastRowNum() ; j++) {
                	Cell cellvalue = sheet.getRow(j).getCell(col);
					allcellvalue[j]=cellvalue;
				}
                uniqueCells[i]=allcellvalue;
            }
			//2.从指定列中寻找重复行
            for(int i=1;i<realRows;i++){
                int nullCols=0;
                for(int j=0;j<uniqueFields.length;j++){
                    Cell[] allcell=new Cell[sheet.getLastRowNum()];
                    for (int k = i; k < realRows; k++) {
                    	HSSFCell cell = sheet.getRow(k).getCell(j);
                    	allcell[k]=cell;
					}
                    //把最后一个元素替代指定的元素
                    allcell[i-1] = allcell[allcell.length-1];
                    //数组缩容
                    allcell = Arrays.copyOf(allcell, allcell.length-1);
                    boolean b = Arrays.asList(allcell).contains(uniqueCells[j][i]);
                    if(!b){
                        nullCols++;
                    }
                }

                if(nullCols==uniqueFields.length){
                    throw new ExcelException("Excel中有重复行，请检查");
                }
            }
		}else{
			for(int i=0;i<numberOfCells;i++){
				String excelFieldName=firstRow.getCell(i).getStringCellValue();
				boolean b = Arrays.asList(this.excelFieldNames).contains(excelFieldName);
				if (!b) {
					throw new ExcelException("列名不一致，请检查列名");
				}
			}
			
		}
	}
	
}
