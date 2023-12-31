package com.likelion.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public  class ApiServiceResponse<T>{
    private Integer statusCode;
    private T body;
}
