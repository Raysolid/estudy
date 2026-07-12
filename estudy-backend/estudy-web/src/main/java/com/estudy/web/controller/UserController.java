package com.estudy.web.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.component.RedisComponent;
import com.estudy.entity.dto.UserDto;
import com.estudy.entity.dto.UserToken;
import com.estudy.entity.po.Paper;
import com.estudy.entity.vo.Result;
import com.estudy.entity.vo.StatsData;
import com.estudy.service.PaperService;
import com.estudy.service.StatsService;
import com.estudy.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserService userService;

    @Resource
    private PaperService paperService;

    @Resource
    private StatsService statsService;

    /**
     * 获取用户信息
     */
    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public Result getUserInfo(@RequestHeader(value = "token") String token) {
        UserToken userToken = redisComponent.getUserToken(token);
        UserDto userDto = userService.getUserInfo(userToken.getUserId());
        return Result.success(userDto);
    }

    /**
     * 更新用户信息
     */
    @RequestMapping("/updateUserInfo")
    @GlobalInterceptor
    public Result updateUserInfo(@RequestHeader(value = "token") String token,
                                 @NotEmpty String nickname,
                                 String preference) {
        UserToken userToken = redisComponent.getUserToken(token);
        userService.updateUserInfo(userToken.getUserId(), nickname, preference);
        return Result.success(null);
    }

    /**
     * 获取用户统计数据
     */
    @RequestMapping("/getUserStats")
    @GlobalInterceptor
    public Result getUserStats(@RequestHeader(value = "token") String token) {
        UserToken userToken = redisComponent.getUserToken(token);
        StatsData statsData = statsService.getStatsData(userToken.getUserId());
        return Result.success(statsData);
    }

    /**
     * 根据用户偏好推荐试卷
     */
    @RequestMapping("/recommendPapers")
    @GlobalInterceptor
    public Result recommendPapers(@RequestHeader(value = "token") String token) {
        String userId = redisComponent.getUserToken(token).getUserId();
        List<Paper> papers = paperService.recommendPapers(userId);
        return Result.success(papers);
    }

    /**
     * 打卡
     */
    @RequestMapping("/sign")
    @GlobalInterceptor
    public Result sign(@RequestHeader(value = "token") String token) {
        String userId = redisComponent.getUserToken(token).getUserId();
        userService.sign(userId);
        return Result.success(null);
    }

    /**
     * 获取本月签到情况
     */
    @RequestMapping("/getMonthSignInfo")
    @GlobalInterceptor
    public Result getMonthSignInfo(@RequestHeader(value = "token") String token) {
        String userId = redisComponent.getUserToken(token).getUserId();
        Map<String, Boolean> signInfo = redisComponent.getMonthSignInfo(userId);
        return Result.success(signInfo);
    }

}
