package com.fct.web.admin.http.cache;

import com.fct.common.data.entity.ArticleCategory;
import com.fct.common.data.entity.ImageCategory;
import com.fct.common.data.entity.VideoCategory;
import com.fct.common.interfaces.CommonService;
import com.fct.web.admin.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CacheCommonManager {

    @Autowired
    private CommonService commonService;

    private List<ArticleCategory> findArticleCategory()
    {
        try {
            List<ArticleCategory> lsCategory = commonService.findArticleCategory(-1, "", "");
            if (lsCategory == null && lsCategory.size() <= 0) {
                lsCategory = new ArrayList<>();
            }

            return lsCategory;
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
        return new ArrayList<>();
    }

    public List<ArticleCategory> findArticleCategoryByParent()
    {
        List<ArticleCategory> lsCate = new ArrayList<>();

        for (ArticleCategory cate:findArticleCategory()
                ) {
            if(cate.getParentId() ==0)
                lsCate.add(cate);
        }
        return lsCate;
    }

    public List<ArticleCategory> findArticleCategoryByParentId(Integer parentId)
    {
        List<ArticleCategory> lsCate = new ArrayList<>();

        for (ArticleCategory cate:findArticleCategory()
                ) {
            if(cate.getParentId() == parentId)
                lsCate.add(cate);
        }
        return lsCate;
    }

    public String getArticleCateName(String ids)
    {
        List<ArticleCategory> cateList = findArticleCategory();
        String name = "";
        for (ArticleCategory cate: cateList
                ) {
            if(ids.contains(cate.getCode()))
            {
                if(!StringUtils.isEmpty(name))
                {
                    name +="-";
                }
                name += cate.getName();
            }
        }
        return name;
    }

    public List<ImageCategory> findImageCategory()
    {
        try {
            List<ImageCategory> list = commonService.findImageCategory();
            if (list == null && list.size() <= 0) {
                list = new ArrayList<>();
            }
            return list;
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
        return new ArrayList<>();
    }

    public String getImageCategoryName(Integer id)
    {
        List<ImageCategory> list = findImageCategory();
        for (ImageCategory cate: list
                ) {
            if(id == cate.getId())
            {
                return cate.getName();
            }
        }
        return "";
    }

    public List<VideoCategory> findVideoCategory()
    {
        try {
            List<VideoCategory> list = commonService.findVideoCategory();
            if (list == null && list.size() <= 0) {
                list = new ArrayList<>();
            }
            return list;
        }
        catch (Exception exp)
        {
            Constants.logger.error(exp.toString());
        }
        return new ArrayList<>();
    }

    public String getVideoCategoryName(Integer id)
    {
        List<VideoCategory> list = findVideoCategory();
        for (VideoCategory cate: list
                ) {
            if(id == cate.getId())
            {
                return cate.getName();
            }
        }
        return "";
    }
}