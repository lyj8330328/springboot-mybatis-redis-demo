package com.example.springbootmybatisredisdemo.mapper;


import com.example.springbootmybatisredisdemo.pojo.Category;
import org.apache.ibatis.annotations.*;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CategoryMapper {
    @Select("select * from category")
    List<Category> findAll();

    @Insert("insert into category(name) values (#{name})")
    int save(Category category);

    @Delete(" delete from category where id= #{id} ")
    void delete(int id);

    @Select("select * from category where id= #{id} ")
    Category get(int id);

    @Update("update category set name=#{name} where id=#{id} ")
    void update(Category category);
}
