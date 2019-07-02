package com.kayak.algorithmtest.service;

import com.kayak.algorithmtest.entity.BondInfo;
import com.kayak.algorithmtest.mapper.BondInfoMapper;
import com.kayak.cloud.algorithm.comm.CALCResponse;
import com.kayak.cloud.algorithm.comm.Foreign;
import com.kayak.common.untils.BeanUtil;
import com.kayak.common.untils.ExcelUtil;
import com.kayak.common.untils.MapUtil;
import com.kayak.common.untils.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author mxl
 * @title: BatchTestServiceImp
 * @projectName algorithm_test
 * @description: TODO
 * @date 2019/6/27 17:26
 */
@Service
public class BatchTestServiceImp implements IBatchTestService {


//    时间急先这样定义，后面做Excel模板统一做映射;
    private final String MARKET_CODE="市场";

    private final String BOND_CODE="债券代码";

    private final String EXCEL_DATE="日期";

    private final String CACL_INTEREST="计算应收利息";

    private final String FULL_PRICE="全价";

    private final String REAL_PRICE="估值净价";

    private final String CACL_YTM="计算到期收益率";

    private final String RESULT="比较结果（1-正确，0-错误）";

    private final String EXCEL_INTEREST="应收利息";

    private final String EXCEL_YTM="收益率";

    @Autowired
    private BondInfoMapper bondInfoMapper;

    @Override
    public MultipartFile batchTestValuation(MultipartFile file)throws Exception{
        Long t1=System.currentTimeMillis();
        List<Map<String,String>> result= ExcelUtil.readExcelContent(file);
        Map<String, com.kayak.cloud.algorithm.model.BondInfo> bondInfoMap=new HashMap<>(MapUtil.getInitialCapacityForMap(16));
        for (Map<String, String> map : result) {
            Boolean flag=true;
            String bondId=map.get(MARKET_CODE)+map.get(BOND_CODE);
            com.kayak.cloud.algorithm.model.BondInfo  bondInfo=new com.kayak.cloud.algorithm.model.BondInfo();
            if(bondInfoMap.containsKey(bondId)){
                bondInfo=bondInfoMap.get(bondId);
                if(BeanUtil.isEmpty(bondInfo)){
                    continue;
                }
            }else{
                BondInfo bondInfo1= bondInfoMapper.getBondInfoByBondId(bondId);
                if(BeanUtil.isEmpty(bondInfo1)){
                    bondInfoMap.put(bondId,null);
                    System.out.println(bondId);
                    continue;
//                    throw new BusinessException("通过bond_id未查询到bondInfo信息，该bond_id为"+bondId);
                }
                BeanUtils.copyProperties(bondInfo1,bondInfo);
                bondInfoMap.put(bondId,bondInfo);
            }
//            CALCResponse res= Foreign.GetAI(bondInfo,null,map.get(EXCEL_DATE),null);
            if(StringUtil.isEmpty(map.get(EXCEL_YTM))){
                System.out.println(map.toString());
            }
            CALCResponse res= Foreign.GetFullPriceByYTM(bondInfo,new BigDecimal(StringUtil.isEmpty(map.get(EXCEL_YTM))?"0":map.get(EXCEL_YTM)),map.get(EXCEL_DATE));
            CALCResponse res2=Foreign.GetYTMByFullPrice(bondInfo,new BigDecimal(map.get(FULL_PRICE)),map.get(EXCEL_DATE));

            if(res.isOK()){
                BigDecimal interest= res.toBigDecimal();
                if(null!=interest){
                    map.put(CACL_INTEREST,interest.toString());
                    if(!interest.toString().equals(map.get(EXCEL_INTEREST))){
                        flag=false;
                    }
                }
            }
            if(res2.isOK()){
                BigDecimal ytm=res2.toBigDecimal();
                if(null!=ytm){
                    map.put(CACL_YTM,ytm.toString());
                    if(!ytm.toString().equals(map.get(EXCEL_YTM))){
                        flag=false;
                    }
                }
            }
            map.put(RESULT,flag?"1":"0");
        }
        ExcelUtil.writeDataToExistExcel(file,result,null,null,null,8,10);
        System.out.println(System.currentTimeMillis()-t1);
        return file;
    }


    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        Long t1=System.currentTimeMillis();
        System.out.println(t1);
        InputStream is=null;
        OutputStream out=null;
        try {

            String filePath = "E:\\Z\\中债估值.xlsx";
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            // MockMultipartFile(String name, @Nullable String originalFilename, @Nullable String contentType, InputStream contentStream)
            // 其中originalFilename,String contentType 旧名字，类型  可为空
            // ContentType.APPLICATION_OCTET_STREAM.toString() 需要使用HttpClient的包
//            MultipartFile multipartFile = new MockMultipartFile("copy"+file.getName(),file.getName(),ContentType.APPLICATION_OCTET_STREAM.toString(),fileInputStream);


//            is= new FileInputStream("E:\\Z\\中债估值.xlsx");
            ExcelUtil util = new ExcelUtil();
//            XSSFWorkbook xssfWorkbook=new XSSFWorkbook(is);

            out=new FileOutputStream("E:\\Z\\test.xlsx");
//            xssfWorkbook.write(out);
            System.out.println(System.currentTimeMillis()-t1);
        }
        catch (Exception e) {
            if (e.toString().contains("java.io.FileNotFoundException")) {
                System.out.println("系统找不到指定的文件!");
            }
            else if (e.toString().contains("org.apache.poi.poifs.filesystem.OfficeXmlFileException: The supplied data appears to be in the Office 2007+ XML")) {
                System.out.println("请改成xls格式文件!");
            }
            else if (e.toString().contains("java.lang.RuntimeException: colNum error")) {
                System.out.println("文档列数不符合规则!");
            }
            else if (e.toString().contains("java.lang.RuntimeException: CellOfNull")) {
                System.out.println("文档列数为null!");
            }
            e.printStackTrace();
        }finally {
            try {
                if(is!=null){
                    is.close();
                }
                if(out!=null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
