package com.software.dev.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "url_response_assumption")
public class UrlResponseAssumption implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求id
     */
    @Id
    @Column(name = "request_id")
    private String requestId;

    @Column(name = "key_first")
    private String keyFirst;
    
    @Column(name = "value_first")
    private String valueFirst;
    
    @Column(name = "key_second")
    private String keySecond;
    
    @Column(name = "value_second")
    private String valueSecond;
    
    @Column(name = "key_third")
    private String keyThird;
    
    @Column(name = "value_third")
    private String valueThird;
    
    @Column(name = "value_else")
    private String valueElse;

}
