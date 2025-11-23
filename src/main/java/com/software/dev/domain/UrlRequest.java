package com.software.dev.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *  url_request
 * @author 大狼狗 2019-03-11
 */
@Data
@Entity
@Table(name = "url_request")
public class UrlRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private String requestId;

    /**
     * 请求名称
     */
    @Column(name = "request_name")
    private String requestName;

    /**
     * 触发时间cron表达式
     */
    @Column(name = "request_cron")
    private String requestCron;

    /**
     * 请求url
     */
    @Column(name = "request_url")
    private String requestUrl;

    /**
     * 请求分组
     */
    @Column(name = "request_group")
    private String requestGroup;

    /**
     * 请求方法，get/post
     */
    @Column(name = "request_method")
    private String requestMethod;

    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 超时时间ms
     */
    @Column(name = "request_timeout")
    private Integer requestTimeout;
    /**
     * 更新时间
     */
    @Column(name = "update_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Transient
    private String triggerState;

    @Transient
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String nextFireTime;

    public UrlRequest() {
    }
    @Data
    public static class RequestMethod{
        public static String GET="GET";
        public static String POST="POST";
    }

    @Data
    /**
     *                      case 0:return '停止';
     *                     case 1:return '启动';
     *                     case  2:return '禁用';
     *                     case 3:return '暂停';
     * */
    public static class RequestStatus{
        public static Integer STOP=0;
        public static Integer START=1;
    }
}