package com.estudy.service.impl;

import com.estudy.entity.enums.PageSize;
import com.estudy.entity.enums.ResultCode;
import com.estudy.entity.enums.TypeEnum;
import com.estudy.entity.po.*;
import com.estudy.entity.query.QueryParams;
import com.estudy.entity.query.SimplePage;
import com.estudy.entity.vo.ExamData;
import com.estudy.entity.vo.ExamReport;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.exception.BusinessException;
import com.estudy.mappers.AnswerDetailMapper;
import com.estudy.mappers.ExamRecordMapper;
import com.estudy.mappers.PaperMapper;
import com.estudy.mappers.QuestionMapper;
import com.estudy.service.ExamService;
import com.estudy.utils.StrUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {

    @Resource
    private PaperMapper<Paper, QueryParams> paperMapper;

    @Resource
    private QuestionMapper<Question, QueryParams> questionMapper;

    @Resource
    private ExamRecordMapper<ExamRecord, QueryParams> examRecordMapper;

    @Resource
    private AnswerDetailMapper<AnswerDetail, QueryParams> answerDetailMapper;

    @Override
    public List<ExamReport> getRecentExam(String userId) {
        QueryParams params = new QueryParams();
        params.setConditions("e.user_id", userId);
        return examRecordMapper.selectRecordsByParams(params, null);
    }

    @Override
    public PaginationResult<ExamReport> getExamRecord(String userId, Integer pageNo) {
        QueryParams params = new QueryParams();
        params.setConditions("e.user_id", userId);
        int count = examRecordMapper.selectCount(params);
        SimplePage page = new SimplePage(pageNo, count, PageSize.SIZE10.getSize());
        List<ExamReport> examReports = examRecordMapper.selectRecordsByParams(params, page);
        PaginationResult<ExamReport> result = new PaginationResult<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), examReports);
        return result;
    }

    @Override
    public ExamReport getRecordInfo(Integer recordId) {
        ExamReport examReport = examRecordMapper.selectRecordById(recordId);
        if (examReport == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        List<AnswerDetail> answerDetails = answerDetailMapper.selectAnswerDetails(recordId);
        examReport.setAnswerDetails(answerDetails);
        return examReport;
    }

    @Override
    public ExamData getExamQuestions(Integer paperId, Boolean isExam) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        QueryParams params = new QueryParams();
        params.setConditions("paper_id", paperId);
        List<Question> questions = questionMapper.selectList(params);
        if (isExam) {
            questions.forEach(q -> {
                q.setAnswer(null);
                q.setAnalysis(null);
            });
        } else {
            Collections.shuffle(questions);
        }
        ExamData result = new ExamData();
        result.setExamInfo(paper);
        result.setQuestions(questions);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer submitExam(Integer paperId, String userId, Date startTime, Date endTime, List<String> userAnswers) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        QueryParams params = new QueryParams();
        params.setConditions("paper_id", paperId);
        List<Question> questions = questionMapper.selectList(params);
        List<AnswerDetail> answerRecords = new ArrayList<>();
        ExamRecord examRecord = new ExamRecord();
        examRecord.setUserId(userId);
        examRecord.setPaperId(paperId);
        examRecord.setStartTime(startTime);
        long seconds = (endTime.getTime() - startTime.getTime()) / 1000;
        examRecord.setDuration(seconds);
        // 验证答案，计算分数
        Integer score = 0;
        Integer isCorrect = null;
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String rightAnswer = question.getAnswer();
            String userAnswer = userAnswers.get(i).replace("-", ",");
            isCorrect = 0;
            if (rightAnswer.equals(userAnswer)) {
                isCorrect = 1;
                // 答案正确，增加总分
                score += TypeEnum.getScoreByType(question.getType());
            }
            // 每题作答记录
            AnswerDetail detail = new AnswerDetail();
            detail.setQuestionId(question.getQuestionId());
            detail.setUserAnswer(userAnswer);
            detail.setIsCorrect(isCorrect);
            answerRecords.add(detail);
        }
        examRecord.setScore(score);
        // 保存考试记录
        examRecordMapper.insert(examRecord);
        Integer recordId = examRecord.getRecordId();
        answerRecords.forEach(ar -> {
            ar.setRecordId(recordId);
        });
        answerDetailMapper.insertBatch(answerRecords);
        return recordId;
    }

    @Override
    public PaginationResult<ExamReport> getExamRecords(String userId, Integer paperId, Integer pageNo, Integer pageSize) {
        QueryParams params = new QueryParams();
        if (!StrUtils.isEmpty(userId)) {
            params.setConditions("e.user_id", userId);
        }
        if (paperId != null) {
            params.setConditions("e.paper_id", paperId);
        }
        int count = examRecordMapper.selectCount(params);
        SimplePage page = new SimplePage(pageNo, count, pageSize);
        params.setSimplePage(page);
        List<ExamReport> papers = examRecordMapper.selectRecordsByParams(params, page);
        PaginationResult<ExamReport> result = new PaginationResult<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), papers);
        return result;
    }

}
