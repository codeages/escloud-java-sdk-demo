package com.codeages.demo.controller;

import com.codeages.escloud.Auth;
import com.codeages.escloud.service.PlayService;
import com.codeages.escloud.service.ResourceService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class DemoController {

    @Value("${accessKey}")
    private String accessKey;

    @Value("${secretKey}")
    private String secretKey;

    /*Demo临时存储，重启服务后清空。 */
    private HashMap<String, Object> storage = new HashMap<String, Object>();

    /*Step1:分片上传初始化 */
    @GetMapping("/uploader/init")
    Object initUpload(@RequestParam Map<String, String> initParam) {
        Auth auth = new Auth(accessKey, secretKey);
        ResourceService resourceService = new ResourceService(auth, new HashMap<String, String>());

        HashMap params = new HashMap();
        params.put("name", initParam.get("name"));
        params.put("extno", initParam.get("extno"));

        Map token = null;
        try {
            token = resourceService.startUpload(params);
        } catch (Exception e) {
            //处理对应的异常
            e.printStackTrace();
        }

        JSONPObject jsonpObject = new JSONPObject(initParam.get("callback"), token);
        return jsonpObject;
    }

    /*Step2:分片上传完成 */
    @GetMapping("/uploader/finish")
    Object finishUpload(@RequestParam Map<String, String> finishParam) {
        Auth auth = new Auth(accessKey, secretKey);
        String callback = finishParam.get("callback");

        ResourceService resourceService = new ResourceService(auth, new HashMap<String, String>());
        Map resource = null;
        try {
            resource = resourceService.finishUpload(finishParam.get("no"));
            storage.put(resource.get("no").toString(), resource);
        } catch (Exception e) {
            //处理对应的异常
            e.printStackTrace();
        }

        JSONPObject jsonpObject = new JSONPObject(callback, resource);
        return jsonpObject;
    }

    /*Step3: 获取上传文件列表 */
    @GetMapping("/files")
    Object fileList() {
        //业务系统中维护上传文件列表，也可以通过接口获取完整列表信息
        //http://docs.qiqiuyun.com/v2/resource/manage.html#%E5%88%97%E8%A1%A8%E6%9F%A5%E8%AF%A2
        return new ArrayList<Object>(storage.values());
    }

    /*Step4: 获取播放token */
    @GetMapping("/player/token")
    Object playToken(@RequestParam Map<String, String> playParam) {
        Auth auth = new Auth(accessKey, secretKey);
        PlayService playService = new PlayService(auth, new HashMap<String, String>());

        String no = playParam.get("no");
        String token = playService.makePlayToken(no, 60, new HashMap<String, Object>());
        return token;
    }

    /*Step5: 下载文件 */
    @GetMapping("/file/download")
    Object fileDownload(@RequestParam Map<String, String> downloadParam) {
        Auth auth = new Auth(accessKey, secretKey);
        ResourceService resourceService = new ResourceService(auth, new HashMap<String, String>());
        String no = downloadParam.get("no");
        Map downloadInfo = null;
        try {
            downloadInfo = resourceService.getDownloadUrl(no);
        } catch (Exception e) {
            //处理对应的异常
            e.printStackTrace();
        }
        return downloadInfo;
    }
}
