package com.software.dev.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 *  url_response
 * @author 大狼狗 2019-03-15
 */
@Data
@Entity
@Table(name = "url_response")
public class UrlResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 响应id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private String responseId;

    /**
     * 请求id
     */
    @Column(name = "request_id")
    private String requestId;

    /**
     * 应答时间
     */
    @Column(name = "response_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date responseTime;
    /**
     * 请求时间
     */
    @Column(name = "request_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date requestTime;

    /**
     * 响应内容
     */
    @Column(name = "response_text")
    private String responseText;

    /**
     * 状态：0失败 1成功 9无响应
     */
    private Integer status;
    /**
     * 推断结果
     */
    @Column(name = "assumption_result")
    private String assumptionResult;

    public UrlResponse() {
    }

}