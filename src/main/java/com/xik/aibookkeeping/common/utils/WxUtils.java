package com.xik.aibookkeeping.common.utils;


import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson2.JSONObject;

public class WxUtils {


    private static final String APP_ID = "wxb2fc5eda551c6baf";  // AppID
    private static final String APP_SECRET = "b671e96fe31f26f0d7c08111987b7909";  // AppSecret
    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    static {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据前端传来的 code 获取 session_key 和 openid
     */
    public Map<String, String> getSessionKeyAndOpenId(String code) {
        RestTemplate restTemplate = new RestTemplate();
        String url = WX_LOGIN_URL + "?appid=" + APP_ID + "&secret=" + APP_SECRET + "&js_code=" + code + "&grant_type=authorization_code";

        String response = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.parseObject(response);

        Map<String, String> result = new HashMap<>();
        result.put("session_key", jsonObject.getString("session_key"));
        result.put("openid", jsonObject.getString("openid"));
        return result;
    }

    /**
     * 解密微信用户信息
     */
    public static JSONObject decryptUserInfo(String encryptedData, String sessionKey, String iv) {
        try {
            byte[] dataByte = Base64.decodeBase64(encryptedData);
            byte[] keyByte = Base64.decodeBase64(sessionKey);
            byte[] ivByte = Base64.decodeBase64(iv);

            // 如果密钥长度不足 16，则填充
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }

            // 生成密钥
            Key key = new SecretKeySpec(keyByte, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
            params.init(new IvParameterSpec(ivByte));

            cipher.init(Cipher.DECRYPT_MODE, key, params);
            byte[] resultByte = cipher.doFinal(dataByte);
            if (resultByte != null && resultByte.length > 0) {
                String result = new String(resultByte, StandardCharsets.UTF_8);
                return JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
