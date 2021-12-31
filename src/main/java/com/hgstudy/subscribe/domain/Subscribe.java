package com.hgstudy.subscribe.domain;

import com.hgstudy.content.constant.ContentType;
import com.hgstudy.service.constant.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Subscribe {

    private Long idx;
    private String register;
    private ServiceType serviceType;
    private ContentType contentType;
    private Long contentIdx;
    private Long hit;
//    private Integer version;

    public Subscribe(String register, ServiceType serviceType, ContentType contentType, Long contentIdx) {
        this.register = register;
        this.serviceType = serviceType;
        this.contentType = contentType;
        this.contentIdx = contentIdx;
    }
}
