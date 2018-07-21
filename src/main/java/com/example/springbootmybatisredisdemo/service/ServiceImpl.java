package com.example.springbootmybatisredisdemo.service;

import com.example.springbootmybatisredisdemo.RedisPage.RedisPage;
import com.example.springbootmybatisredisdemo.mapper.CategoryMapper;
import com.example.springbootmybatisredisdemo.pojo.Category;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;

@org.springframework.stereotype.Service
public class ServiceImpl implements Service {


    private static final org.slf4j.Logger LOGGER=LoggerFactory.getLogger(ServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisPage page;

    /**
     * 从数据库中查找
     * @param start
     * @param
     * @return
     */
//    public PageInfo<Category> db_select(int start,int size){
//        ZSetOperations operations=redisTemplate.opsForZSet();
//        PageHelper.startPage(start, size, "id desc");
//        List<Category> categories = categoryMapper.findAll();
//        PageInfo<Category> pageInfo = new PageInfo<>(categories);
//        for (Category category : categories) {
//            operations.add("category", category, category.getId());
//        }
//        //先删再存，避免重复
//        operations.removeRangeByScore("page",1,1);
//        operations.add("page",pageInfo.getPages(),1);
//        return pageInfo;
//    }
//
//    public void lastPage(int start,int size){
//        ZSetOperations operations=redisTemplate.opsForZSet();
//        PageHelper.startPage(start+1, size, "id desc");
//        List<Category> categories = categoryMapper.findAll();
//        PageInfo<Category> pageInfo2 = new PageInfo<>(categories);
//        for (Category category : categories) {
//            operations.add("category", category, category.getId());
//        }
//        //先删再存，避免重复
//        operations.removeRangeByScore("page",1,1);
//        operations.add("page",pageInfo2.getPages(),1);
//        operations.removeRangeByScore("lastPage",1,1);
//        operations.add("lastPage",categories.size(),1);
//    }

//    public PageInfo<Category> show(List<Category> categories,int start,int pages){
//        PageInfo<Category> pageInfo=new PageInfo<>(categories);
//
//        pageInfo.setPageNum(++start); //设置当前页
//        pageInfo.setPages(pages);//设置总页数
//        return pageInfo;
//    }
    /**
     * 思路：当第一次读取数据库的时候就记录所有条目的ID，然后将所有条目存储到缓存中，然后重复查找的时候就判断缓存中缓存是否存在
     * @return
     */
    @Override
    public PageInfo<Category> findAll(int start,int size) {
        return page.findByPage(start,size);
//        //准备工作
//        ZSetOperations operations=redisTemplate.opsForZSet();
//        Long count=operations.size("category");
//        long page=count%5>0?(count/5)+1:count/5;
//        int end= (int) (count%5);
//        Long pagesize=operations.size("page");
//        int pages=0;
//        if(pagesize!=0) {
//            String real_page=operations.range("page",0,1).iterator().next().toString();
//            pages=Integer.parseInt(real_page);
//
//        }
//
//        if(start==pages&&pages!=0){
//            lastPage(start,size);
//        }
//
//        int lastPage=5;
//        if(start>pages&&pages!=0) --start;
//        if(operations.size("lastPage")!=0) {
//            lastPage = Integer.parseInt(operations.range("lastPage", 0, 1).iterator().next().toString());
//        }
//
//
//        //先识别用户是从首页依次往后遍历，还是从末页往前遍历
//        /**
//         * 就是判断start的值是否等于最大页数，如果相等，则判断是一直点击下一页到达最后一页，还是在中途点击了末页到底最后一页。
//         * 判断的方法就是通过比较最后一页的页数*页长是否等于缓存中的数据量，如果大于说明全部数据还没有完全进入
//         */
//        if(start>0) --start;//处理0页
//        else if(start*5>=count) --start;//处理最后一页+1
//        LOGGER.info("start:"+start);
//        LOGGER.info("page:"+pages);
//
//        Set<Category> categories1=operations.reverseRange("category",start*5,(start+1)*5-1); //倒序查询
//        Set<Category> categories2=operations.range("category",count-(start+1)*5,count-(start*5)-1); //正序查询
//
//        LOGGER.info("categories1:"+categories1.size());
//        LOGGER.info("categories2:"+categories2.size());
//
//        int result=0;
//        for (Category category:categories1){
//            for (Category category1:categories2){
//                if(category.getId()==category1.getId()){
//                    result++;
//                }
//            }
//        }
//        LOGGER.info("result:"+result);
//        if(result==5){
//            //缓存中已经有了该页数据
//            LOGGER.info("从缓存中调");
//            List<Category> categoryList=new ArrayList<>(categories1);
//            PageInfo<Category> pageInfo=new PageInfo<>(categoryList);
//
//            pageInfo.setPageNum(++start); //设置当前页
//            pageInfo.setPages(pages);//设置总页数
//            return pageInfo;
//        }else {
//            //
//            if(categories1.size()==5&&categories1.size()!=0&&categories2.size()==0){
//                LOGGER.info("从缓存中调,1!=0,2=0");
//                return show(new ArrayList<>(categories1),start,pages);
//            }
////            else if(categories1.size()==0&&categories2.size()!=0&&start!=pages/2){
////                LOGGER.info("从缓存中调,1=0.2!=0");
////                return show(categories2,start,pages);
////            }
//            else if(categories1.size()-lastPage==0&&categories2.size()==0&&start+1==pages||(categories1.size()+5-lastPage==categories2.size()&&categories1.size()!=0&&start*5+lastPage==count)){
//                if(categories2.size()==0){
//                    LOGGER.info("从缓存中调，最后一页");
//                    return show(new ArrayList<>(categories1),start,pages);
//                }else {
//                    LOGGER.info("从缓存中调,最后一页:" + (5 - lastPage));
//                    LOGGER.info("最后一页：" + lastPage);
//                    List<Category> categoryList = new ArrayList<>(categories2);
//                    List<Category> newList = new ArrayList<>();
//                    for (int i = categoryList.size() - 1; i >= 5 - lastPage; i--) {
//                        newList.add(categoryList.get(i));
//                    }
//
//                    return show(newList, start, pages);
//                }
//            }
//            else if(categories1.size()==5&&categories2.size()==5){
//                LOGGER.info("从缓存中调,1=0.2!=0");
//               //转换成倒序
//                List<Category> categoryList=new ArrayList<>(categories2);
//                List<Category> newList=new ArrayList<>();
//                for (int i=categoryList.size()-1;i>=0;i--){
//                    newList.add(categoryList.get(i));
//                }
//
//                return show(newList,start,pages);
//            }
//            else {
//                LOGGER.info("从数据库中调");
//                PageInfo<Category> pageInfo = db_select(start+1,size);
//                return pageInfo;
//            }
//        }

    }

    @Override
    public void save(Category category) {
        ZSetOperations operations=redisTemplate.opsForZSet();
        Set<Category> categories=operations.reverseRange("category",0,1);
        if (categories.size()!=0) {
            category.setId(categories.iterator().next().getId() + 1);
            operations.add("category", category, categories.iterator().next().getId() + 1);
        }
        categoryMapper.save(category);
        //更新缓存
    }
    /**
     * 删除逻辑：
     * 如果缓存存在：删除，不存在则不操作
     * @param id
     */
    @Override
    public void delete(int id) {
        ZSetOperations operations=redisTemplate.opsForZSet();
        operations.removeRangeByScore("category",id,id);
        categoryMapper.delete(id);
    }

    /**
     * 获取一个分类：
     * 如果缓存中存在，则从缓存中获取，否则从DB中获取
     */
    @Override
    public Category get(int id) {
        ZSetOperations operations=redisTemplate.opsForZSet();

        //缓存存在
        Long hasKey=operations.size("category");
        if(hasKey>0){
            LOGGER.info("在缓存中查找");
            Set<Category> categories=operations.rangeByScore("category",id,id);
            return categories.iterator().next();
        }else {
            LOGGER.info("在数据库中查找");
            Category category=categoryMapper.get(id);
            operations.add("category",category,category.getId());
            return category;
        }
    }

    @Override
    public void update(Category category) {
        ZSetOperations operations=redisTemplate.opsForZSet();
        operations.removeRangeByScore("category",category.getId(),category.getId());
        operations.add("category",category,category.getId());
        categoryMapper.update(category);
    }
}
