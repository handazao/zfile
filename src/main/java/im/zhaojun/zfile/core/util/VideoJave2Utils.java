package im.zhaojun.zfile.core.util;


import cn.hutool.core.date.StopWatch;
import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.ScreenExtractor;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.filtergraphs.OverlayWatermark;
import ws.schild.jave.filters.helpers.OverlayLocation;
import ws.schild.jave.info.AudioInfo;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoSize;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class VideoJave2Utils {
    /**
     * 允许的视频格式
     */
    static final String ALLOW_IMG_FORMAT = "avi|wma|rmvb|flash|mp4|mid|3gp|wmv|mpg|mp3|mkv|mpeg|mov|flv";
    /**
     * 水印视频名字拼接
     */
    static final String NEW_VIDOE_WATER_NAME_PRE_STR = "h";

    /***
     * 压缩视频文件后缀
     */
    static final String NEW_VIDOE_ZIP_NAME_PRE_STR = "_ys";
    /**
     * 视频截图文件名拼接
     * eg:原视频 test.mp4
     * 截图名称 test_sc1.jpg 、test_sc10.jpg
     */
    static final String NEW_SC_NAME_PRE_STR = "_sc";

    /**
     * 视频加图片水印
     * jave2插件     3.0版本后 针对windows版本的路径有bug,导致windows下添加水印命令不成功
     * 但是3.0版本前，没找到添加水印功能，所以不考虑windows或则反编译MovieFilter.escapingPath 方法修改
     * <p>
     * movie='C\\:\\Users\\lin\\Desktop\\usedMarketIcon.png'[watermark];[0:v][watermark]overlay='main_w-overlay_w-10:main_h-overlay_h-10'
     * <p>
     * C\\: 其实只需要一个\
     *
     * @param urlYuan       源视频绝对路径
     * @param targetUrl     水印视频绝对路径
     * @param waterIconPath 水印图文件绝对路径
     * @throws EncoderException
     */
    public static String generateVideoImageWatermark(String urlYuan, String targetUrl, String waterIconPath) throws EncoderException {
        /**如果没有传入生成后的地址,在在源目录下保存生成后的水印视频**/
        if (StrUtil.isBlank(targetUrl)) {
            targetUrl = urlYuan.substring(0, urlYuan.lastIndexOf(".")) + NEW_VIDOE_WATER_NAME_PRE_STR + urlYuan.substring(urlYuan.lastIndexOf("."));
        }
        if (StrUtil.isBlank(waterIconPath)) {
            throw new RuntimeException("水印图地址为空");
        }
        File sourceVideo = new File(urlYuan);
        if (!sourceVideo.exists()) {
            throw new RuntimeException("源视频文件不存在" + sourceVideo.getAbsolutePath());
        }
        File watermark = new File(waterIconPath);
        if (!watermark.exists()) {
            throw new RuntimeException("水印图文件不存在：" + waterIconPath);
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        VideoAttributes vidAttr = new VideoAttributes();
        OverlayWatermark overlayWatermark = new OverlayWatermark(watermark, OverlayLocation.BOTTOM_RIGHT, -10, -10);
        log.info("overlayWatermark.getExpression()" + overlayWatermark.getExpression());
        vidAttr.addFilter(overlayWatermark);
        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setCodec(AudioAttributes.DIRECT_STREAM_COPY);
        EncodingAttributes encAttr = new EncodingAttributes().setVideoAttributes(vidAttr).setAudioAttributes(audioAttributes);
        File target = new File(targetUrl);
        new Encoder().encode(new MultimediaObject(sourceVideo), target, encAttr);
        stopWatch.stop();
        log.info("生成视频图片水印 =" + urlYuan + "水印视频 =" + targetUrl + "耗时=" + stopWatch.getTotalTimeMillis() + "毫秒");
        return targetUrl;
    }

    /**
     * 获取视频时长 返回秒
     *
     * @param urlYuan 原视频绝对路径
     * @return
     * @throws EncoderException
     */
    public static int readVideoLength(String urlYuan) throws EncoderException {
        File sourceFile = new File(urlYuan); //
        MultimediaObject multimediaObject = new MultimediaObject(sourceFile);
        MultimediaInfo multimediaInfo = multimediaObject.getInfo();
        long sum = multimediaInfo.getDuration() / 1000; //时长sum单位：秒
        return (int) sum;
    }

    /**
     * 视频压缩
     *
     * @param urlYuan
     * @param targetUrl
     * @return
     * @throws EncoderException
     */
    public static String compressionVideo(String urlYuan, String targetUrl) throws EncoderException {
        // 视频属性设置
        /**
         * 码率：影响体积，与体积成正比：码率越大，体积越大；码率越小，体积越小。
         */
        int audioMaxBitRate = 128000;
        int videoMaxBitRate = 800000;
        /**
         * 采样率
         */
        int maxSamplingRate = 44100;

        /**
         * 帧率（FPS） ：影响画面流畅度，与画面流畅度成正比：帧率越大，画面越流畅；帧率越小，画面越有跳动感。
         * 如果码率为变量，则帧率也会影响体积，帧率越高，每秒钟经过的画面越多，需要的码率也越高，体积也越大。
         * 帧率就是在1秒钟时间里传输的图片的帧数，也可以理解为图形处理器每秒钟能够刷新几次。
         * 每秒显示的图片数影响画面流畅度，与画面流畅度成正比：帧率越大，画面越流畅；帧率越小，画面越有跳动感。
         * 由于人类眼睛的特殊生理结构，如果所看画面之帧率高于16的时候，就会认为是连贯的，此现象称之为视觉暂留。
         * 并且当帧速达到一定数值后，再增长的话，人眼也不容易察觉到有明显的流畅度提升了。
         */
        int maxFrameRate = 20;//帧率
        int maxWidth = 1280;//压缩后的视频最大宽度
        if (StrUtil.isBlank(targetUrl)) {
            targetUrl = urlYuan.substring(0, urlYuan.lastIndexOf(".")) + NEW_VIDOE_ZIP_NAME_PRE_STR + maxWidth + urlYuan.substring(urlYuan.lastIndexOf("."));
        }
        File sourceFile = new File(urlYuan);
        File target = new File(targetUrl);
        MultimediaObject multimediaObject = new MultimediaObject(sourceFile);
        MultimediaInfo multimediaInfo = multimediaObject.getInfo();
        AudioInfo audioInfo = multimediaInfo.getAudio();
        double mb = Math.ceil(sourceFile.length() / 1048576);
        //int second = (int) multimediaObject.getInfo().getDuration() / 1000;
        //int second = (int) multimediaInfo.getDuration() / 1000;
        log.info("原视频={},大小={}M", urlYuan, mb);
        //BigDecimal bd = new BigDecimal(String.format("%.4f", mb / second));
        //log.info("开始压缩视频了--> 视频每秒平均 " + bd + " MB ");
        // 根据视频大小来判断是否需要进行压缩,视频 > 5MB, 或者每秒 > 0.5 MB 才做压缩， 不需要的话可以把判断去掉
        int maxSize = 10;
        //boolean temp = mb > maxSize || bd.compareTo(new BigDecimal(0.5)) > 0;
        boolean temp = mb < maxSize;
        if (temp) {
            return urlYuan;
        }
        long time = System.currentTimeMillis();
        AudioAttributes audio = new AudioAttributes();
        // 设置通用编码格式10                   audio.setCodec("aac");
        // 设置最大值：比特率越高，清晰度/音质越好
        // 设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 128000 = 182kb)
        //if (audioInfo.getBitRate() > audioMaxBitRate) {
        //    audio.setBitRate(new Integer(audioMaxBitRate));
        //}

        // 设置重新编码的音频流中使用的声道数（1 =单声道，2 = 双声道（立体声））。如果未设置任何声道值，则编码器将选择默认值 0。
        //audio.setChannels(audioInfo.getChannels());
        // 采样率越高声音的还原度越好，文件越大
        // 设置音频采样率，单位：赫兹 hz
        // 设置编码时候的音量值，未设置为0,如果256，则音量值不会改变
        // audio.setVolume(256);
        //if (audioInfo.getSamplingRate() > maxSamplingRate) {
        //    audio.setSamplingRate(maxSamplingRate);
        //}

        // 视频编码属性配置
        ws.schild.jave.info.VideoInfo videoInfo = multimediaInfo.getVideo();
        VideoAttributes video = new VideoAttributes();
        video.setCodec("h264");
        //设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 800000 = 800kb)
        if (videoInfo.getBitRate() > videoMaxBitRate) {
            video.setBitRate(videoMaxBitRate);
        }

        // 视频帧率：15 f / s  帧率越低，效果越差
        // 设置视频帧率（帧率越低，视频会出现断层，越高让人感觉越连续），视频帧率（Frame rate）是用于测量显示帧数的量度。所谓的测量单位为每秒显示帧数(Frames per Second，简：FPS）或“赫兹”（Hz）。
        if (videoInfo.getFrameRate() > maxFrameRate) {
            video.setFrameRate(maxFrameRate);
        }

        // 限制视频宽高
        int width = videoInfo.getSize().getWidth();
        int height = videoInfo.getSize().getHeight();
        if (width > maxWidth) {
            float rat = (float) width / maxWidth;
            video.setSize(new VideoSize(maxWidth, (int) (height / rat)));
        }

        EncodingAttributes attr = new EncodingAttributes();
        // attr.setOutputFormat("mp4");
        attr.setAudioAttributes(audio);
        attr.setVideoAttributes(video);
        //attr.setEncodingThreads(Runtime.getRuntime().availableProcessors() / 2);
        new Encoder().encode(multimediaObject, target, attr);
        log.info("压缩视频={},新视频={},耗时={}秒", sourceFile, targetUrl, (System.currentTimeMillis() - time) / 1000);
        return targetUrl;
    }

    /**
     * 将视频时长转换成"00:00:00.000"的字符串格式
     *
     * @param duration 视频时长（单位：秒）
     * @return
     */
    public static String durationFormatToString(BigDecimal duration) {
        BigDecimal nine = BigDecimal.valueOf(9);
        BigDecimal sixty = BigDecimal.valueOf(60);

        BigDecimal second = duration.divideAndRemainder(sixty)[1];
        BigDecimal minute = duration.subtract(second).divide(sixty).divideAndRemainder(sixty)[1];
        BigDecimal hour = duration.subtract(second).divideToIntegralValue(BigDecimal.valueOf(3600));

        String str = "";
        if (hour.intValue() != 0) {
            if (hour.compareTo(nine) > 0) {
                str += hour.intValue() + ":";
            } else {
                str += "0" + hour.intValue() + ":";
            }
        }
        if (minute.compareTo(nine) > 0) {
            str += minute.intValue() + ":";
        } else {
            str += "0" + minute.intValue() + ":";
        }
        if (second.compareTo(nine) > 0) {
            str += second.intValue();
        } else {
            str += "0" + second.intValue();
        }
        return str;
    }

    /**
     * 视频截取
     *
     * @param urlYuan     原视频绝对路径 eg:C:/Users/lin/Desktop/test.mp4
     * @param targetPath  截图路径 eg:C:/Users/lin/Desktop/
     * @param secondsList 截取的秒 列表 eg:[1,10,20] 截取 第1秒，第10秒，第20秒
     * @throws EncoderException
     */
    public static void generateVideoManyScreenImage(String urlYuan, String targetPath, List<Integer> secondsList) throws EncoderException {
        checkIsVideo(urlYuan);
        /**默认同目录下**/
        if (StrUtil.isBlank(targetPath)) {
            targetPath = urlYuan.substring(0, urlYuan.lastIndexOf("."));
        }
        /**默认截取第1秒**/
        if (secondsList == null || secondsList.size() == 0) {
            secondsList = new ArrayList<>();
            secondsList.add(1);
        }
        MultimediaObject multimediaObject = new MultimediaObject(new File(urlYuan));
        ScreenExtractor screenExtractor = new ScreenExtractor();
        for (Integer seconds : secondsList) {
            String targetUrl = targetPath + NEW_SC_NAME_PRE_STR + seconds + ".jpg";
            int width = -1;
            int height = -1;
            long millis = seconds * 1000;
            File outputFile = new File(targetUrl);
            int quality = 1;
            log.info("原视频 = {},生成截图 = {}", urlYuan, targetUrl);
            screenExtractor.renderOneImage(multimediaObject, width, height, millis, outputFile, quality);
        }
    }


    /**
     * 校验文件是否是视频文件
     *
     * @param urlYuan 视频绝对路径
     */
    public static void checkIsVideo(String urlYuan) {
        if (StrUtil.isBlank(urlYuan)) {
            throw new RuntimeException("源视频路径为空");
        }
        urlYuan = urlYuan.toLowerCase();
        String suffix = urlYuan.substring(urlYuan.lastIndexOf(".") + 1);
        if (!ALLOW_IMG_FORMAT.contains(suffix)) {
            throw new RuntimeException("源视频格式不合法");
        }
    }

}
