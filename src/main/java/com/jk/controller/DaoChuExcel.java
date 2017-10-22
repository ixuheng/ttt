package com.jk.controller;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.jk.dao.MlsGoodsMapper;
import com.jk.model.MlsGoods;
import com.jk.util.ExcelException;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
public class DaoChuExcel<T>{
	private MlsGoodsMapper mlsGoodsMapper;
	private WritableWorkbook workbook;
	//导出excel
	private WritableSheet sheet;
	private String sheetName;
	private int sheetSize; 
	//List<Integer> sheetHead = new ArrayList<Integer>();
	private String[] enFields;
	private String[] cnFields;
	/**
	 * 导出：类的英文属性和Excel中的中文列名的对应关系
	 * 导入：Excel中的中文列头和类的英文属性的对应关系Map
	 */
	LinkedHashMap<String,String> fieldMap;
	//private final int allCount;
	private int sheetNumStart=0;
	//int sheetNumEnd;
	private int firstIndex;
	private int lastIndex;
	//导入excel
	//wwwwwwwww
	private Class<T> entityClass;
	private String[] uniqueFields;
	private InputStream in;
	   
	/**    
	 * <pre>创建一个导入实例 DaoChuExcel.    
	 *    
	 * @param mlsGoodsMapper
	 * @param in
	 * @param entityClass
	 * @param uniqueFields</pre>    
	 * @throws ExcelException 
	 * @throws IOException 
	 * @throws BiffException 
	 */
	public DaoChuExcel(InputStream in, MlsGoodsMapper mlsGoodsMapper, Class<T> entityClass,
			String[] uniqueFields) throws BiffException, IOException, ExcelException {
		super();
		this.in=in;
		this.mlsGoodsMapper = mlsGoodsMapper;
		this.entityClass = entityClass;
		this.uniqueFields = uniqueFields;
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
	public DaoChuExcel(LinkedHashMap<String,String> fieldMap,int allCount,int sheetSize,WritableWorkbook workbook,String sheetName,MlsGoodsMapper mlsGoodsMapper) {
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
	public void initexcel() throws BiffException, IOException, ExcelException {
		//ssssssssssss
		//根据Excel数据源创建WorkBook
        Workbook wb=Workbook.getWorkbook(in);
        //获取所有工作表
        Sheet[] sheets = wb.getSheets();
        //获取工作表的有效行数
        int realRows=0;
        String[] excelFieldNames={};
		LinkedHashMap<String, Integer> colMap=new LinkedHashMap<String, Integer>();
		//1.获取uniqueFields指定的列
        Cell[][] uniqueCells=new Cell[uniqueFields.length][];
		for (int i = 0; i < sheets.length; i++) {
        	//getRealRows(realRows,sheets[i]);
			checkExcelSheets(realRows,i,sheets[i],excelFieldNames,colMap,uniqueCells);
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
	
	//检查所有工作表
	public void checkExcelSheets(int realRows,int sheetIndex,Sheet sheet,String[] excelFieldNames,LinkedHashMap<String, Integer> colMap,Cell[][] uniqueCells) throws ExcelException {
		Cell[] firstRow=sheet.getRow(0);
		String[] newExcelFieldNames=new String[firstRow.length];
		for(int i=0;i<sheet.getRows();i++){
            int nullCols=0;
            for(int j=0;j<sheet.getColumns();j++){
                Cell currentCell=sheet.getCell(j,i);
                if(currentCell==null || "".equals(currentCell.getContents().toString())){
                    nullCols++;
                }
            }

            if(nullCols==sheet.getColumns()){
                break;
            }else{
                realRows++;
            }
        }
		//如果Excel中没有数据则提示错误
        if(realRows<=1){
        	throw new ExcelException("Excel文件中没有任何数据");
        }
		//获取Excel中的列名
		if (sheetIndex==0) {
			for(int i=0;i<firstRow.length;i++){
				newExcelFieldNames[i]=firstRow[i].getContents().toString().trim();
				excelFieldNames=newExcelFieldNames;
			}
			//将列名和列号放入Map中,这样通过列名就可以拿到列号
			for(int i=0;i<excelFieldNames.length;i++){
				colMap.put(excelFieldNames[i], firstRow[i].getColumn());
			}
			for(int i=0;i<uniqueFields.length;i++){
                int col=colMap.get(uniqueFields[i]);
                uniqueCells[i]=sheet.getColumn(col);
            }
			//2.从指定列中寻找重复行
            for(int i=1;i<realRows;i++){
                int nullCols=0;
                for(int j=0;j<uniqueFields.length;j++){
                    String currentContent=uniqueCells[j][i].getContents();
                    Cell sameCell=sheet.findCell(currentContent, 
                            uniqueCells[j][i].getColumn(),
                            uniqueCells[j][i].getRow()+1, 
                            uniqueCells[j][i].getColumn(), 
                            uniqueCells[j][realRows-1].getRow(), 
                            true);
                    if(sameCell!=null){
                        nullCols++;
                    }
                }

                if(nullCols==uniqueFields.length){
                    throw new ExcelException("Excel中有重复行，请检查");
                }
            }
		}else{
			for(int i=0;i<firstRow.length;i++){
				String excelFieldName=firstRow[i].getContents().toString().trim();
				boolean b = Arrays.asList(newExcelFieldNames).contains(excelFieldName);
				if (!b) {
					throw new ExcelException("列名不一致，请检查列名");
				}
			}
			
		}
	}
	public void getRealRows(int realRows,Sheet sheet) throws ExcelException {
		for(int i=0;i<sheet.getRows();i++){
            int nullCols=0;
            for(int j=0;j<sheet.getColumns();j++){
                Cell currentCell=sheet.getCell(j,i);
                if(currentCell==null || "".equals(currentCell.getContents().toString())){
                    nullCols++;
                }
            }

            if(nullCols==sheet.getColumns()){
                break;
            }else{
                realRows++;
            }
        }
		//如果Excel中没有数据则提示错误
        if(realRows<=1){
        	throw new ExcelException("Excel文件中没有任何数据");
        }
	}
	/** <pre>addData(导入)   
	 * 创建人： 徐 恒  492723592@qq.com
	 * 创建时间：2017年9月10日 上午5:41:54    
	 * 修改人： 徐 恒  492723592@qq.com    
	 * 修改时间：2017年9月10日 上午5:41:54    
	 * 修改备注： </pre>    
	 */
	public void addData() {
		
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
		Map<String, Object> map = new HashMap<String, Object>();;
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
     * @MethodName  : setColumnAutoSize
     * @Description : 设置工作表自动列宽和首行加粗
     * @param ws
     */
    private void setColumnAutoSize(WritableSheet ws,int extraWith){
        //获取本列的最宽单元格的宽度
        for(int i=0;i<ws.getColumns();i++){
            int colWith=0;
            for(int j=0;j<ws.getRows();j++){
                String content=ws.getCell(i,j).getContents().toString();
                int cellWith=content.length();
                if(colWith<cellWith){
                    colWith=cellWith;
                }
            }
            //设置单元格的宽度为最宽宽度+额外宽度
            ws.setColumnView(i, colWith+extraWith);
        }

    }

    /**
     * @MethodName  : fillSheet
     * @Description : 向工作表中填充数据
     * @param sheet     工作表 
     * @param list  数据源
     * @param fieldMap 中英文字段对应关系的Map
     * @param cnFields    中文索引
     * @param enFields 英文索引
     */
    private <T> void fillSheet(
    		String[] enFields,
    		String[] cnFields,
            WritableSheet sheet,
            List<T> list,
            LinkedHashMap<String,String> fieldMap
            )throws Exception{
        
        //填充内容
    	for (int j = 0; j < list.size(); j++) {
    		//setSheetLastCellInfo(list,j);
    		Object[] sheetAndIndex = getSheetAndIndex();
    		//获取单个对象
    		T item=list.get(j);
    		WritableSheet threadSheet = (WritableSheet) sheetAndIndex[0];
    		int rowNo=(Integer) sheetAndIndex[1];
    		for(int i=0;i<enFields.length;i++){
    			Object objValue=getFieldValueByNameSequence(enFields[i], item);
    			String fieldValue=objValue==null ? "" : objValue.toString();
    			Label label =new Label(i,rowNo,fieldValue);
    			//threadSheet.addCell(label);
    			name(threadSheet,label);
    		}
		}
        //setColumnAutoSize(sheet, 5);
    }
    public synchronized void name(WritableSheet threadSheet,Label label) {
    	try {
			threadSheet.addCell(label);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
    public synchronized void setSheetLastCellInfo(List<?> list,int j) throws Exception {
    	if (firstIndex>lastIndex) {
			createNewSheet();
		}
    	//获取单个对象
		Object item=list.get(j);
		int rowNo=setRow();
		setCellInfo(rowNo,item);
	}
    public synchronized Object[] getSheetAndIndex() {
    	if (firstIndex>lastIndex) {
			createNewSheet();
		}
    	Object[] sheetAndIndex={this.sheet,this.firstIndex};    	
    	this.firstIndex++;
    	return sheetAndIndex;
	}
    public void setCellInfo(int rowNo,Object item) throws Exception {
    	for(int i=0;i<enFields.length;i++){
			Object objValue=getFieldValueByNameSequence(enFields[i], item);
			String fieldValue=objValue==null ? "" : objValue.toString();
			Label label =new Label(i,rowNo,fieldValue);
			WritableSheet threadSheet = this.sheet;
			threadSheet.addCell(label);
		}
	}
   
    public  int setRow() {
    	//WritableSheet threadSheet = this.sheet;
    	int rowNo=this.firstIndex;
    	this.firstIndex++;
    	return rowNo;
	}
    
    public void createNewSheet() {
    	if (this.sheetNumStart==0) {
    		this.sheet=workbook.createSheet(sheetName, sheetNumStart);
    	}else{
    		setColumnAutoSize(sheet, 5);
    		this.sheet=workbook.createSheet(sheetName+"("+(sheetNumStart)+")", sheetNumStart);
    	}
    	this.firstIndex=0;
    	this.lastIndex=sheetSize;
    	this.sheetNumStart++;
    	setSheetInfo();
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
    	for(int i=0;i<cnFields.length;i++){
    		Label label=new Label(i,this.firstIndex,cnFields[i]);
    		try {
    			sheet.addCell(label);
    		} catch (RowsExceededException e) {
    			e.printStackTrace();
    		} catch (WriteException e) {
    			e.printStackTrace();
    		}
    	}
    	this.firstIndex++;
	}
	
}
