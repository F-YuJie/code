package com.bw.fyj.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "weekth_mail")
@Data
public class Mail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Field(value = "id")
    private int mid;

    @Field(value = "project")
    private String project; //主题

    @Field(value = "emailcontent")
    private String emailcontent; //内容

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Field(value = "sendtime")
    private Date sendtime;  //发送时间

    @Field(value = "status")
    private int status;     //编辑状态  0:草稿    1：已发送

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "sendid")
//    @Field(value = "senduser") Solr 不支持多层次嵌套。。.
    private User senduser;  //寄件人

    @Field(value = "senduserid")
    private int solr_sendid;    //Solr 寄件人ID

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "recid")
//    @Field(value = "recuser")

    private User recuser;   //收件人
    @Field(value = "recuserid")
    private int solr_recid;    //Solr 收件人ID


}
