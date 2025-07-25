package com.xik.aibookkeeping.server.service.impl;



import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.xik.aibookkeeping.common.annotation.OperationLog;
import com.xik.aibookkeeping.common.constant.*;
import com.xik.aibookkeeping.common.context.BaseContext;
import com.xik.aibookkeeping.common.enumeration.OperationType;
import com.xik.aibookkeeping.common.exception.AdminException;
import com.xik.aibookkeeping.common.exception.DecryptException;
import com.xik.aibookkeeping.common.exception.LoginFailedException;
import com.xik.aibookkeeping.common.exception.UserException;
import com.xik.aibookkeeping.common.properties.JwtProperties;
import com.xik.aibookkeeping.common.properties.TencentSmsProperties;
import com.xik.aibookkeeping.common.properties.WeChatProperties;
import com.xik.aibookkeeping.common.utils.HttpClientUtil;
import com.xik.aibookkeeping.common.utils.JwtUtil;
import com.xik.aibookkeeping.common.utils.WxUtils;
import com.xik.aibookkeeping.pojo.dto.*;
import com.xik.aibookkeeping.pojo.entity.Admin;
import com.xik.aibookkeeping.pojo.entity.User;
import com.xik.aibookkeeping.pojo.vo.DecryptVO;
import com.xik.aibookkeeping.pojo.vo.UserAccountLoginVO;
import com.xik.aibookkeeping.pojo.vo.UserLoginVO;
import com.xik.aibookkeeping.pojo.vo.UserPhoneLoginVO;
import com.xik.aibookkeeping.server.mapper.UserMapper;
import com.xik.aibookkeeping.server.rabbitmq.producer.PointsProducer;
import com.xik.aibookkeeping.server.service.IPointsService;
import com.xik.aibookkeeping.server.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author panxikai
 * @since 2025-06-27
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private WeChatProperties weChatProperties;

    @Resource
    private PointsProducer pointsProducer;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TencentSmsProperties tencentSmsProperties;

    private static final Duration EXPIRE_DURATION = Duration.ofMinutes(15);



    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO wxLogin(UserLoginDTO userLoginDTO) {
        log.info("userCode:{}",userLoginDTO.getCode());
        String code = userLoginDTO.getCode();
        String encryptedData = userLoginDTO.getEncryptedData();
        String iv = userLoginDTO.getIv();
        DecryptVO decryptVO = getDecrypt(code);
        String openid = decryptVO.getOpenid();
        String sessionKey = decryptVO.getSessionKey();
        log.info("openid:{}",openid);

        //解密用户数据
        JSONObject userInfo = WxUtils.decryptUserInfo(encryptedData, sessionKey, iv);
        if (userInfo == null){
            throw new DecryptException(MessageConstant.DECRYPE_ERROR);  //用户信息解密失败
        }
        //获取用户信息
        String nickname = "小嘻_"+ randomAlphaNum(7);
        String avatar = ImageConstant.IMAGE_CONSTANT;
        log.info("用户信息：昵称：{}，头像：{}",nickname,avatar);
        //判断openid是否为空
        if (openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_ERROR);
        }
        //判断是否新用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getOpenid, openid);
        User user = this.getOne(queryWrapper);
        if (user == null){
            //是新用户
            user = User.builder()
                    .openid(openid)
                    .nickname(nickname)
                    .avatar(avatar)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            this.save(user);
            // 首次登陆 异步将用户信息保存到积分表
            pointsProducer.initUser(user.getId());

        }
        //为微信用户生成jwt令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        //生成token
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        return UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .nickname(nickname)
                .avatar(avatar)
                .build();
    }

    @Override
    public User getCurrentUser() {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
            queryWrapper.eq(User::getId, currentUserId).eq(User::getIsDeleted, 0);
            return this.baseMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            throw new UserException(MessageConstant.USER_FOUND_ERR);
        }

    }

    @Override
    public void logout() {

    }

    @Override
    public void updateUser(UserDTO userDTO) {
        try {
            Long userId = BaseContext.getCurrentId();
            User user = new User();
            BeanUtils.copyProperties(userDTO, user);
            user.setUpdateTime(LocalDateTime.now());
            user.setId(userId);
            // 查询账号是否存在
            if (userDTO.getAccount() != null) {
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getAccount, userDTO.getAccount()).eq(User::getIsDeleted, 0).eq(User::getStatus, 1);
                User user1 = this.getOne(queryWrapper);
                if (user1 != null){
                    throw new UserException("账号已存在");
                }
            }
            //查询密码
            if (userDTO.getPassword() != null) {
                user.setPassword(DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes()));
            }
            this.updateById(user);
        } catch (Exception e) {
            throw new UserException(MessageConstant.USER_UPDATE_ERR);
        }

    }

    @Override
    public User getByUserId(Long id) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
            queryWrapper.eq(User::getId, id).eq(User::getIsDeleted, 0);
            return this.getOne(queryWrapper);
        } catch (Exception e) {
            throw new UserException(MessageConstant.USER_FOUND_ERR);
        }
    }

    @Override
    public Page<User> pageUser(UserPageQueryDTO userPageQueryDTO) {
        try {
            Page<User> pageQuery = Page.of(userPageQueryDTO.getPage(), userPageQueryDTO.getPageSize());
            // 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            // 添加模糊查询
            if (StringUtils.isNoneBlank(userPageQueryDTO.getNickname())) {
                queryWrapper.like(User::getNickname, userPageQueryDTO.getNickname());
            }
            // 查询未被删除的 并且按照时间排序
            queryWrapper.eq(User::getIsDeleted, 0).orderByDesc(User::getCreateTime);

            return this.page(pageQuery, queryWrapper);
        } catch (Exception e) {
            throw new AdminException(MessageConstant.USER_PAGE_QUERY_ERR + e.getMessage());
        }
    }

    @Override
    public long getNumber() {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getIsDeleted, 0);
            return this.count(queryWrapper);
        } catch (Exception e) {
            throw new UserException(MessageConstant.USER_PAGE_QUERY_ERR + e.getMessage());
        }
    }

    /**
     * 修改用户权限 amdin/user
     * @param role
     */
    @Override
    public void updateRole(String role, Long id) {
        try {
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, id).set(User::getRole, role);
            this.update(updateWrapper);
        } catch (Exception e) {
            throw new UserException(MessageConstant.USER_UPDATE_ERR);
        }
    }

    /**
     * 用户手机登陆
     * @param userPhoneLoginDTO
     * @return
     */
    @Override
    public UserPhoneLoginVO phoneLogin(UserPhoneLoginDTO userPhoneLoginDTO) {
        try {
            // 校验手机号
            if (!isValidPhoneNumber(userPhoneLoginDTO.getPhone())) {
                throw new UserException("手机号码格式不正确");
            }
            // 验证码
            String key = RedisKeyConstant.SMS_CODE_KEY + userPhoneLoginDTO.getPhone();
            String code = stringRedisTemplate.opsForValue().get(key);
            if (!code.equals(userPhoneLoginDTO.getCode())) {
                throw new UserException("验证码错误");
            }

            //获取用户信息
            String nickname = "小嘻_"+ randomAlphaNum(7);
            String avatar = ImageConstant.IMAGE_CONSTANT;
            log.info("用户信息：昵称：{}，头像：{}",nickname,avatar);

            //判断是否新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, userPhoneLoginDTO.getPhone());
            User user = this.getOne(queryWrapper);
            if (user == null){
                //是新用户
                user = User.builder()
                        .phone(userPhoneLoginDTO.getPhone())
                        .nickname(nickname)
                        .avatar(avatar)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                this.save(user);
                // 首次登陆 异步将用户信息保存到积分表
                pointsProducer.initUser(user.getId());

            }
            //为用户生成jwt令牌
            Map<String,Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID,user.getId());
            //生成token
            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
            return UserPhoneLoginVO.builder()
                    .id(user.getId())
                    .token(token)
                    .nickname(nickname)
                    .avatar(avatar)
                    .build();
        } catch (Exception e) {
            throw new UserException("登陆失败");
        }
    }

    /**
     * 发送验证码
     * @param userCodeLoginDTO
     */
    @Override
    public SendSmsResponse sentCode(UserCodeLoginDTO userCodeLoginDTO) {
        try {
            // 校验手机号
            if (!isValidPhoneNumber(userCodeLoginDTO.getPhone())) {
                throw new UserException("手机号码格式不正确");
            }
            // 生成6位验证码
            String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
            // 1. 发送短信
            Credential cred = new Credential(tencentSmsProperties.getSecretId(), tencentSmsProperties.getSecretKey());
            SmsClient client = new SmsClient(cred, "ap-guangzhou");

            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppid(tencentSmsProperties.getSdkAppId());
            req.setSign(tencentSmsProperties.getSignName());
            req.setTemplateID(tencentSmsProperties.getTemplateId());
            req.setPhoneNumberSet(new String[]{"+86" + userCodeLoginDTO.getPhone()});
            req.setTemplateParamSet(new String[]{code, String.valueOf(EXPIRE_DURATION)});
            SendSmsResponse res = client.SendSms(req);
            // 存入redis 15分钟有效期
            String key = RedisKeyConstant.SMS_CODE_KEY + userCodeLoginDTO.getPhone();
            stringRedisTemplate.opsForValue().set(key, code, EXPIRE_DURATION);
            log.info("用户：{} 验证码：{}",userCodeLoginDTO.getPhone(),code);
            return res;
        } catch(Exception e) {
            throw new UserException("验证码发送失败");
        }
    }

    /**
     * 注册
     * @param userRegisterDTO
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        try {
            if (userRegisterDTO.getAccount() == null || userRegisterDTO.getPassword() == null) {
                throw new UserException("账号或密码不能为空");
            }
            if (!isValidAccountOrPassword(userRegisterDTO.getAccount()) || !isValidAccountOrPassword(userRegisterDTO.getPassword())) {
                throw new UserException("账号和密码必须为字母或数字或特殊字符");
            }
            if (userRegisterDTO.getAccount().length() < 6 || userRegisterDTO.getAccount().length() > 20) {
                throw new UserException("账号长度必须在6~20");
            }
            if (userRegisterDTO.getPassword().length() < 6 ||  userRegisterDTO.getPassword().length() > 20) {
                throw new UserException("密码长度必须在6~20");
            }
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(User::getIsDeleted, 0)
                    .eq(User::getStatus,1)
                    .eq(User::getAccount, userRegisterDTO.getAccount());
            User user = this.getOne(queryWrapper);
            if (user != null){
                throw new UserException("账号已存在");
            }
            //获取用户信息
            String nickname = "小嘻_"+ randomAlphaNum(7);
            String avatar = ImageConstant.IMAGE_CONSTANT;
            log.info("用户信息：昵称：{}，头像：{}",nickname,avatar);

            user = User.builder()
                    .account(userRegisterDTO.getAccount())
                    .password(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()))
                    .nickname(nickname)
                    .avatar(avatar)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            this.save(user);
            // 首次登陆 异步将用户信息保存到积分表
            pointsProducer.initUser(user.getId());

        } catch (Exception e) {
            throw new UserException("注册失败" + e.getMessage());
        }
    }

    /**
     * 用户账号登录
     * @param userAccountLoginDTO
     * @return
     */
    @Override
    public UserAccountLoginVO accountLogin(UserAccountLoginDTO userAccountLoginDTO) {
        try {
            if (userAccountLoginDTO.getAccount() == null || userAccountLoginDTO.getPassword() == null) {
                throw new UserException("账号或密码不能为空");
            }
            if (!isValidAccountOrPassword(userAccountLoginDTO.getAccount()) || !isValidAccountOrPassword(userAccountLoginDTO.getPassword())) {
                throw new UserException("账号和密码必须为字母或数字或特殊字符");
            }
            String account = userAccountLoginDTO.getAccount();
            String password = DigestUtils.md5DigestAsHex(userAccountLoginDTO.getPassword().getBytes());
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getAccount, account).eq(User::getIsDeleted, 0).eq(User::getStatus,1);
            User user = this.getOne(queryWrapper);
            if (user == null){
                throw new UserException("账号不存在，请先注册");
            }
            if (!password.equals(user.getPassword())) {
                throw new UserException("密码错误");
            }
            // 颁发token
            //为用户生成jwt令牌
            Map<String,Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID,user.getId());
            //生成token
            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
            return UserAccountLoginVO.builder()
                    .id(user.getId())
                    .token(token)
                    .account(user.getAccount())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .build();
        } catch (Exception e) {
            throw new UserException("登录失败" + e.getMessage());
        }
    }


    //调用微信服务接口获取openid和session_key
    private DecryptVO getDecrypt(String code) {
        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        log.info("Appid:{}",weChatProperties.getAppid());

        map.put("secret",weChatProperties.getSecret());
        log.info("secret:{}",weChatProperties.getSecret());

        map.put("js_code",code);
        log.info("js_code:{}",code);

        map.put("grant_type","authorization_code");

        String json = HttpClientUtil.doGet(LoginConstant.WX_LOGIN,map);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        String sessionKey = jsonObject.getString("session_key");
        DecryptVO decryptVO = DecryptVO.builder()
                .openid(openid)
                .sessionKey(sessionKey)
                .build();
        return decryptVO;
    }

    /**
     * 生成随机昵称
     * @param len
     * @return
     */
    private static String randomAlphaNum(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public boolean isValidPhoneNumber(String phone) {
        // 国内手机号段（简化版），以 13、14、15、16、17、18、19 开头，共 11 位
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    public boolean isValidAccountOrPassword(String input) {
        return input != null && input.matches("^[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$");
    }




}
