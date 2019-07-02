package com.kayak.algorithmtest.utils;

import com.kayak.common.untils.StringUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Date;

/**
 * @author mxl
 * @title: FileUploadUtils
 * @projectName algorithm_test
 * @description: TODO
 * @date 2019/6/2414:38
 */
public class FileUploadUtils {

    /** 默认上传下载文件大小 **/
    public static final long DEFAULT_MAX_SIZE = 52428800;
    /** 默认上传目录 **/
    public static String defaultBaseDir = "upload";
    /** 默认上传根路径 **/
    public static String defaultAbsoluteRootPath = "";
    /** 配置路径 **/
    public static String ConfigPath = "";
    /** 不带时间戳的文件名 **/
    public static String fileName = "";
    /** 文件上传的绝对路径 **/
    public static String uploadFilePath = "";
    /** 上传文件名 **/
    public static String uploadFileName = "";
    /** 下载文件名 **/
    public static String downloadFileName = "";
    /** 下载文件路径 **/
    public static String downFilePath = "";

    /**
     * 文件上传 只提供上传功能，不提供对上传内容进行处理功能 文件名带有时间戳
     *
     * @param request
     * @param file
     *            上传文件
     * @return
     * @throws Exception
     */
    public static final String upload(HttpServletRequest request,
                                      MultipartFile file) throws Exception {
        String filename = extractFilename(file, defaultBaseDir);
        String realpath = StringUtil.isEmpty(defaultAbsoluteRootPath)?extractUploadDir(request):defaultAbsoluteRootPath;

        File desc = getAbsoluteFile(realpath, filename);
        // 上传文件
        file.transferTo(desc);
        return filename;
    }


    /**
     * 文件下载 只提供下载功能，不提供对下载内容进行处理功能
     *
     * @param request  HttpServletRequest
     * @param response  HttpServletResponse
     * @param fileName
     *            文件名
     * @param ContentType
     *            相应类型
     * @param fileByteSize
     *            文件字节大小
     * @return
     * @throws IOException
     */
    public static final String download(HttpServletRequest request,
                                        HttpServletResponse response, String fileName, String ContentType,
                                        int fileByteSize) throws IOException {

        /** 截取文件名 **/
        String newFileName = "";
        newFileName = new String(fileName.getBytes("UTF-8"), "UTF-8");
        newFileName = extractFilename(newFileName);

        /** 获取下载路径 **/
        String realpath = ConfigPath;

        if (null == realpath || "".equals(realpath)) {
            if (!"".equals(defaultAbsoluteRootPath)) {
                realpath = defaultAbsoluteRootPath + "/" + defaultBaseDir;
            } else {
                realpath = extractUploadDir(request);
            }

        } else {
            realpath = realpath + "/" + defaultBaseDir;
        }
        String downLoadPath = realpath + "/" + newFileName;// 下载路径
        setDownFilePath(downLoadPath);
        setDownloadFileName(newFileName);

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        bis = new BufferedInputStream(new FileInputStream(downLoadPath));
        bos = new BufferedOutputStream(response.getOutputStream());

        /** 设置字符编码集 **/
        if ("".equals(ContentType) || null == ContentType) {
            response.setContentType("text/html;charset=UTF-8");
        } else {
            response.setContentType(ContentType);
        }
        response.setCharacterEncoding("UTF-8");

        /** 设置响应类型为attachment **/
        long fileLength = new File(downLoadPath).length();
        //newFileName=URLEncoder.encode(newFileName, "UTF8");
        response.setHeader("Content-disposition", "attachment; filename="
                + newFileName);

//			response.setHeader("Content-disposition","attachment;filename="+java.net.URLEncoder.encode(newFileName, "UTF-8"));
        response.setHeader("Content-Length", String.valueOf(fileLength));

        byte[] buff = null;
        if (fileByteSize > 0) {
            buff = new byte[fileByteSize];
        } else {
            buff = new byte[(int) DEFAULT_MAX_SIZE];
        }

        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
        }

        bis.close();
        bos.close();

        return null;
    }

    /**
     * 删除单个文件 ，直接传入文件路径或者文件名都可以，方法会取到文件名，根据您设置的根路径找到文件进行删除
     *
     * @param filename
     * @return
     */
    public static String deleteFile(String filename) {
        String realpath = ConfigPath;
        //String realpath = Global.getGlobalConf(ConfigPath);
        filename = extractFilename(filename);
        int index = filename.lastIndexOf('\\');
        if (index >= 0) {
            filename = filename.replaceAll("/", "\\\\");
        }
        if (null == realpath || "".equals(realpath)) {
            if (!"".equals(defaultAbsoluteRootPath)) {
                realpath = defaultAbsoluteRootPath + "/" + defaultBaseDir;
                filename = realpath + "/" + filename;
            } else {
                return "文件路径不正确";
            }
        } else {
            realpath = realpath + "/" + defaultBaseDir;
            filename = realpath + "/" + filename;
        }
        // 删除文件
        if (null != filename && !"".equals(filename)
                && !"NULL".equals(filename)) {
            File file = new File(filename);
            if (file.exists()) {
                if (file.isFile()) {
                    file.delete();
                    return "";
                } else {
                    return "不是一个文件";
                }
            } else {
                return "文件不存在";
            }
        } else {
            return "文件不存在";
        }

    }

    /**
     * 获取上传保存文件的绝对路径的文件对象
     *
     * @param uploadDir
     * @param filename
     * @return
     * @throws IOException
     */
    private static final File getAbsoluteFile(String uploadDir, String filename)
            throws IOException {
        if (uploadDir.endsWith("/")) {
            uploadDir = uploadDir.substring(0, uploadDir.length() - 1);
        }
        if (filename.startsWith("/")) {
            filename = filename.substring(0, uploadDir.length() - 1);
        }
        File deFile=new File(uploadDir);
        deFile.setWritable(true, false);
        File desc = new File(uploadDir + "/" + filename);
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        if (!desc.exists()) {
            desc.createNewFile();
        }
        setUploadFilePath(desc.toString());
        return desc;
    }

    /**
     * 获取带有时间戳的文件名(上传)
     *
     * @param file
     * @param baseDir
     * @return
     * @throws UnsupportedEncodingException
     */
    private static final String extractFilename(MultipartFile file,
                                                String baseDir) throws UnsupportedEncodingException {
        String filename = file.getOriginalFilename();
        setFileName(filename);
        int slashIndex = filename.indexOf("/");
        if (slashIndex >= 0) {
            filename = filename.substring(slashIndex + 1);
        }

        filename = formatterFileName(filename);
        setUploadFileName(filename);
        filename = baseDir + "/" + filename;
        return filename;
    }

    /**
     * 获取不带有时间戳的文件名(上传)
     *
     * @param file
     * @param baseDir
     * @return
     * @throws UnsupportedEncodingException
     */
    private static final String extractFilenameNotTime(MultipartFile file,
                                                       String baseDir) throws UnsupportedEncodingException {
        String filename = file.getOriginalFilename();
        setFileName("test12.doc");
        int slashIndex = filename.indexOf("/");
        if (slashIndex >= 0) {
            filename = filename.substring(slashIndex + 1);
        }
        setUploadFileName("test.doc");
        if(baseDir.endsWith("\\") || baseDir.endsWith("/")){
            baseDir = baseDir.substring(0, baseDir.length()-1);
        }
        filename = baseDir + "/" + URLEncoder.encode(filename,"UTF-8");
        return filename;
    }

    /**
     * 获取文件的文件名 ，例如：c:\wu\ht\tt.txt 最后获取的是tt.txt
     *
     * @param fileName
     * @return
     * @throws UnsupportedEncodingException
     */
    public static final String extractFilename(String fileName) {
        int slashIndex = fileName.lastIndexOf("\\");
        int slashIndex2 = fileName.lastIndexOf("/");
        if (slashIndex >= 0) {
            fileName = fileName.substring(slashIndex + 1);
        } else if (slashIndex2 >= 0) {
            fileName = fileName.substring(slashIndex + 1);
        }
        return fileName;
    }

    /**
     * 获取eclipse/myeclipse工程根路径的绝对路径
     *
     * @param request
     * @return
     */
    public static final String extractUploadDir(HttpServletRequest request) {

        return request.getSession().getServletContext().getRealPath("/")
                + defaultBaseDir;

    }

    /**
     * 格式化文件名 在文件名结尾加上时间戳
     *
     * @param name
     * @return
     */
    public static String formatterFileName(String name) {
        int lastindex = name.lastIndexOf('.');
        String head = name.substring(0, lastindex);
        String end = name.substring(lastindex);
        Date date = new Date();
        long time = date.getTime();
        return head + time + end;
    }

    public static String getUploadFileName() {
        return uploadFileName;
    }

    public static void setUploadFileName(String uploadFileName) {
        FileUploadUtils.uploadFileName = uploadFileName;
    }

    public static String getUploadFilePath() {
        return uploadFilePath;
    }

    public static void setUploadFilePath(String uploadFilePath) {
        FileUploadUtils.uploadFilePath = uploadFilePath;
    }

    public static String getDownloadFileName() {
        return downloadFileName;
    }

    public static void setDownloadFileName(String downloadFileName) {
        FileUploadUtils.downloadFileName = downloadFileName;
    }

    public static String getDownFilePath() {
        return downFilePath;
    }

    public static void setDownFilePath(String downFilePath) {
        FileUploadUtils.downFilePath = downFilePath;
    }

    public static String getConfigPath() {
        return ConfigPath;
    }

    public static void setConfigPath(String configPath) {
        ConfigPath = configPath;
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        FileUploadUtils.fileName = fileName;
    }

}
