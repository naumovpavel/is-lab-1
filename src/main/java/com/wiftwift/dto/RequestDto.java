package com.wiftwift.dto;

import lombok.Data;

@Data
public class RequestDto {
    private Long requestId;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
}
