package com.kayak.common.untils.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;


public class FileUtils
{
    public static String FILENAME_PATTERN = "[a-zA-Z0-9_\\-\\|\\.\\u4e00-\\u9fa5]+";

    /**
     * 输出指定文件的byte数组
     * 
     * @param filePath 文件路径
     * @param os 输出流
     * @return
     */
    public static void writeBytes(String filePath, OutputStream os) throws IOException
    {
        FileInputStream fis = null;
        try
        {
            File file = new File(filePath);
            if (!file.exists())
            {
                throw new FileNotFoundException(filePath);
            }
            fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int length;
            while ((length = fis.read(b)) > 0)
            {
                os.write(b, 0, length);
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (os != null)
            {
                try
                {
                    os.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除文件
     * 
     * @param filePath 文件
     * @return
     */
    public static boolean deleteFile(String filePath)
    {
        boolean flag = false;
        File file = new File(filePath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists())
        {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 文件名称验证
     * 
     * @param filename 文件名称
     * @return true 正常 false 非法
     */
    public static boolean isValidFilename(String filename)
    {
        return filename.matches(FILENAME_PATTERN);
    }


    /**
     　* @description: 获取文件后缀不带小数点
     * @ author mxl
     　* @params:  file
     　* @return: String
     　* @date 2019/6/25 10:25
     　*/
    public static String getFileSuffix(MultipartFile file){
        if(null==file){
            return "";
        }
        String filename = file.getOriginalFilename();
        String suffix=filename.substring(filename.lastIndexOf(".")+1);
        return suffix;
    }

    /**
     　* @description: 获取文件后缀带小数点
     * @ author mxl
     　* @params:  file
     　* @return: String
     　* @date 2019/6/25 10:25
     　*/
    public static String getFileSuffixCarryPoint(MultipartFile file){
        return "."+getFileSuffix(file);
    }

    /**
     　* @description: 获取文件后缀
     * @ author mxl
     　* @params:
     　* @return:
     　* @date 2019/6/25 15:37
     　*/
//    public static String getFileSuffix(File file){
//        if(null==file){
//            return "";
//        }
//        String filename = file.getName();
//        String suffix=filename.substring(filename.lastIndexOf(".")+1);
//        return suffix;
//    }
}
