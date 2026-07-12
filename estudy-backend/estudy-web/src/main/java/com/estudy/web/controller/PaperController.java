package com.estudy.web.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.component.RedisComponent;
import com.estudy.entity.po.Paper;
import com.estudy.entity.vo.Result;
import com.estudy.service.PaperService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/paper")
@Validated
public class PaperController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private PaperService paperService;

    /**
     * 检查是否购买试卷
     */
    @RequestMapping("/authPaper")
    @GlobalInterceptor
    public Result authPaper(@RequestHeader(value = "token") String token,
                            @NotNull Integer paperId) {
        String userId = redisComponent.getUserToken(token).getUserId();
        boolean hasPaper = redisComponent.authPaper(userId, paperId);
        return Result.success(hasPaper);
    }

    /**
     * 购买试卷
     */
    @RequestMapping("/unlockPaper")
    @GlobalInterceptor
    public Result unlockPaper(@RequestHeader(value = "token") String token,
                              @NotNull Integer paperId) {
        String userId = redisComponent.getUserToken(token).getUserId();
        paperService.unlockPaper(userId, paperId);
        return Result.success(null);
    }

    /**
     * 获取试卷信息
     */
    @RequestMapping("/getPaperInfo")
    @GlobalInterceptor
    public Result getPaperInfo(@NotNull Integer paperId) {
        Paper paper = paperService.getPaperInfo(paperId);
        return Result.success(paper);
    }

}
