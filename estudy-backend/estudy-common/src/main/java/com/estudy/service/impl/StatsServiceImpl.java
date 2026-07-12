package com.estudy.service.impl;

import com.estudy.component.RedisComponent;
import com.estudy.entity.po.ExamRecord;
import com.estudy.entity.po.Paper;
import com.estudy.entity.po.Question;
import com.estudy.entity.po.User;
import com.estudy.entity.query.QueryParams;
import com.estudy.entity.vo.StatsData;
import com.estudy.mappers.ExamRecordMapper;
import com.estudy.mappers.PaperMapper;
import com.estudy.mappers.QuestionMapper;
import com.estudy.mappers.UserMapper;
import com.estudy.service.StatsService;
import com.estudy.utils.DateUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserMapper<User, QueryParams> userMapper;

    @Resource
    private PaperMapper<Paper, QueryParams> paperMapper;

    @Resource
    private ExamRecordMapper<ExamRecord, QueryParams> examRecordMapper;

    @Resource
    private QuestionMapper<Question, QueryParams> questionMapper;

    @Override
    public StatsData getStatsData(String userId) {
        QueryParams params = new QueryParams();
        params.setConditions("e.user_id", userId);
        Integer paperCount = paperMapper.selectCount(new QueryParams());
        Integer totalExams = examRecordMapper.selectCount(params);
        Integer continuousSignCount = redisComponent.getContinuousSignCount(userId);
        StatsData statsData = new StatsData();
        statsData.setPaperCount(paperCount);
        statsData.setTotalExams(totalExams);
        statsData.setContinuousSignCount(continuousSignCount);
        return statsData;
    }

    @Override
    public Map<String, Integer> getOverviewData() {
        QueryParams params = new QueryParams();
        Integer userCount = userMapper.selectCount(params);
        Integer paperCount = paperMapper.selectCount(params);
        Integer questionCount = questionMapper.selectCount(params);
        String time = DateUtils.format(new Date(), "yyyy-MM-dd");
        params.setStartTime(time);
        params.setEndTime(time);
        Integer todayExamCount = examRecordMapper.selectCount(params);
        Map<String, Integer> result = new HashMap<>();
        result.put("userCount", userCount);
        result.put("paperCount", paperCount);
        result.put("questionCount", questionCount);
        result.put("todayExamCount", todayExamCount);
        return result;
    }

    @Override
    public Map<String, Long> getWeekExamData() {
        QueryParams params = new QueryParams();
        List<String> dateList = DateUtils.getBeforeDates(7);
        params.setStartTime(dateList.get(0));
        params.setEndTime(dateList.get(dateList.size() - 1));
        params.setOrderBy("start_time asc");
        List<ExamRecord> examRecords = examRecordMapper.selectList(params);
        Map<String, Long> dateCountMap = examRecords.stream()
                .collect(Collectors.groupingBy(
                        er -> DateUtils.format(er.getStartTime(), "MM-dd"),
                        Collectors.counting()
                ));
        Map<String, Long> result = dateList.stream()
                .collect(Collectors.toMap(
                        date -> date.substring(5),
                        date -> dateCountMap.getOrDefault(date.substring(5), 0L)
                ));
        return result;
    }

    @Override
    public Map<String, Object> getStatsData() {
        Map<String, Object> result = examRecordMapper.selectStatsCount();
        Long monthActiveUsers = redisComponent.getActiveUserCount(true);
        Long todayActiveUsers = redisComponent.getActiveUserCount(false);
        result.put("monthActiveUsers", monthActiveUsers);
        result.put("todayActiveUsers", todayActiveUsers);
        Map<String, Object> pointsCount = userMapper.selectPointsCount();
        result.putAll(pointsCount);
        return result;
    }
}
