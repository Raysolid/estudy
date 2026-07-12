package com.estudy.service.impl;

import com.estudy.component.RedisComponent;
import com.estudy.entity.enums.DifficultyEnum;
import com.estudy.entity.enums.PageSize;
import com.estudy.entity.enums.ResultCode;
import com.estudy.entity.enums.TypeEnum;
import com.estudy.entity.po.Category;
import com.estudy.entity.po.Paper;
import com.estudy.entity.po.Question;
import com.estudy.entity.query.QueryParams;
import com.estudy.entity.query.SimplePage;
import com.estudy.entity.vo.QuestionInfo;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.exception.BusinessException;
import com.estudy.mappers.CategoryMapper;
import com.estudy.mappers.PaperMapper;
import com.estudy.mappers.QuestionMapper;
import com.estudy.service.QuestionService;
import com.estudy.utils.StrUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private CategoryMapper<Category, QueryParams> categoryMapper;

    @Resource
    private PaperMapper<Paper, QueryParams> paperMapper;

    @Resource
    private QuestionMapper<Question, QueryParams> questionMapper;

    @Override
    public List<QuestionInfo> getHotQuestions(Integer categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        QueryParams params = new QueryParams();
        params.setConditions("p.category_id", categoryId);
        params.setOrderBy("answerCount");
        return questionMapper.selectQuestionsByParams(params);
    }

    @Override
    public PaginationResult<QuestionInfo> searchQuestions(String keyword, Integer categoryId, Integer difficulty, String sort, String sortOrder, Integer pageNo) {
        QueryParams params = new QueryParams();
        // 筛选分类
        if (categoryId != null) {
            Category category = categoryMapper.selectById(categoryId);
            if (category == null) {
                throw new BusinessException(ResultCode.CODE_400);
            }
            params.setConditions("p.category_id", categoryId);
        }
        // 筛选难度
        if (difficulty != null) {
            DifficultyEnum difficultyEnum = DifficultyEnum.getDifficultByType(difficulty);
            if (difficultyEnum == null) {
                throw new BusinessException(ResultCode.CODE_400);
            }
        }
        params.setConditions("q.difficulty", difficulty);
        params.setFuzzyConditions("q.content", keyword);
        // 筛选排序
        String orderBy = null;
        switch (sort) {
            case "new" -> orderBy = "q.question_id";
            case "difficulty" -> orderBy = "q.difficulty";
            case "hot" -> orderBy = "answerCount";
        }
        if (orderBy != null) {
            params.setOrderBy(orderBy + " " + sortOrder);
        }
        // 分页
        int count = questionMapper.selectCount(new QueryParams());
        SimplePage page = new SimplePage(pageNo, count, PageSize.SIZE10.getSize());
        params.setSimplePage(page);
        List<QuestionInfo> searchList = questionMapper.selectQuestionsByParams(params);
        page = new SimplePage(pageNo, searchList.size(), PageSize.SIZE10.getSize());
        PaginationResult<QuestionInfo> result = new PaginationResult<>(page.getCountTotal(), page.getPageSize(), page.getPageNo(), page.getPageTotal(), searchList);
        return result;
    }

    @Override
    public PaginationResult<Question> getQuestionList(String content, Integer type, Integer difficulty, Integer pageNo, Integer pageSize) {
        QueryParams params = new QueryParams();
        if (!StrUtils.isEmpty(content)) {
            params.setFuzzyConditions("content", content);
        }
        if (type != null) {
            TypeEnum typeEnum = TypeEnum.getEnumByType(type);
            if (typeEnum == null) {
                throw new BusinessException(ResultCode.CODE_400);
            }
            params.setConditions("type", type);
        }
        if (difficulty != null) {
            DifficultyEnum difficultyEnum = DifficultyEnum.getDifficultByType(difficulty);
            if (difficultyEnum == null) {
                throw new BusinessException(ResultCode.CODE_400);
            }
            params.setConditions("difficulty", difficulty);
        }
        int count = questionMapper.selectCount(params);
        SimplePage page = new SimplePage(pageNo, count, pageSize);
        params.setSimplePage(page);
        List<Question> papers = questionMapper.selectList(params);
        PaginationResult<Question> result = new PaginationResult<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), papers);
        return result;
    }

    @Override
    public Question getQuestionInfo(String userId, Integer questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        boolean hasPaper = redisComponent.authPaper(userId, question.getPaperId());
        if (!hasPaper) {
            question.setOptions(null);
            question.setAnswer(null);
            question.setAnalysis(null);
        }
        question.setUnLock(hasPaper);
        return question;
    }

    @Override
    public void editQuestion(Question question, Boolean isAdd) {
        if (!isAdd) {
            Question bean = questionMapper.selectById(question.getQuestionId());
            if (bean == null) {
                throw new BusinessException(ResultCode.CODE_400);
            }
        }
        if (question.getType() != null) {
            TypeEnum typeEnum = TypeEnum.getEnumByType(question.getType());
            if (typeEnum == null) {
                throw new BusinessException(ResultCode.CODE_400);
            }
        }
        if (StrUtils.isEmpty(question.getContent())) {
            throw new BusinessException("题目内容不能为空");
        }
        if (question.getDifficulty() != null) {
            DifficultyEnum difficultyEnum = DifficultyEnum.getDifficultByType(question.getDifficulty());
            if (difficultyEnum == null) {
                throw new BusinessException(ResultCode.CODE_400);
            }
        }
        if (StrUtils.isEmpty(question.getOptions()) || StrUtils.isEmpty(question.getAnswer())) {
            throw new BusinessException("题目信息必须设置完整");
        }
        if (isAdd) {
            questionMapper.insert(question);
        } else {
            questionMapper.updateById(question);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Integer questionId) {
        Question question = questionMapper.selectById(questionId);
        Paper paper = paperMapper.selectById(question.getPaperId());
        questionMapper.deleteById(questionId);
        if (paper != null) {
            Integer questionCount = paper.getQuestionCount() - 1;
            paper.setQuestionCount(questionCount);
            Integer totalScore = paper.getTotalScore() - TypeEnum.getScoreByType(question.getType());
            paper.setTotalScore(totalScore);
            paperMapper.updateById(paper);
        }
    }

    @Override
    public PaginationResult<Question> getNoComposeQuestions(Integer pageNo, Integer pageSize) {
        Integer start = (pageNo - 1) * pageSize;
        List<Question> questions = questionMapper.selectListNoPaperId(start, pageSize);
        Integer count = questionMapper.selectNoPaperIdCount();
        Integer pageTotal = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
        PaginationResult<Question> result = new PaginationResult<>(count, pageSize, pageNo, pageTotal, questions);
        return result;
    }

    @Override
    public List<Question> getQuestionsByPaper(Integer paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        QueryParams params = new QueryParams();
        params.setConditions("paper_id", paperId);
        return questionMapper.selectList(params);
    }

}
