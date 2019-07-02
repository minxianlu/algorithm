package com.kayak.common.untils;

import com.kayak.common.exception.BusinessException;
import com.kayak.common.untils.file.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * @author mxl
 * @title: ExcelUtil
 * @projectName algorithm_test
 * @description: excel工具类
 * @date 2019/6/24 15:29
 */
public class ExcelUtil {

    /*HSSFWorkbook,excel文件后缀*/
    private static final String HSSF_SUFFIX="xls";
    /*XSSFWorkbook,excel文件后缀*/
    private static final String XSSF_SUFFIX="xlsx";


    
     /**
     　*@description: 读取excel内容;读取工作簿中的第一个sheet的全部内容
       *@author: mxl
     　*@params: file:MultipartFile格式文件，不允许为null
     　*@return: List<Map<String,String>> 数据集合;List中的一个map对应excel中一条数据;map:key为第一行(表头);value为对应数据
     　*@date: 2019/6/25 16:45
     　*/
    public static List<Map<String,String>> readExcelContent(MultipartFile file) {
        return readExcelContent(file,null,null,null,null,null);
    }

    /**
     　* @description: 读取excel内容;读取工作簿中指定sheet的全部内容
       * @ author mxl
     　* @params:file:MultipartFile格式文件，不允许为null
                        sheetNo:工作表编号，为null时则默认给1
     　* @return: List<Map<String,String>> 数据集合;List中的一个map对应excel中一条数据;map:key为第一行(表头);value为对应数据
     　* @date 2019/6/25 16:45
     　*/
    public static List<Map<String,String>> readExcelContent(MultipartFile file,Integer sheetNo) {
        return readExcelContent(file,sheetNo,null,null,null,null);
    }

     /**
     　* @description: 读取excel内容,读取指定的一个sheet中的指定内容
       * @ author mxl
     　* @params:   file：文件；
                    sheetNo:工作表的编号,为null时给1,大于等于1小于工作表数目(例：1,2,3,4,...);
                    startLineNo:开始行号,为null时给1,大于等于1小于工作表中数据行数(例：1,2,3,4,...);
                    endLineNo:结束行号,为null时给数据总行数,大于开始行号小于工作表中数据行数;(例：100,101,...);
                    startCellNo:开始列号,为null时给1,大于等于1小于工作表中第一行列数(例：1,2,3,4,...);
                    endCellNo:结束列号,为null时给第一行总列数,大于开始列号小于工作表中第一行列数;(例：100,101,...);
      　* @return: List<Map<String,String>> ; map中以表中第一行为key
     　* @date 2019/6/25 10:36
     　*/
    public static List<Map<String,String>> readExcelContent(MultipartFile file, Integer sheetNo, Integer startLineNo, Integer endLineNo,Integer startCellNo,Integer endCellNo){
        Workbook wk=getWorkbookFromMultipartFile(file);
        List<Map<String,String>>  result=readExcelContent(wk,sheetNo,startLineNo,endLineNo,startCellNo,endCellNo);
        return result;
    }
     /**
     　* @description: 向已存在的Excel中的第一个工作表写入数据,如果表头(第一行)为空，则将datas的第一条数据的key集合写入表头，如果不为空，则从第二行开始写入datas全部数据
       * @ author mxl
     　* @params:file:文件；
                datas:数据集；datas中的Map为对应的数据，key为第一行表头，value为对应数据
     　* @return:
     　* @date 2019/6/26 15:37 
     　*/
    public static void writeDataToExistExcel(MultipartFile file,List<Map<String,String>> datas){
        writeDataToExistExcel(file,datas,null,null,null,null,null);
    }
    /**
     　* @description: 向已存在的Excel中指定工作表写入数据,如果表头(第一行)为空，则将datas的第一条数据的key集合写入表头，如果不为空，则从第二行开始写入datas全部数据
     * @ author mxl
     　* @params:  file:文件；
                    datas:数据集；datas中的Map为对应的数据，key为第一行表头，value为对应数据
                    sheetNo:指定的工作表;sheetNo为Null时默认给1
     　* @return:
     　* @date 2019/6/26 13:43
     　*/
    public static void writeDataToExistExcel(MultipartFile file,List<Map<String,String>> datas,Integer sheetNo){
        writeDataToExistExcel(file,datas,sheetNo,null,null,null,null);
    }

    /**
     　* @description: 向已存在的Excel中写入数据,如果表头(第一行)为空，则将datas的第一条数据的key集合写入表头;
     * @ author mxl
     　* @params:  file:文件；
                    datas:数据集；datas中的Map为对应的数据，key为第一行表头，value为对应数据
                    sheetNo:指定的工作表;sheetNo为Null时默认给1
                    startLineNo:开始写入的行;startLineNo为null时默认给2
                    endLineNo:写入的终止行;endLineNo为null时默认给datas的长度
                    startCellNo:开始写入的单元格;startCellNo为null时默认给1
                    endCellNo:结束写入的单元格;endCellNo为null时默认给表头的长度
     　* @return:
     　* @date 2019/6/26 13:43
     　*/
    public static void writeDataToExistExcel(MultipartFile file,List<Map<String,String>> datas,Integer sheetNo, Integer startLineNo, Integer endLineNo,Integer startCellNo,Integer endCellNo ){
        Workbook wk=getWorkbookFromMultipartFile(file);
        writeDataToExistExcel(wk,datas, sheetNo,  startLineNo,  endLineNo, startCellNo, endCellNo);
    }


     /**
     　* @description: 读取excel文件内容
       * @ author mxl
     　* @params:   wk：工作簿，工作簿中的数据第一行应为数据头部；
                    sheetNo:工作表的编号,为null时给1,大于等于1小于工作表数目(例：1,2,3,4,...);
                    startLineNo:开始行号,为null时给1,大于等于1小于工作表中数据行数(例：1,2,3,4,...);
                    endLineNo:结束行号,为null时给数据总行数,大于开始行号小于工作表中数据行数;(例：100,101,...);
                    startCellNo:开始列号,为null时给1,大于等于1小于工作表中第一行列数(例：1,2,3,4,...);
                    endCellNo:结束列号,为null时给第一行总列数,大于开始列号小于工作表中第一行列数;(例：100,101,...);
     　* @return: List<Map<String,String>> ; map中以表中第一行为key
     　* @date 2019/6/25 15:13 
     　*/
    public static List<Map<String,String>> readExcelContent(Workbook wk,Integer sheetNo, Integer startLineNo, Integer endLineNo,Integer startCellNo,Integer endCellNo){
        String tip=checkParamsForReadExcel(wk,sheetNo,startLineNo,endLineNo,startCellNo,endCellNo);
        if(StringUtil.isNotEmpty(tip)){
            throw new BusinessException(tip);
        }
        List<Map<String,String>> result=new ArrayList<>();
        sheetNo=null==sheetNo?1:sheetNo;
        startLineNo=null==startLineNo?1:startLineNo;
        startCellNo=null==startCellNo?1:startCellNo;

        Sheet sheet=null;
        try {
            sheet=wk.getSheetAt(sheetNo-1);
            int rowNum=sheet.getLastRowNum();
            endLineNo=null==endLineNo?rowNum:endLineNo;
            Row firstRow=sheet.getRow(0);
            if(null==firstRow){
                //待定
                throw new BusinessException("工作表中首行为空！");
            }
            short firstCellNum=firstRow.getLastCellNum();
            endCellNo=null==endCellNo?firstCellNum:endCellNo;
            for (int i=startLineNo;i<=endLineNo;i++){
//                System.out.println("获取第"+i+"行数据！");
                Row row=sheet.getRow(i);
                Map<String,String> map=new HashMap<>(MapUtil.getInitialCapacityForMap(endCellNo-startCellNo+1));
                for (int j=(startCellNo-1);j<endCellNo;j++){
                    Cell firstRowCell=firstRow.getCell(j);
                    Cell cell=row.getCell(j);

                    firstRowCell.setCellType(CellType.STRING);
                    String key=firstRowCell.getStringCellValue();
                    if(StringUtil.isEmpty(key)){
                        //如果存在key为空;未定
                    }
                    if(map.containsKey(key)){
                        //如果存在相同的列;未定
                    }
                    if(null==cell){
                        map.put(key.trim(),"");
                        continue;
                    }
                    cell.setCellType(CellType.STRING);
                    String value=cell.getStringCellValue();

                    if(null==value){
                        map.put(key.trim(),"");
                        continue;
                        //如果取的值为空
                    }
                    map.put(key.trim(),value.trim());
                }
                result.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }





    /**
     　* @description: 从文件中获取工作簿
     * @ author mxl
     　* @params:
     　* @return:
     　* @date 2019/6/25 17:16
     　*/
    private static Workbook getWorkbookFromMultipartFile(MultipartFile file){
        if (null==file){
            throw new BusinessException("获取工作簿时，file为空！");
        }
        Workbook wk=null;
        InputStream is=null;
        try {
            is=file.getInputStream();
            String suffix= FileUtils.getFileSuffix(file);
            if(HSSF_SUFFIX.equalsIgnoreCase(suffix)){
                wk=new HSSFWorkbook(is);
            }
            if(XSSF_SUFFIX.equalsIgnoreCase(suffix)){
                wk=new XSSFWorkbook(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(null!=is){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(null==wk){
            throw new BusinessException("未获取到工作簿！");
        }
        return wk;
    }

     /**
     　* @description: 为ReadExcel校验参数
       * @ author mxl
     　* @params:
     　* @return: 
     　* @date 2019/6/25 15:41 
     　*/
    private static String checkParamsForReadExcel(Workbook wk,Integer sheetNo, Integer startLineNo, Integer endLineNo,Integer startCellNo,Integer endCellNo){
        if(null==wk){
            return "校验Excel参数有误;Workbook为空！";
        }
        sheetNo=null==sheetNo?1:sheetNo;
        startLineNo=null==startLineNo?1:startLineNo;
        Sheet sheet=getSheetFromWorkbookBySheetNo(wk,sheetNo);
        int rowNum=sheet.getLastRowNum();
        endLineNo=null==endLineNo?rowNum:endLineNo;
        String tip=checkLineNo(startLineNo,endLineNo,rowNum);
        if(StringUtil.isNotEmpty(tip)){
            throw new BusinessException(tip);
        }
        Row row=sheet.getRow(0);
        if(null==row){
            return "校验Excel参数有误;获取的行为空！";
        }
        int cellNum=row.getLastCellNum();
        endCellNo=null==endCellNo?cellNum:endCellNo;
        tip=checkCellNo(startCellNo,endCellNo,cellNum);
        if(StringUtil.isNotEmpty(tip)){
            throw new BusinessException(tip);
        }
        return null;
    }


 /**
 　* @description: 从MultipartFile文件中获取excel中指定工作表的第一行数据(表头);sheetNo为null,则获取第一个工作表
   * @ author mxl
 　* @params: file:MultipartFile格式文件
 　* @return: List数据集合
 　* @date 2019/6/25 17:29 
 　*/
    public static List<String> getExcelFirstData(MultipartFile file,Integer sheetNo){
        sheetNo=null==sheetNo?1:sheetNo;
        Workbook wk=getWorkbookFromMultipartFile(file);
        Sheet sheet=getSheetFromWorkbookBySheetNo(wk,sheetNo);
        List<String> result=new ArrayList<>();
        Row firstRow=sheet.getRow(0);
        for (Cell cell : firstRow) {
            if(null!=cell){
                cell.setCellType(CellType.STRING);
                String value=cell.getStringCellValue();
                result.add(value);
            }
        }
        return result;
    }
     /**
     　* @description: 校验工作表编号
       * @ author mxl
     　* @params:sheetNo：工作表编号，为null时默认给1
                sheetNum：工作簿中工作表的数目，不允许为null，应大于0
     　* @return: 
     　* @date 2019/6/25 17:48 
     　*/
    private static String checkSheetNo(Integer sheetNo,Integer sheetNum){
        sheetNo=null==sheetNo?1:sheetNo;
        if(null==sheetNum||sheetNum<1){
            return "校验工作表编号，未获取到工作表总数！";
        }
        if(sheetNo<1){
            return "校验工作表编号，工作表编号应大于等于1 ！";
        }
        if(sheetNo>sheetNum){
            return "校验工作表编号，工作表编号应小于工作簿中工作表数目！";
        }
        return "";
    }
     /**
     　* @description: 校验行编号
       * @ author mxl
     　* @params:  startLineNo:开始行编号，为null时则默认给1
                    endLineNo:结束行编号，不允许为null
                    rowNum:sheet中数据总行数，不允许为null，应大于0
     　* @return:返回错误提示信息，没有错误则返回空字符串或返回null；
     　* @date 2019/6/26 11:55
     　*/
    private static String checkLineNo(Integer startLineNo,Integer endLineNo,Integer rowNum){
        startLineNo=null==startLineNo?1:startLineNo;
        if(null==rowNum||rowNum<1){
            return "校验行编号，数据总数为空！";
        }
        if(null==endLineNo){
            return "校验行编号，结束行编号为空！";
        }
        if(startLineNo<1){
            return "校验行编号，开始行大于等于1！";
        }
        if(startLineNo>endLineNo){
            return "校验行编号，开始行大于结束行！";
        }
        if(endLineNo>rowNum){
            return "校验行编号，结束行大于数据总数！";
        }
        return "";
    }
     /**
     　* @description: 校验单元格编号
       * @ author mxl
     　* @params:  startCellNo:开始单元格编号：为null时默认给1
                    endCellNo：结束单元格编号;不允许为null
                    cellNum：单元格总数，不允许为null，应大于0
     　* @return: 返回错误提示信息，没有错误则返回空字符串或返回null；
     　* @date 2019/6/26 11:56 
     　*/
    private static String checkCellNo(Integer startCellNo,Integer endCellNo,Integer cellNum){
        startCellNo=null==startCellNo?1:startCellNo;
        if(null==cellNum||cellNum<1){
            return "校验单元编号，行单元总数为空！";
        }
        if(null==endCellNo){
            return "校验单元编号，结束单元编号为空！";
        }
        if(startCellNo<1){
            return "校验单元编号，开始单元编号大于等于1！";
        }
        if(startCellNo>endCellNo){
            return "校验单元编号，开始单元编号大于结束单元编号！";
        }
        if(endCellNo>cellNum){
            return "校验单元编号，结束单元编号大于行单元总数！";
        }
        return "";

    }


    
     /**
     　* @description: 向已存在的Excel中写入数据,如果表头(第一行)为空，则将datas的第一条数据的key集合写入表头;
       * @ author mxl
     　* @params:  wk:工作簿；
                    datas:数据集；datas中的Map为对应的数据，key为第一行表头，value为对应数据
                    sheetNo:指定的工作表;sheetNo为Null时默认给1
                    startLineNo:开始写入的行;startLineNo为null时默认给2
                    endLineNo:写入的终止行;endLineNo为null时默认给datas的长度
                    startCellNo:开始写入的单元格;startCellNo为null时默认给1
                    endCellNo:结束写入的单元格;endCellNo为null时默认给表头的长度
     　* @return: 
     　* @date 2019/6/26 13:43 
     　*/
    public static void writeDataToExistExcel(Workbook wk,List<Map<String,String>> datas,Integer sheetNo, Integer startLineNo, Integer endLineNo,Integer startCellNo,Integer endCellNo){
        if(BeanUtil.isEmpty(datas)){
            throw new BusinessException("向已存在Excel写入数据，数据为空！");
        }
        sheetNo=null==sheetNo?1:sheetNo;
        startLineNo=null==startLineNo?2:startLineNo;
        endLineNo=endLineNo==null?datas.size():endLineNo;
        startCellNo=startCellNo==null?1:startCellNo;


        Sheet sheet=getSheetFromWorkbookBySheetNo(wk,sheetNo);
        Row firstRow=sheet.getRow(0);

        String tip=checkLineNo(startLineNo,endLineNo,datas.size());
        if(StringUtil.isNotEmpty(tip)){
            throw new BusinessException(tip);
        }

        if(null==firstRow){
            List<String> list=new ArrayList<>(datas.get(0).keySet());
            writeDataForFirstRow(sheet,list);
            firstRow=sheet.getRow(0);
        }
        int firstRowCellNum=firstRow.getLastCellNum();
        endCellNo=endCellNo==null?firstRowCellNum:endCellNo;
        tip=checkCellNo(startCellNo,endCellNo,firstRowCellNum);
        if(StringUtil.isNotEmpty(tip)){
            throw new BusinessException(tip);
        }
        for(int i=(startLineNo-1);i<=endLineNo;i++){
            Row row=sheet.getRow(i);
            row=row==null?sheet.createRow(i):row;

            Map<String,String> data=datas.get(i-1);
            for(int j=(startCellNo-1);j<endCellNo;j++){
                Cell firstRowCell=firstRow.getCell(j);
                firstRowCell.setCellType(CellType.STRING);
                String key=firstRowCell.getStringCellValue();

                Cell cell=row.getCell(j);
                cell=cell==null?row.createCell(j):cell;
                cell.setCellType(CellType.STRING);
                cell.setCellValue(data.containsKey(key)?data.get(key):"");
            }
        }
    }
    
     /**
     　* @description: 向第一行(表头)写入数据，不考虑复杂表头;
       * @ author mxl
     　* @params: sheet:工作表;datas:要写入的数据
     　* @return: 
     　* @date 2019/6/26 14:57 
     　*/
    private static void writeDataForFirstRow(Sheet sheet,List<String> datas){
        if(BeanUtil.isEmpty(datas)){
            throw new BusinessException("写入第一行表头数据时，数据为空！");
        }
        if(null==sheet){
            throw new BusinessException("写入第一行表头数据时，工作表为空！");
        }
        Row row =sheet.createRow(0);
        for(int i=0;i<datas.size();i++){
            Cell cell=row.createCell(i);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(datas.get(i));
        }
    }
     /**
     　* @description: 从工作簿中按sheetNo获取工作表，并校验sheetNo是否合法
       * @ author mxl
     　* @params:wk:工作簿，sheetNo:工作簿中工作表的编号
     　* @return:sheet,获取到的工作表
     　* @date 2019/6/26 15:00
     　*/
    private static Sheet getSheetFromWorkbookBySheetNo(Workbook wk,Integer sheetNo){
        String tip=checkSheetNo(sheetNo,wk.getNumberOfSheets());
        if(StringUtil.isNotEmpty(tip)){
            throw new BusinessException(tip);
        }
        Sheet sheet=wk.getSheetAt(sheetNo-1);
        if(null==sheet){
            throw new BusinessException("从工作簿中按sheetNo未获取到工作表！");
        }
        return sheet;
    }




}
