package com.example.springbootmybatisredisdemo.RedisPage;

import com.example.springbootmybatisredisdemo.mapper.CategoryMapper;
import com.example.springbootmybatisredisdemo.pojo.Category;
import com.example.springbootmybatisredisdemo.service.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
@Service
public class RedisPage {

    private static final org.slf4j.Logger LOGGER=LoggerFactory.getLogger(ServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 从数据库中查找
     * @param start
     * @param size
     * @return
     */
    public PageInfo<Category> db_select(int start,int size){
        ZSetOperations operations=redisTemplate.opsForZSet();
        PageHelper.startPage(start, size, "id desc");
        List<Category> categories = categoryMapper.findAll();
        PageInfo<Category> pageInfo = new PageInfo<>(categories);
        for (Category category : categories) {
            operations.add("category", category, category.getId());
        }
        //先删再存，避免重复
        operations.removeRangeByScore("page",1,1);
        operations.add("page",pageInfo.getPages(),1);
        return pageInfo;
    }

    public void lastPage(int start,int size){
        LOGGER.info("从数据库中调取最后一页，然后放入缓存");
        ZSetOperations operations=redisTemplate.opsForZSet();
        PageHelper.startPage(start+1, size, "id desc");
        List<Category> categories = categoryMapper.findAll();
        PageInfo<Category> pageInfo2 = new PageInfo<>(categories);
        for (Category category : categories) {
            operations.add("category", category, category.getId());
        }
        System.out.println(pageInfo2);
        //先删再存，避免重复
        operations.removeRangeByScore("page",1,1);
        operations.add("page",pageInfo2.getPages(),1);
        operations.removeRangeByScore("lastPage",1,1);
        operations.add("lastPage",pageInfo2.getTotal()%5,1);
    }

    public PageInfo<Category> show(List<Category> categories,int start,int pages){
        PageInfo<Category> pageInfo=new PageInfo<>(categories);

        pageInfo.setPageNum(++start); //设置当前页
        pageInfo.setPages(pages);//设置总页数
        return pageInfo;
    }
    public PageInfo<Category> findByPage(int start, int size) {
        //准备工作

        ZSetOperations operations=redisTemplate.opsForZSet();
        Long count=operations.size("category");
        Long pagesize=operations.size("page");
        Long lastPages=operations.size("lastPage");


        int pages=0;
        if(pagesize!=0) {
            String real_page=operations.range("page",0,1).iterator().next().toString();
            pages=Integer.parseInt(real_page);

        }
        LOGGER.info("start:"+start);
        LOGGER.info("pages:"+pages);

        long newStart=pages-start;

        if(start+newStart==0&&start!=1){
            LOGGER.info("进入最后一页");
            lastPage(start,size);
        }

        int lastPage=-1;
        if(start>pages&&pages!=0) --start;
        if(operations.size("lastPage")!=0) {
            lastPage = Integer.parseInt(operations.range("lastPage", 0, 1).iterator().next().toString());
        }


        //先识别用户是从首页依次往后遍历，还是从末页往前遍历
        /**
         * 就是判断start的值是否等于最大页数，如果相等，则判断是一直点击下一页到达最后一页，还是在中途点击了末页到底最后一页。
         * 判断的方法就是通过比较最后一页的页数*页长是否等于缓存中的数据量，如果大于说明全部数据还没有完全进入
         */

        if(start>0) --start;//处理0页
        else if(start*5>=count) --start;//处理最后一页+1
        LOGGER.info("count:"+count);
        Set<Category> categories1=operations.reverseRange("category",start*5,(start+1)*5-1); //倒序查询

        LOGGER.info("newStart:"+newStart);
        Set<Category> categories2;
        if (start+1==pages){
             categories2=operations.range("category",newStart*5,lastPage-1); //正序查询
        }else {
            categories2=operations.range("category",newStart*5-1,newStart*5+3);
        }

        LOGGER.info("categories1:"+categories1.size());
        LOGGER.info("categories2:"+categories2.size());

        for (Category category:categories1){
            System.out.println(category.getId());
        }
        for (Category category:categories2){
            System.out.println(category.getId());
        }

        //比较正反两次查询的结果是否相同
        int result=0;
        for (Category category:categories1){
            for (Category category1:categories2){
                if(category.getId()==category1.getId()){
                    //System.out.println(category.getId());
                    result++;
                }
            }
        }
        LOGGER.info("result:"+result);
        /**
         * 可能的结果有以下几种
         * 1.categories2.size()==0&&categories1.size()==0说明缓存中没有数据，从数据库中查
         * 2.categories1.size()==5&&categories2.size()==0说明用户是从首页开始，一直点击下一页，逆序查找当页数据
         * 3.categories2.size()==lastPage
         */

        if(categories1.size()==0&&categories2.size()==0){
            LOGGER.info("从数据库中调用-1");
            return db_select(start+1,size);
        }else {
            if(start+1==pages){
                LOGGER.info("最后一页");
                if(count==start*5+lastPage){
                    LOGGER.info("从首页开始点击下一页到达末页，从缓存中调-1");
                    List<Category> categoryList = new ArrayList<>(categories1);
                    return show(categoryList,start,size);
                }else if(categories2.size()==lastPage){
                    LOGGER.info("中间点击末页，从缓存中调-2");
                    //逆向输出
                    List<Category> categoryList = new ArrayList<>(categories2);
                    List<Category> newList = new ArrayList<>();
                    for (int i = lastPage-1; i >= 0; i--) {
                        newList.add(categoryList.get(i));
                    }
                    return show(newList,start,size);
                }
                else {
                    LOGGER.info("从数据库中调-2");
                    return db_select(start+1,size);
                }
            }
            else {
                if(result==5){
                    LOGGER.info("从缓存中调-3");
                    List<Category> categoryList = new ArrayList<>(categories1);
                    return show(categoryList,start,size);
                }
                else {
                    //19条记录
                    if(start>newStart&&categories2.size()>=categories1.size()){
                        LOGGER.info("从缓存中调-5");
                        if(categories1.size()==lastPage&&count!=(start+1)*5+lastPage){
                            LOGGER.info("从数据库中调-5");
                            return db_select(start+1,size);
                        }else {
                            //逆向输出
                            List<Category> categoryList = new ArrayList<>(categories2);
                            List<Category> newList = new ArrayList<>();
                            for (int i = categoryList.size() - 1; i >= 0; i--) {
                                newList.add(categoryList.get(i));
                            }
                            return show(newList, start, size);
                        }
                    }else if(start<newStart&&categories1.size()==5){
                        LOGGER.info("从缓存中调-7");
                        List<Category> categoryList = new ArrayList<>(categories1);
                        return show(categoryList,start,size);
                    }
                    else if(categories1.size()==5&&categories2.size()==0){
                        LOGGER.info("从缓存中调-6");
                        List<Category> categoryList = new ArrayList<>(categories1);
                        return show(categoryList,start,size);
                    }
                    else {
                        LOGGER.info("从数据库中调-4");
                        return db_select(start+1,size);
                    }

                }
            }
        }

    }
}
