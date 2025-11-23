package com.software.dev.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_token")
public class SysToken {
    /**
     * 请求id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tokenId;

    @Column(name = "token_value")
    private String tokenValue;
}
