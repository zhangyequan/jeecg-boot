package org.jeecg.modules.visit.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
public class UploadService {

    public JSONObject uploadInfo(String visitinfo, MultipartFile file, String uploadDir) {
        JSONObject object = new JSONObject();
        try {
            //图片路径
            String imgUrl = null;
            //上传
            String imgurl = upload(file, uploadDir, file.getOriginalFilename());
            if (imgurl.equals("") || imgurl==null) {
                imgUrl = new File(uploadDir).getName() + "/" + imgurl;
            }

            object.put("messageCode", "1");
            object.put("messageCont", "信息上传成功!");
            object.put("imgUrl", imgurl);
        } catch (Exception e) {
            object.put("messageCode", "0");
            object.put("messageCont", "信息上传失败!");
            e.printStackTrace();
        }finally {
            return object;
        }
    }

    private String upload(MultipartFile file, String path, String fileName) throws Exception {
        // 生成新的文件名
        String realPath = path + "/" + UUID.randomUUID().toString().replace("-", "")+fileName.substring(fileName.lastIndexOf("."));
        File dest = new File(realPath);
        // 判断文件父目录是否存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdir();
        }
        // 保存文件
        file.transferTo(dest);
        return realPath;
    }
}
