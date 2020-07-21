package com.jinxiu123.bilibili.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jinxiu123.bilibili.common.OgnlUtils;
import com.jinxiu123.bilibili.enity.Content;
import com.jinxiu123.bilibili.enity.Data;
import com.jinxiu123.bilibili.enity.Type;
import com.jinxiu123.bilibili.mapper.ContentMapper;
import com.jinxiu123.bilibili.mapper.DataMapper;
import com.jinxiu123.bilibili.mapper.TypeMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrawlService {
    Logger logger = LoggerFactory.getLogger(CrawlService.class);
    @Resource
    private DataMapper dataMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private ContentMapper contentMapper;

    /**
     * @param url
     * @author yjl
     * @deprecated 爬虫描述
     */
    public void Cowller(String url, long time, Map count) {
        String url1 = url + Long.toString(time);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url1);
        builder.addHeader("User-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36");
        Request request = builder.build();
        Response response = null;
        String text = null;
        //重试机制
        for (int i = 0; i < 10; i++) {
            try {
                response = okHttpClient.newCall(request).execute();
                text = response.body().string();
                //抓取成功后跳出循环
                break;
            } catch (IOException e) {
                logger.warn("爬虫连接超时，正在准备第 {} 次重试 ， URL: {}", i + 1, url1);
                continue;
            }
        }
        if (text == null) {
            logger.error("程序抓取失败");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logger.info("数据抓去成功，时间:{},url:{}", simpleDateFormat.format(new Date(time)), url1);
        //入库
        Data data = new Data();
        data.setResponseText(text);
        data.setCreatTime(new Date());
        data.setUrl(url1);
        data.setState("waiting");

        dataMapper.insert(data);

        int num = (int) count.get("count");
        num += 20;
        count.put("count", num);
        //抓取5次
        if (num >= 100) {
            return;
        }
        Cowller(url, System.currentTimeMillis(), count);
    }



    /**
     * etl数据处理
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public void etl() {
        //查询所有等待处理的数据源
        List<Data> list = dataMapper.findByState("waiting");
        for (Data data : list) {
            String json = data.getResponseText();
            Map root = new Gson().fromJson(json, new TypeToken<Map>() {
            }.getType());
            //获取主题数据
            List<Map<String, Object>> list1 = OgnlUtils.getlistmap("data.items", root);

            for (Map<String, Object> contentMap : list1) {
                //去除广告
                if (OgnlUtils.getstring("card_goto", contentMap).equals("banner") ||
                        OgnlUtils.getstring("card_goto", contentMap).contains("ad_web")||
                        OgnlUtils.getstring("card_goto", contentMap).contains("rcmd")||
                        OgnlUtils.getstring("param", contentMap).equals("")) {
                    continue;
                }
                creatcontent(contentMap);
            }
            //对处理完成的source更新状态为processed
            data.setState("processed");
            dataMapper.updateByPrimaryKeySelective(data);
        }
    }

    /**
     * 主数据处理
     *
     * @param contentMap
     */
    public void creatcontent(Map contentMap) {
        Type type = new Type();
        Content content = new Content();
        if (OgnlUtils.getstring("goto", contentMap).equals("av")) {
            type.setTypeId(1);
            type.setTypeName("视频");
        } else if (OgnlUtils.getstring("goto", contentMap).equals("live")) {
            type.setTypeId(2);
            type.setTypeName("直播");
        } else if (OgnlUtils.getstring("goto", contentMap).equals("bangumi")) {
            type.setTypeId(3);
            type.setTypeName("番剧");
        } else if (OgnlUtils.getstring("goto", contentMap).equals("article")) {
            type.setTypeId(4);
            type.setTypeName("文章");
        } else {
            type.setTypeId(5);
            type.setTypeName("其他");
        }
        if (typeMapper.selectByPrimaryKey(type.getTypeId()) == null) {
            typeMapper.insert(type);
        }
        //插入主数据
        String param = OgnlUtils.getstring("param", contentMap);

        Content content1 = contentMapper.selectByPrimaryKey(Integer.parseInt(param));
        if (content1 != null){
            logger.info("ID为:{} 的内容已存在,此内容被忽略",content1.getContentId());
            return;
        }
            content.setContentId(Integer.parseInt(param));
            //类型id
            content.setTypeId(type.getTypeId());

            //upid
            Number upid = OgnlUtils.getNumber("args.up_id", contentMap);
            if (upid != null) {
                content.setUpId(upid.intValue());
            } else {
                content.setUpId(1111);
            }
            //up主
            String upname = OgnlUtils.getstring("args.up_name", contentMap);
            if (!upname.equals("") ) {
                content.setUpName(upname);
            } else {
                content.setUpName("B站官方");
            }

            //圈子名称
            String Rname = OgnlUtils.getstring("args.rname", contentMap);
            if (!Rname.equals("") ) {
                content.settRname(Rname);
            } else {
                content.settRname("动漫/电影/电视剧");
            }

            //主题名称
            String tname = OgnlUtils.getstring("args.tname", contentMap);
            if (!tname.equals("")) {
                content.settTname(tname);
            } else {
                content.settTname("番剧");
            }


            //标题
            content.settTitle(OgnlUtils.getstring("title", contentMap));
            //url
            String url = null;
            if (type.getTypeId() == 1) {
                if(OgnlUtils.getstring("card_goto",contentMap).equals("special_s")){
                    param= StringUtils.substringAfterLast(OgnlUtils.getstring("uri",contentMap),"/");
                }
                url = "https://www.bilibili.com/video/av" + param;
            } else if (type.getTypeId() == 2) {
                url = "https://live.bilibili.com/" + param;
            } else {
                url =OgnlUtils.getstring("uri", contentMap);
            }
            content.settUrl(url);
            //播放量
            content.settPlaynum(OgnlUtils.getstring("cover_left_text_1", contentMap));
            //弹幕量
            content.settChatnum(OgnlUtils.getstring("cover_left_text_2", contentMap));
            //时常
            content.settLongtime(OgnlUtils.getstring("cover_right_text", contentMap));
            //推荐原因
            content.settRcmdReason(OgnlUtils.getstring("rcmd_reason", contentMap));
            contentMapper.insert(content);

        logger.info("Content ID:{} , 内容成功导入",content.getContentId());


    }

    /**
     *
     * @param page 页数
     * @param rows 行数
     * @return
     */
    public Page<Map> findcontents(Integer page,Integer rows,Integer typeid,String keyword ){
        Map params=new HashMap();
        if(typeid!=null && typeid!=-1){
            params.put("typeid",typeid);
        }
        if(keyword!=null&&!keyword.trim().equals("")){
            params.put("keyword","%"+keyword+"%");
        }
        PageHelper.startPage(page,rows);

       Page<Map> list=(Page<Map>) contentMapper.findparams(params);
       return list;
    }
    public void delete(Integer contentId){
        contentMapper.deleteByPrimaryKey(contentId);
    }
}
