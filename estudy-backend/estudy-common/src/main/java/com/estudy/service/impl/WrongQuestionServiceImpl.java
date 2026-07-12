package com.estudy.service.impl;

import com.estudy.entity.enums.PageSize;
import com.estudy.entity.enums.ResultCode;
import com.estudy.entity.po.Question;
import com.estudy.entity.po.WrongQuestion;
import com.estudy.entity.query.QueryParams;
import com.estudy.entity.query.SimplePage;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.entity.vo.WrongQuestionInfo;
import com.estudy.exception.BusinessException;
import com.estudy.mappers.QuestionMapper;
import com.estudy.mappers.WrongQuestionMapper;
import com.estudy.service.WrongQuestionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class WrongQuestionServiceImpl implements WrongQuestionService {

    @Resource
    private QuestionMapper<Question, QueryParams> questionMapper;

    @Resource
    private WrongQuestionMapper<WrongQuestion, QueryParams> wrongQuestionMapper;

    @Override
    public List<WrongQuestionInfo> getRecentWrongQuestions(String userId) {
        return wrongQuestionMapper.selectWrongQuestionList(userId, null, false);
    }

    @Override
    public PaginationResult<WrongQuestionInfo> getWrongQuestions(String userId, Integer pageNo) {
        QueryParams params = new QueryParams();
        params.setConditions("user_id", userId);
        int count = wrongQuestionMapper.selectCount(params);
        SimplePage page = new SimplePage(pageNo, count, PageSize.SIZE10.getSize());
        List<WrongQuestionInfo> wrongQuestions = wrongQuestionMapper.selectWrongQuestionList(userId, page, true);
        PaginationResult<WrongQuestionInfo> result = new PaginationResult<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), wrongQuestions);
        return result;
    }

    @Override
    public WrongQuestionInfo getWrongQuestionById(Integer questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        return wrongQuestionMapper.selectWrongQuestionById(questionId);
    }

    @Override
    public void addWrongQuestion(String userId, Integer questionId, String userAnswer) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        // 作答正确
        if (question.getAnswer().equals(userAnswer)) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        // 已在错题本中
        QueryParams params = new QueryParams();
        params.setConditions("user_id", userId);
        params.setConditions("question_id", questionId);
        Integer count = wrongQuestionMapper.selectCount(params);
        if (count > 0) {
            throw new BusinessException("错题本中已存在当前题目");
        }
        WrongQuestion wrongQuestion = new WrongQuestion();
        wrongQuestion.setUserId(userId);
        wrongQuestion.setQuestionId(questionId);
        wrongQuestion.setUserAnswer(userAnswer);
        wrongQuestion.setWrongTime(new Date());
        wrongQuestionMapper.insert(wrongQuestion);
    }

    @Override
    public void removeWrongQuestion(String userId, Integer wrongId) {
        WrongQuestion wrongQuestion = wrongQuestionMapper.selectById(wrongId);
        if (wrongQuestion == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        QueryParams params = new QueryParams();
        params.setConditions("w.user_id", userId);
        params.setConditions("w.wrong_id", wrongId);
        wrongQuestionMapper.deleteByParam(params);
    }

}
