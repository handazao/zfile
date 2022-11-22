package im.zhaojun.zfile.module.file.controller;


import cn.hutool.core.util.StrUtil;
import cn.xuyanwu.spring.file.storage.FileInfo;
import cn.xuyanwu.spring.file.storage.FileStorageService;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.VideoJave2Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件上传
 */
@Slf4j
@RestController
public class FileDetailController {

    @Autowired
    private FileStorageService fileStorageService;//注入实列

    /**
     * 上传文件，成功返回文件 url
     */
    @PostMapping("/upload")
    public AjaxJson upload(MultipartFile file, String enterpriseName) {
        String path = "";
        if (StrUtil.isNotBlank(enterpriseName)) {
            String nowdayStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            path = enterpriseName + File.separator + nowdayStr + File.separator;
        }
        FileInfo fileInfo = fileStorageService.of(file)
                .setPath(path) //保存到相对路径下，为了方便管理，不需要可以不写
                .setObjectId("0")   //关联对象id，为了方便管理，不需要可以不写
                .setObjectType("0") //关联对象类型，为了方便管理，不需要可以不写
                .upload();  //将文件上传到对应地方
        if (fileInfo != null && StrUtil.isNotBlank(fileInfo.getUrl())) {
            try {
                String localPath = fileInfo.getBasePath() + File.separator + fileInfo.getPath() + File.separator + fileInfo.getFilename();
                log.info("视频源地址:{}", localPath);
                String targetPath = VideoJave2Utils.compressionVideo(localPath, "");
                log.info("视频压缩地址:{}", targetPath);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        }
        return fileInfo == null ? AjaxJson.getError("上传失败！") : AjaxJson.getSuccess("上传成功！", fileInfo);
    }

    /**
     * 上传图片，成功返回文件信息
     * 图片处理使用的是 https://github.com/coobird/thumbnailator
     */
    @PostMapping("/upload-image")
    public AjaxJson uploadImage(MultipartFile file) {
        FileInfo fileInfo = fileStorageService.of(file)
                .image(img -> img.size(1000, 1000))  //将图片大小调整到 1000*1000
                .thumbnail(th -> th.size(200, 200))  //再生成一张 200*200 的缩略图
                .upload();
        return fileInfo == null ? AjaxJson.getError("上传失败！") : AjaxJson.getSuccess("上传成功！", fileInfo);
    }

    /**
     * 上传文件到指定存储平台，成功返回文件信息
     */
    @PostMapping("/upload-platform")
    public FileInfo uploadPlatform(MultipartFile file) {
        return fileStorageService.of(file)
                .setPlatform("aliyun-oss-1")    //使用指定的存储平台
                .upload();
    }


    /**
     * 指定文件压缩
     *
     * @param localPath
     * @return
     */
    @GetMapping("/compression")
    public AjaxJson compression(String localPath) {
        String targetPath = "";
        if (StrUtil.isBlank(localPath)) {
            return AjaxJson.getError("文件路径不能为空!");
        }
        try {
            log.info("视频源地址:{}", localPath);
            targetPath = VideoJave2Utils.compressionVideo(localPath, "");
            log.info("视频压缩地址:{}", targetPath);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        return AjaxJson.getSuccess("操作成功！",targetPath);
    }
}
