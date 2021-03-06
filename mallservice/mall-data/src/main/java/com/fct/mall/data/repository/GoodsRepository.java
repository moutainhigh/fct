package com.fct.mall.data.repository;

import com.fct.mall.data.entity.Goods;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * Created by jon on 2017/5/16.
 */
public interface GoodsRepository extends JpaRepository<Goods, Integer> {

    @Query(nativeQuery = true,
            value = "select count(0) from goods where categoryCode like ?1")
    int countByCategory(String id);

    int countByGradeId(Integer gradeId);

    @Query(nativeQuery = true,
            value = "UPDATE Goods set SortIndex=?2,updatetime=?3 WHERE Id=?1")
    void updateSortIndex(Integer id,Integer sortIndex,String updateTime);

    @Query(nativeQuery = true,
            value = "UPDATE Goods set Status=?2,updatetime=?3 WHERE Id=?1")
    void updateStatus(Integer id,Integer status,String updateTime);

    @Query(nativeQuery = true,
            value = "UPDATE Goods set IsDel=1, Status=-1,UpdateTime=now() WHERE Id=?2")
    void delete(Integer id);

}
