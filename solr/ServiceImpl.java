package com.bw.fyj.service.impl;

import com.bw.fyj.dao.MailDao;
import com.bw.fyj.dao.UserDao;
import com.bw.fyj.pojo.Mail;
import com.bw.fyj.pojo.User;
import com.bw.fyj.service.IMailService;
import org.apache.dubbo.config.annotation.Service;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;


@Service
public class MailServiceImpl implements IMailService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private MailDao mailDao;

    @Autowired
    private SolrClient solrClient;


    /**
     * 分页查询列表
     *
     * @return
     */
    @Override
    public ModelAndView findByPageAndContent(String content, int page, int size, ModelAndView mv) {
        Specification<Mail> spec = new Specification<Mail>() {
            @Override
            public Predicate toPredicate(Root<Mail> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                return cb.like(root.get("emailcontent"), "%" + content + "%");
            }
        };

        Page<Mail> all = mailDao.findAll(spec, PageRequest.of(page - 1, size));//后台获取数据

        List<Mail> mailList = all.getContent();//列表信息

        int number = all.getNumber();//请求页
        int totalPages = all.getTotalPages();//尾页

        SolrQuery entries = new SolrQuery();
        entries.set("q", "*:*");
        try {
            QueryResponse query = solrClient.query(entries);
            SolrDocumentList results = query.getResults();

            if (results.size() <= 0) {      //判断Solr数据库是否有数据

                mailList.forEach(i -> {

                    i.setSolr_sendid(i.getSenduser().getUid());//提交Solr_SendId
                    i.setSolr_recid(i.getRecuser().getUid());//提交Solr_recId

                    try {
                        solrClient.addBean(i);//存入Solr数据库
                        solrClient.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
			//关闭资源
			solrClient.close();
		}
        mv.addObject("maillist", mailList);
        mv.addObject("number", number);
        mv.addObject("totalPages", totalPages);
        mv.setViewName("list.html");
        return mv;
    }


    /**
     * 新建邮件
     * 当前登陆用户ID uid
     * mail 中需要当前登陆用户信息：sendId
     */
    @Override
    public void saveMail(Mail mail, int uid, int choose) {
        User byUid = this.findByUid(uid);
        mail.setStatus(choose);// 0: 草稿、1：已发送
        mail.setSenduser(byUid);
        mail.setSendtime(new Date());
        mailDao.save(mail);
    }


    /**
     * 邮件搜索
     *
     * @return
     */
    @Override
    public ModelAndView findByContent(String content, int page, int size, ModelAndView mv) {

        ArrayList<Mail> mailList = new ArrayList<>();

        SolrQuery query = new SolrQuery();
        query.set("q", "emailcontent:*" + content + "*");//查询条件

        query.setStart(page);   //分页参数
        query.setRows(size);



        if (!content.equals("")) {//开启高亮
            query.setHighlight(true);
            query.addHighlightField("emailcontent");//高亮显示字段
            query.setHighlightSimplePre("<font color='red'>");//前缀
            query.setHighlightSimplePost("</font>");//后缀
        }
        try {
            QueryResponse response = solrClient.query(query);
            Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();//获取所有高亮字段
            SolrDocumentList solrDocumentList = response.getResults();//查询到的信息

            long numFound = solrDocumentList.getNumFound();
            long start = solrDocumentList.getStart();
            mv.addObject("numFound", numFound);
            mv.addObject("start", start);

            Iterator<SolrDocument> iterator = solrDocumentList.iterator();
            while (iterator.hasNext()) {     //遍历对象
                SolrDocument next = iterator.next();
                int mid = Integer.parseInt(next.getFieldValue("id").toString());//邮件ID
                String project = (String) next.getFieldValue("project").toString();//主题
                String contentBySolr = (String) next.getFieldValue("emailcontent").toString();//内容
                Date sendtime = (Date) next.getFieldValue("sendtime");//发送时间
                int status = Integer.parseInt(next.getFieldValue("status").toString());//状态
                String senduserid = next.getFieldValue("senduserid").toString();//发送人Id
                User senduser = this.findByUid(Integer.parseInt(senduserid));
                String recuserid = next.getFieldValue("recuserid").toString();//接收人Id
                User recuser = this.findByUid(Integer.parseInt(recuserid));

                Mail mail = new Mail();
                mail.setMid(mid);
                mail.setProject(project);
                mail.setEmailcontent(contentBySolr);
                mail.setSendtime(sendtime);
                mail.setSenduser(senduser);
                mail.setRecuser(recuser);
                mail.setStatus(status);

                if (highlighting != null){
                    List<String> contentBySolrList = highlighting.get(mid + "").get("emailcontent");//获取设置高亮的字段
                    if (contentBySolrList.size() > 0) {

                        String s = contentBySolrList.get(0);

                        mail.setEmailcontent(s);//高亮片段
                    }
                }

                mailList.add(mail);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
			//关闭资源
			solrClient.close();
		}
		
        mv.addObject("maillist", mailList);
        mv.setViewName("list.html");
        return mv;
    }
	
	
	/**
	*另一种 solr 查询对象转换 getBeans(Object.class)
	*/
	{
	 //1.请求连接
        String solrUrl = "http://localhost:8081/solr/core-demo";
        //2.创建对象
        HttpSolrClient client = new HttpSolrClient.Builder(solrUrl).build();
        //3.执行查询功能
        SolrQuery query = new SolrQuery();
        String keywords = "手机";
        //(1)设置查询条件q,根据用户输入情况返回不同结果
        if (StringUtils.isEmpty(keywords)) {
            query.set("q", "*:*");//用户没有输入数据则返回所有结果
        } else {
            query.set("q", "prod_name:" + keywords);//用户输入手机则返回相关结果
        }
        //(2)设置过滤查询fq，过滤出相关查询
        //(2.1)类别筛选
        String prod_catalog_name = "手机饰品";
        if (!StringUtils.isEmpty(prod_catalog_name)) {
//            query.set("fq","prod_catalog_name:" + prod_catalog_name);//两种写法均可种写法
            query.addFilterQuery("prod_catalog_name:" + prod_catalog_name);

        }
        //(2.2)价格筛选
        String prod_price = "1-";
        if (!StringUtils.isEmpty(prod_price)) {
            String[] strings = prod_price.split("-");
            if (strings.length == 1) {
                query.addFilterQuery("prod_price:[" + strings[0] + " TO *]");
            } else {
                if (StringUtils.isEmpty(strings[0])) {
                    query.addFilterQuery("prod_price:[* TO " + strings[1] + "]");
                } else
                    query.addFilterQuery("prod_price:[" + strings[0] + " TO " + strings[1] + "]");
            }
        }
        //(3)设置排序条件sort，排序相关内容
        //psort = 1升序，2降序
        int psort = 0;
        if (psort == 1) {
            query.addSort("prod_price", SolrQuery.ORDER.asc);
        } else if (psort == 2) {
            query.addSort("prod_price", SolrQuery.ORDER.desc);
        }
        //(4)设置分页功能,start默认为0，rows默认为10
        /**
         * 类似于mysql的分页
         * start offset 偏移量
         * rows rows 返回的最大记录数
         *
         * start = rows * (page - 1)
         */
        query.setStart(0);
        query.setRows(60);
        //(5)设置回显fl，作用：保护隐私数据。因此对其他数据都不显示
//        query.setFields("prod_name", "prod_catalog_name");//只显示商品名称和种类，对价格和其他数据则获取为null
        //(6)设置默认域df
//        query.set("df","prod_name");
        //(7)设置高亮hl
        query.setHighlight(true);//启动高亮设置
        query.addHighlightField("prod_name");//指定域高亮
        query.setHighlightSimplePre("<font color='red'>");//设置前缀
        query.setHighlightSimplePost("</font>");//设置后缀


        QueryResponse queryResponse = client.query(query);

        Map<String, Map<String, List<String>>> map = queryResponse.getHighlighting();
        List<Products> productsList = queryResponse.getBeans(Products.class);
        System.out.println(productsList.size());
        for (Products p : productsList) {

            String id = p.getPid();
            Map<String, List<String>> map1 = map.get(id);
            List<String> map2 = map1.get("prod_name");

            if (map1 != null) {
                System.out.println(map2.get(0) + p);
            } else
                System.out.println(p);
        }
        //4.关闭资源
        client.close();
	}


}