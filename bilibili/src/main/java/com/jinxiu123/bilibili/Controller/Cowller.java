package com.jinxiu123.bilibili.Controller;

import com.github.pagehelper.Page;
import com.jinxiu123.bilibili.Service.CrawlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class Cowller {
    @Autowired
    private CrawlService crawl;
    @RequestMapping("/a")
    @ResponseBody
    public void Cowller() throws IOException {
//            crawl.getCrawldata();
        crawl.etl();
    }
    @RequestMapping("/")
    public String index(){
        return "manager-1";
    }
    @RequestMapping("/list")
    @ResponseBody
    public Map list(Integer page,Integer limit ,Integer typeid, String keyword){
        Page<Map> p= crawl.findcontents(page,limit,typeid,keyword);
        Map map=new HashMap();
        map.put("code",0);
        map.put("msg","");
        map.put("count",p.getTotal());
        map.put("data",p.getResult());
        return map;
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Map delete( Integer contentId){
        crawl.delete(contentId);
        Map result =new HashMap();
        result.put("code",0);
        result.put("msg",contentId+"已删除");
        return result;
    }

}
