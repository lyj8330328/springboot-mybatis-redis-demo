package com.example.springbootmybatisredisdemo.service;

import com.example.springbootmybatisredisdemo.pojo.Category;
import com.github.pagehelper.PageInfo;


public interface Service {
    PageInfo<Category> findAll(int start,int size);

    void save(Category category);

    void delete(int id);

    Category get(int id);

    void update(Category category);
}
