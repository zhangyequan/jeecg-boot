package org.jeecg.modules.visit.test;


import com.alibaba.fastjson.JSONObject;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.RestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 系统用户单元测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = JeecgSystemApplication.class)
public class VisitTest {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 测试地址：实际使用时替换成你自己的地址
     */
    private final String BASE_URL = "http://localhost:8080/jeecg-boot/visit/visitinfo/";

    // 请实际使用时替换成你自己的用户名和密码
    private final String USERNAME = "admin";
    private final String PASSWORD = "123456";

    /**
     * 测试用例：上传
     */
    @Test
    public void testUpload() {
        // 用户Token
        String token = this.getToken();
        // 请求地址
        String url = BASE_URL + "uploadinfo" ;
        // 请求 Header （用于传递Token）
        HttpHeaders headers = this.getHeaders(token);
        // 请求方式是 POST 代表提交新增数据
        HttpMethod method = HttpMethod.POST;

        System.out.println("请求地址：" + url);
        System.out.println("请求方式：" + method);
        System.out.println("请求Token：" + token);

        JSONObject params = new JSONObject();
        params.put("visitedname", "短发短发");
        params.put("visitedpnum","454544");
        params.put("visitedevent","如同让人");
        params.put("visittime","2021-05-31");
        params.put("lefttime","2021-05-31");
        params.put("visitorname","共和国好过");
        params.put("visitorpnum","45454");
        params.put("identitynum","34343");
        params.put("platenum","34343");
        //params.put("file","");

        System.out.println("请求参数：" + params.toJSONString());

        // 利用 RestUtil 请求该url
        ResponseEntity<JSONObject> result = RestUtil.request(url, method, headers, null, params.toJSONString(), JSONObject.class);
        if (result != null && result.getBody() != null) {
            System.out.println("返回结果：" + result.getBody().toJSONString());
        } else {
            System.out.println("查询失败");
        }
    }

    /**
     * 测试用例：查询列表
     */
    @Test
    public void testlist() {
        // 用户Token
        String token = this.getToken();
        // 请求地址
        String url = BASE_URL + "list" ;
        // 请求 Header （用于传递Token）
        HttpHeaders headers = this.getHeaders(token);
        // 请求方式是 POST 代表提交新增数据
        HttpMethod method = HttpMethod.POST;

        System.out.println("请求地址：" + url);
        System.out.println("请求方式：" + method);
        System.out.println("请求Token：" + token);

        JSONObject params = new JSONObject();
        params.put("status", "1");
        params.put("pageNo","1");
        params.put("pageSize","10");

        System.out.println("请求参数：" + params.toJSONString());

        // 利用 RestUtil 请求该url
        ResponseEntity<JSONObject> result = RestUtil.request(url, method, headers, null, params.toJSONString(), JSONObject.class);
        if (result != null && result.getBody() != null) {
            System.out.println("返回结果：" + result.getBody().toJSONString());
        } else {
            System.out.println("查询失败");
        }
    }

    /**
     * 测试用例：审批
     */
    @Test
    public void testApprove() {
        // 用户Token
        String token = this.getToken();
        // 请求地址
        String url = BASE_URL + "approve" ;
        // 请求 Header （用于传递Token）
        HttpHeaders headers = this.getHeaders(token);
        // 请求方式是 POST 代表提交新增数据
        HttpMethod method = HttpMethod.POST;

        System.out.println("请求地址：" + url);
        System.out.println("请求方式：" + method);
        System.out.println("请求Token：" + token);

        JSONObject params = new JSONObject();
        params.put("id", "1399928374978134017");

        System.out.println("请求参数：" + params.toJSONString());

        // 利用 RestUtil 请求该url
        ResponseEntity<JSONObject> result = RestUtil.request(url, method, headers, null, params.toJSONString(), JSONObject.class);
        if (result != null && result.getBody() != null) {
            System.out.println("返回结果：" + result.getBody().toJSONString());
        } else {
            System.out.println("查询失败");
        }
    }

    private String getToken() {
        String token = JwtUtil.sign(USERNAME, PASSWORD);
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, 60);
        return token;
    }

    private HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        String mediaType = MediaType.APPLICATION_JSON_UTF8_VALUE;
        headers.setContentType(MediaType.parseMediaType(mediaType));
        headers.set("Accept", mediaType);
        headers.set("X-Access-Token", token);
        return headers;
    }
}
