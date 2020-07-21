package com.jinxiu123.bilibili.task;


import com.jinxiu123.bilibili.Service.CrawlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Component
public class Crowltask {
    @Value("${app.crawerl.url}")
    private  String url;
    @Value("${app.crawerl.enabled}")
    private Boolean enabled;
    Logger logger = LoggerFactory.getLogger(Crowltask.class);
    @Resource
    private CrawlService crawlService;

    //定时任务方法
    //通过Scheduled指名当前方法是个任务调度
    //秒  分  时  日   月  星期 0 0 * * * ?
    @Scheduled(cron = "${app.crawerl.cron}")//每小时执行一次
    public void crowlrunner(){
            if(enabled==false){
                logger.warn("爬虫任务已被关闭，如需使用，请设置application.yml");
                return;
            }
            //用来记录抓取次数
            HashMap count = new HashMap();
            count.put("count", 0);

            crawlService.Cowller(url, System.currentTimeMillis(), count);
            //对source表数据进行处理
            crawlService.etl();


    }
}
