package com.software.dev.domain;
import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 *  url_request_token
 * @author 大狼狗 2019-04-28
 */
@Data
@Entity
@Table(name = "url_request_token")
public class UrlRequestToken implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求id
     */
    @Id
    @Column(name = "request_id")
    private String requestId;

    /**
     * TOKEN请求地址
     */
    @Column(name = "token_url")
    private String tokenUrl;

    /**
     * 请求方式
     */
    private String method;

    /**
     * 请求参数
     */
    private String param;
    /**
     * 参数方式：1form表单 2json格式/xml
     */
    @Column(name = "param_type")
    private String paramType;

    /**
     * 追加方式：1通过url追加 2通过form追加
     */
    @Column(name = "append_type")
    private String appendType;

    /**
     * 追加参数key名
     */
    @Column(name = "append_name")
    private String appendName;

    /**
     * TOKEN表达式
     */
    @Column(name = "token_expression")
    private String tokenExpression;

    /**
     * 是否启动：1启用 0禁用
     */
    private Integer status;

    public UrlRequestToken() {
    }
    @Data
    public static class Method{
        public static String GET="GET";
        public static String POST="POST";
    }
    @Data
    public static class ParamType{
        public static String FORM="FORM";
        public static String JSON="JSON";
    }
    @Data
    public static class AppendType{
        public static String URL="URL";
        public static String FORM="FORM";
    }
}