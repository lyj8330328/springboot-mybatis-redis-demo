package com.example.springbootmybatisredisdemo.controller;

import com.example.springbootmybatisredisdemo.pojo.Category;
import com.example.springbootmybatisredisdemo.service.Service;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CategoryController {

    @Autowired
    private Service service;

    //1.返回5条记录
    @GetMapping("/category")
    public String listCategory(Model model,@RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
        PageInfo<Category> pageInfo=service.findAll(start,size);
        model.addAttribute("pageInfo",pageInfo);
        return "listCategories";
    }
    //2.保存一条记录
    @PutMapping("/category")
    public String addCategories(Category category) throws Exception {
        System.out.println("保存一条记录");
        service.save(category);
        return "redirect:/category";
    }
    //3.删除一条记录
    @DeleteMapping("/category/{id}")
    public String deleteCategory(Category category){
        System.out.println("删除一条记录！");
        service.delete(category.getId());
        return "redirect:/category";
    }

    //4.更新一条记录
    @PostMapping("/category/{id}")
    public String updateCategory(Category category,int start){
        System.out.println("更新一条记录！");
        service.update(category);
        return "redirect:/category?start="+start;
    }
    //6.跳转到编辑页
    @GetMapping("/category/{start}/{id}")
    public String addCategory(@PathVariable("id") int id,@PathVariable("start") int start, Model model) throws Exception{
        System.out.println("编辑视图");
        Category category=service.get(id);
        model.addAttribute("c",category);
        model.addAttribute("start",start);
        return "editCategory";
    }


}
