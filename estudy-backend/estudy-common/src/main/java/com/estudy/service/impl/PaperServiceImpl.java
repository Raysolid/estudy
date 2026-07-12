package com.estudy.service.impl;

import com.estudy.component.RedisComponent;
import com.estudy.entity.enums.PageSize;
import com.estudy.entity.enums.ResultCode;
import com.estudy.entity.enums.TypeEnum;
import com.estudy.entity.po.Category;
import com.estudy.entity.po.Paper;
import com.estudy.entity.po.Question;
import com.estudy.entity.po.User;
import com.estudy.entity.query.QueryParams;
import com.estudy.entity.query.SimplePage;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.exception.BusinessException;
import com.estudy.mappers.CategoryMapper;
import com.estudy.mappers.PaperMapper;
import com.estudy.mappers.QuestionMapper;
import com.estudy.mappers.UserMapper;
import com.estudy.service.PaperService;
import com.estudy.utils.StrUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class PaperServiceImpl implements PaperService {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserMapper<User, QueryParams> userMapper;

    @Resource
    private CategoryMapper<Category, QueryParams> categoryMapper;

    @Resource
    private PaperMapper<Paper, QueryParams> paperMapper;

    @Resource
    private QuestionMapper<Question, QueryParams> questionMapper;

    @Override
    public List<Paper> recommendPapers(String userId) {
        if (userId == null) {
            return paperMapper.selectPaperListByCategoryId(null, null);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        String[] preferCategoryIds = user.getPreference().split(",");
        List<Integer> paperIds = paperMapper.selectPaperIdByCategoryIds(preferCategoryIds);
        if (paperIds.isEmpty()) {
            return paperMapper.selectPaperListByCategoryId(null, null);
        }
        long limit = Math.min(paperIds.size(), 5);
        List<Integer> randomIds = new Random().ints(0, paperIds.size()).distinct().limit(limit)
                .mapToObj(paperIds::get).toList();
        return paperMapper.selectPaperByIds(randomIds);
    }

    @Override
    public List<Paper> getHotPapersByCategory(Integer categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        return paperMapper.selectPaperListByCategoryId(categoryId, null);
    }

    @Override
    public PaginationResult<Paper> getPapersByCategory(Integer categoryId, Integer pageNo) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        QueryParams params = new QueryParams();
        params.setConditions("category_id", categoryId);
        int count = paperMapper.selectCount(params);
        SimplePage page = new SimplePage(pageNo, count, PageSize.SIZE10.getSize());
        List<Paper> papers = paperMapper.selectPaperListByCategoryId(categoryId, page);
        PaginationResult<Paper> result = new PaginationResult<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), papers);
        return result;
    }

    @Override
    public void unlockPaper(String userId, Integer paperId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        if (user.getPoints() < paper.getPointsRequired()) {
            throw new BusinessException("用户积分不足");
        }
        userMapper.updateUserPoints(userId, -paper.getPointsRequired());
        redisComponent.unlockPaper(userId, paperId);
    }

    @Override
    public Paper getPaperInfo(Integer paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        return paper;
    }

    @Override
    public PaginationResult<Paper> getPaperList(String name, Integer categoryId, String tag, Integer pageNo, Integer pageSize) {
        QueryParams params = new QueryParams();
        if (!StrUtils.isEmpty(name)) {
            params.setFuzzyConditions("name", name);
        }
        if (categoryId != null) {
            Category category = categoryMapper.selectById(categoryId);
            if (category == null) {
                throw new BusinessException(ResultCode.CODE_400);
            }
            params.setConditions("category_id", categoryId);
        }
        if (!StrUtils.isEmpty(tag)) {
            params.setFuzzyConditions("tag", tag);
        }
        int count = paperMapper.selectCount(params);
        SimplePage page = new SimplePage(pageNo, count, pageSize);
        params.setSimplePage(page);
        List<Paper> papers = paperMapper.selectList(params);
        PaginationResult<Paper> result = new PaginationResult<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), papers);
        return result;
    }

    @Override
    public void addPaper(Paper paper) {
        if (paper.getCategoryId() == null) {
            throw new BusinessException("请选择试卷所属分类");
        }
        if (StrUtils.isEmpty(paper.getName())) {
            throw new BusinessException("试卷名称不能为空");
        }
        if (paper.getDuration() == null) {
            throw new BusinessException("答题时间不能为空");
        }
        paper.setQuestionCount(0);
        paper.setTotalScore(0);
        paperMapper.insert(paper);
    }

    @Override
    public void updatePaper(Paper paper) {
        Paper bean = paperMapper.selectById(paper.getPaperId());
        if (bean == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        if (paper.getCategoryId() == null) {
            throw new BusinessException("请选择试卷所属分类");
        }
        if (StrUtils.isEmpty(paper.getName())) {
            throw new BusinessException("试卷名称不能为空");
        }
        if (paper.getDuration() == null) {
            throw new BusinessException("答题时间不能为空");
        }
        paperMapper.updateById(paper);
    }

    @Override
    public void deletePaper(Integer paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        QueryParams params = new QueryParams();
        params.setConditions("paper_id", paperId);
        Integer count = questionMapper.selectCount(params);
        if (count > 0) {
            throw new BusinessException("该试卷下还有题目，无法进行删除");
        }
        paperMapper.deleteById(paperId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Paper composePaper(Integer paperId, Integer[] addIds, Integer[] removeIds) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        // 移除题目
        if (removeIds.length > 0) {
            questionMapper.updatePaperIdBatch(removeIds, null);
        }
        // 添加题目
        if (addIds.length > 0) {
            questionMapper.updatePaperIdBatch(addIds, paperId);
        }
        // 修改试卷信息
        QueryParams params = new QueryParams();
        params.setConditions("paper_id", paperId);
        List<Question> questions = questionMapper.selectList(params);
        Integer totalScore = questions.stream().mapToInt(q -> TypeEnum.getScoreByType(q.getType())).sum();
        paper.setTotalScore(totalScore);
        paper.setQuestionCount(questions.size());
        paperMapper.updateById(paper);
        return paper;
    }

}
