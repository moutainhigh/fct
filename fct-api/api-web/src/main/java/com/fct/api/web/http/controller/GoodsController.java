package com.fct.api.web.http.controller;

import com.fct.api.web.http.json.JsonListResponseEntity;
import com.fct.api.web.http.json.JsonResponseEntity;
import com.fct.mall.data.entity.Goods;
import com.fct.mall.data.entity.GoodsGrade;
import com.fct.mall.interfaces.MallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by jon on 2017/5/27.
 */
@RestController
@RequestMapping(value = "/goods")
public class GoodsController {

    @Autowired
    private MallService mallService;

    /**
     * 获取商品品级
     * @return
     */
    @RequestMapping(value = "grade", method = RequestMethod.GET)
    public JsonResponseEntity<List<GoodsGrade>> findGoodsGrade(){

        List<GoodsGrade> lsGG = mallService.findGoodsGrade();

        JsonResponseEntity<List<GoodsGrade>> responseEntity = new JsonResponseEntity<>();
        responseEntity.setData(lsGG);

        return responseEntity;
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public JsonListResponseEntity<Goods> findGoods(String name, String categorycode, Integer gradeid,
                                                         Integer pageindex, Integer pagesize){

        Page<Goods> ls = mallService.findGoods(name,categorycode,gradeid,1,pageindex,pagesize);

        JsonListResponseEntity<Goods> responseEntity = new JsonListResponseEntity<>();
        responseEntity.setContent(ls.getContent(), true, "xx:desc", String.valueOf(pageindex));

        return responseEntity;
    }
}