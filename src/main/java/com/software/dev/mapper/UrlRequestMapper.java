package com.software.dev.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.dev.domain.UrlRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface UrlRequestMapper extends BaseMapper<UrlRequest> {

    @Select("""
        <script>
         SELECT t.*, tri.TRIGGER_STATE, tri.NEXT_FIRE_TIME FROM url_request t 
         LEFT JOIN qrtz_triggers tri ON t.request_id = tri.JOB_NAME WHERE 1=1 
        <if test='search!=null'> AND (t.request_name LIKE CONCAT('%',#{search},'%') OR t.request_group LIKE CONCAT('%',#{search},'%'))</if>
         ORDER BY t.request_id DESC 
        <if test='pageStart!=null and pageSize != null'> LIMIT #{pageStart}, #{pageSize}</if>
        </script>
        """)
    List<UrlRequest> listUrl(Integer pageStart,Integer pageSize,String search);

}
