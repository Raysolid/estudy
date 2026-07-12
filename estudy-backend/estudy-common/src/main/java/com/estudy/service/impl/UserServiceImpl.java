package com.estudy.service.impl;

import com.estudy.component.RedisComponent;
import com.estudy.config.AppConfig;
import com.estudy.entity.constants.Constants;
import com.estudy.entity.dto.SettingDto;
import com.estudy.entity.dto.UserDto;
import com.estudy.entity.dto.UserToken;
import com.estudy.entity.enums.ResultCode;
import com.estudy.entity.po.User;
import com.estudy.entity.query.QueryParams;
import com.estudy.entity.query.SimplePage;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.exception.BusinessException;
import com.estudy.mappers.UserMapper;
import com.estudy.service.UserService;
import com.estudy.utils.CopyUtils;
import com.estudy.utils.MailUtils;
import com.estudy.utils.StrUtils;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MailUtils mailUtils;

    @Resource
    private UserMapper<User, QueryParams> userMapper;

    @Override
    public String checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        redisComponent.saveCheckCode(code);
        return captcha.toBase64();
    }

    @Override
    public void emailCode(String email) {
        try {
            String code = StrUtils.randomNumber(6);
            String subject = "账号验证";
            String text = String.format("您的验证码是：%s（有效期10分钟，请勿泄露给陌生人）", code);
            mailUtils.sendSimpleMail(email, subject, text);
            redisComponent.saveEmailCode(email ,code);
        } catch (MessagingException e) {
            throw new BusinessException("验证码发送失败");
        }
    }

    @Override
    public void register(String email, String password, String emailCode) {
        try {
            // 验证码错误
            if (!emailCode.equals(redisComponent.getEmailCode(email))) {
                throw new BusinessException("验证码错误");
            }
            createAccount(email, password, null, "", 100);
        } finally {
            // 重置验证码
            redisComponent.deleteEmailCode(email);
        }
    }

    @Override
    public UserDto loginByPassword(String email, String password) {
        User user = userMapper.selectByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new BusinessException("账号或密码错误");
        }
        return saveToken(user);
    }

    @Override
    public UserDto loginByEmailCode(String email, String emailCode) {
        try {
            // 验证码错误
            if (!emailCode.equals(redisComponent.getEmailCode(email))) {
                throw new BusinessException("验证码错误");
            }
            User user = userMapper.selectByEmail(email);
            if (user == null) {
                throw new BusinessException("账号不存在");
            }
            return saveToken(user);
        } finally {
            // 重置验证码
            redisComponent.deleteEmailCode(email);
        }
    }

    @Override
    public UserDto autoLogin(String token) {
        UserToken userToken = redisComponent.getUserToken(token);
        if (userToken == null) {
            throw new BusinessException(ResultCode.CODE_401);
        }
        User user = userMapper.selectById(userToken.getUserId());
        return saveToken(user);
    }

    @Override
    public void logout(String token) {
        redisComponent.cleanUserToken(token);
    }

    @Override
    public UserDto getUserInfo(String userId) {
        User user = userMapper.selectById(userId);
        return CopyUtils.copy(user, UserDto.class);
    }

    @Override
    public void changePassword(String token, String password, String emailCode) {
        UserToken userToken = redisComponent.getUserToken(token);
        String email = userToken.getEmail();
        try {
            // 验证码错误
            if (!emailCode.equals(redisComponent.getEmailCode(email))) {
                throw new BusinessException("验证码错误");
            }
            if (!password.matches(Constants.REGEX_PASSWORD)) {
                throw new BusinessException("密码不符合要求");
            }
            User user = userMapper.selectById(userToken.getUserId());
            if (password.equals(user.getPassword())) {
                throw new BusinessException("新密码不能与旧密码一致");
            }
            user.setPassword(password);
            userMapper.updateById(user);
            logout(token);
        } finally {
            // 重置验证码
            redisComponent.deleteEmailCode(email);
        }
    }

    @Override
    public void updateUserInfo(String userId, String nickname, String preference) {
        User user = new User();
        user.setUserId(userId);
        user.setNickname(nickname);
        user.setPreference(preference);
        userMapper.updateById(user);
    }

    @Override
    public void sign(String userId) {
        userMapper.updateUserPoints(userId, 10);
        redisComponent.sign(userId);
    }

    @Override
    public String loginAdmin(String username, String password, String checkCode) {
        try {
            SettingDto setting = redisComponent.getSetting();
            String adminPassword;
            if (setting == null || setting.getAdminPassword() == null) {
                adminPassword = appConfig.getAdminPassword();
            } else {
                adminPassword = setting.getAdminPassword();
            }
            // 账号或密码错误
            if (!username.equals(appConfig.getAdminAccount()) || !password.equals(StrUtils.md5(adminPassword))) {
                throw new BusinessException("账号或密码错误");
            }
            // 验证码错误
            if (!checkCode.equals(redisComponent.getCheckCode())) {
                throw new BusinessException("验证码错误");
            }
            return redisComponent.saveAdminToken(username);
        } finally {
            // 重置验证码
            redisComponent.deleteCheckCode();
        }
    }

    @Override
    public PaginationResult<User> getUserList(String userId, String email, String nickname, Integer pageNo, Integer pageSize) {
        QueryParams params = new QueryParams();
        if (!StrUtils.isEmpty(userId)) {
            params.setConditions("user_id", userId);
        }
        if (!StrUtils.isEmpty(email)) {
            params.setConditions("email", email);
        }
        if (!StrUtils.isEmpty(nickname)) {
            params.setFuzzyConditions("nickname", nickname);
        }
        int count = userMapper.selectCount(params);
        SimplePage page = new SimplePage(pageNo, count, pageSize);
        params.setSimplePage(page);
        List<User> papers = userMapper.selectList(params);
        PaginationResult<User> result = new PaginationResult<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), papers);
        return result;
    }

    @Override
    public void editUser(User user, Boolean isAdd) {
        if (StrUtils.isEmpty(user.getEmail()) || StrUtils.isEmpty(user.getNickname())) {
            throw new BusinessException("必填信息不能为空");
        }
        if (!isAdd) {   // 修改
            User bean = userMapper.selectById(user.getUserId());
            if (bean == null) {
                throw new BusinessException(ResultCode.CODE_400);
            }
            if (bean.getPassword().equals(user.getPassword())) {
                throw new BusinessException("新密码与旧密码不能一致");
            }
            if (user.getPassword() == null) {   // 不修改密码
                user.setPassword(bean.getPassword());
            }
            user.setCreateTime(bean.getCreateTime());
            userMapper.updateById(user);
        } else {    // 新增
            if (StrUtils.isEmpty(user.getPassword())) {
                throw new BusinessException("密码不能为空");
            }
            createAccount(user.getEmail(), user.getPassword(), user.getNickname(), user.getPreference(), user.getPoints());
        }
    }

    @Override
    public void deleteUser(String userId) {
        userMapper.deleteById(userId);
    }

    /**
     * 创建账号
     */
    private void createAccount(String email, String password, String nickname, String preference, Integer points) {
        // 检查邮箱是否被注册
        User user = userMapper.selectByEmail(email);
        if (user != null) {
            throw new BusinessException("当前邮箱已注册");
        }
        if (!password.matches(Constants.REGEX_PASSWORD)) {
            throw new BusinessException("密码不符合要求");
        }
        // 新增用户
        String userId = "U" + StrUtils.randomNumber(11);
        user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        if (StrUtils.isEmpty(nickname)) {
            user.setNickname("user-" + StrUtils.randomString(8));
        } else {
            user.setNickname(nickname);
        }
        user.setPassword(StrUtils.md5(password));
        user.setPreference(preference);
        user.setPoints(points);
        user.setCreateTime(new Date());
        userMapper.insert(user);
    }

    /**
     * 保存登录状态
     */
    private UserDto saveToken(User user) {
        // 保存登录信息到redis
        UserToken userToken = getUserToken(user);
        redisComponent.saveUserToken(userToken);
        // 封装数据
        UserDto userDto = CopyUtils.copy(user, UserDto.class);
        userDto.setToken(userToken.getToken());
        return userDto;
    }

    /**
     * 获取token
     */
    private UserToken getUserToken(User user) {
        UserToken userToken = new UserToken();
        userToken.setUserId(user.getUserId());
        userToken.setEmail(user.getEmail());
        userToken.setNickName(user.getNickname());
        // 生成token
        String token = StrUtils.md5(user.getUserId() + StrUtils.randomString(20));
        userToken.setToken(token);
        return userToken;
    }
}
