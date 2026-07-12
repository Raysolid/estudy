package com.estudy.component;

import com.estudy.entity.dto.SettingDto;
import com.estudy.entity.dto.UserToken;
import com.estudy.entity.vo.TaskResult;
import com.estudy.utils.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    /**
     * 验证码
     */
    public void saveCheckCode(String code) {
        redisUtils.setex(CHECK_CODE, code, ONE_MINUTE);
    }
    public String getCheckCode() {
        return (String) redisUtils.get(CHECK_CODE);
    }
    public void deleteCheckCode() {
        redisUtils.delete(CHECK_CODE);
    }

    /**
     * 邮箱验证码
     */
    public void saveEmailCode(String email, String code) {
        redisUtils.setex(EMAIL_CODE + email, code, ONE_MINUTE * 10);
    }
    public String getEmailCode(String email) {
        return (String) redisUtils.get(EMAIL_CODE + email);
    }
    public void deleteEmailCode(String email) {
        redisUtils.delete(EMAIL_CODE + email);
    }

    /**
     * token
     */
    public void saveUserToken(UserToken userToken) {
        redisUtils.setex(KEY_TOKEN + userToken.getToken(), userToken, ONE_DAY * 7);
    }
    public UserToken getUserToken(String token) {
        return (UserToken) redisUtils.get(KEY_TOKEN + token);
    }
    public void cleanUserToken(String token) {
        redisUtils.delete(KEY_TOKEN + token);
    }

    public String saveAdminToken(String username) {
        String token = UUID.randomUUID().toString();
        redisUtils.setex(ADMIN_TOKEN + token, username, ONE_DAY);
        return token;
    }
    public String getAdminToken(String token) {
        return (String) redisUtils.get(ADMIN_TOKEN + token);
    }
    public void cleanAdminToken(String token) {
        redisUtils.delete(ADMIN_TOKEN + token);
    }

    /**
     * 签到
     */
    public void sign(String userId) {
        LocalDateTime now = LocalDateTime.now();
        String key = STUDY_SIGN + userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        int offset = now.getDayOfMonth() - 1;
        redisUtils.setBit(key, offset, true);
        // 计算月底到当天的天数差，作为key的过期时间
        LocalDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long daysRemaining = ChronoUnit.SECONDS.between(now, endOfMonth);
        redisUtils.expire(key, daysRemaining);
    }
    public Integer getContinuousSignCount(String userId) {  // 连续签到天数
        LocalDateTime now = LocalDateTime.now();
        String key = STUDY_SIGN + userId + ":" + now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        int dayOfMonth = now.getDayOfMonth();
        boolean signToday = redisUtils.getBit(key, dayOfMonth - 1);
        int checkDay = signToday ? dayOfMonth : dayOfMonth - 1;
        // 获取本月第1天到当天的签到结果
        List<Long> result = redisUtils.bitField(key, checkDay);
        if (result == null || result.isEmpty() || result.get(0) == null) {
            return 0;
        }
        Long num = result.get(0);
        int count = 0;
        // 从低位（当天）开始向前遍历
        while ((num & 1) == 1) {
            count++;
            num >>>= 1; // 无符号右移
        }
        return count;
    }
    public Map<String, Boolean> getMonthSignInfo(String userId) {   // 当月签到情况
        LocalDateTime date = LocalDateTime.now();
        String key = STUDY_SIGN + userId + ":" + date.format(DateTimeFormatter.ofPattern("yyyyMM"));
        int daysInMonth = date.toLocalDate().lengthOfMonth();
        List<Long> result = redisUtils.bitField(key, daysInMonth);
        Map<String, Boolean> signMap = new HashMap<>();
        if (result != null && !result.isEmpty() && result.get(0) != null) {
            Long num = result.get(0);
            // 从高位（月初）向低位（月末）解析，或按需调整
            for (int i = daysInMonth - 1; i >= 0; i--) {
                String d = date.withDayOfMonth(i + 1).format(DateTimeFormatter.ofPattern("yyyy-M-d"));
                signMap.put(d, (num & 1) == 1);
                num >>>= 1;
            }
        }
        return signMap;
    }

    /**
     * 保存/查询任务进度
     */
    public void saveTask(String taskId, TaskResult taskResult) {
        redisUtils.set(TASK_PROGRESS + taskId, taskResult);
    }
    public TaskResult getTask(String taskId) {
        TaskResult taskResult = (TaskResult) redisUtils.get(TASK_PROGRESS + taskId);
        if (taskResult != null && taskResult.isCompleted()) {
            redisUtils.delete(TASK_PROGRESS + taskId);
        }
        return taskResult;
    }
    public void deleteTask(String taskId) {
        redisUtils.delete(TASK_PROGRESS + taskId);
    }

    /**
     * 系统设置
     */
    public SettingDto getSetting() {
        SettingDto settingDto = (SettingDto) redisUtils.get(SYSTEM_SETTING);
        if (settingDto == null) {
            settingDto = new SettingDto();
        }
        return settingDto;
    }
    public void saveSetting(SettingDto settingDto) {
        redisUtils.set(SYSTEM_SETTING, settingDto);
    }

    /**
     * 月活跃用户数量
     */
    public Long getActiveUserCount(Boolean isMonthly) {
        return isMonthly ? redisUtils.count(STUDY_SIGN) : redisUtils.count(KEY_TOKEN);
    }

    /**
     * 试卷权限
     */
    public boolean authPaper(String userId, Integer paperId) {
        String key = PAPER_PERMISSION + userId;
        if (!redisUtils.keyExists(key)) {
            return false;
        }
        return redisUtils.isSetMember(key, paperId);
    }
    public void unlockPaper(String userId, Integer paperId) {
        redisUtils.addToSet(PAPER_PERMISSION + userId, paperId);
    }

    public static final String KEY_PREFIX = "estudy:";
    public static final Long ONE_MINUTE = 60L;
    public static final Long ONE_DAY = ONE_MINUTE * 60 * 24;

    // 验证码
    public static final String CHECK_CODE = KEY_PREFIX + "checkcode";
    public static final String EMAIL_CODE = KEY_PREFIX + "emailcode:";

    // 用户token
    public static final String KEY_TOKEN = KEY_PREFIX + "token:";
    public static final String ADMIN_TOKEN = KEY_TOKEN + "admin:";

    // 每日学习签到
    public static final String STUDY_SIGN = KEY_PREFIX + "sign:";

    // 异步接口任务进度
    public static final String TASK_PROGRESS = KEY_PREFIX + "task:";

    // 系统设置
    public static final String SYSTEM_SETTING = KEY_PREFIX + "setting";

    // 试卷权限
    public static final String PAPER_PERMISSION = KEY_PREFIX + "paper:";

}
