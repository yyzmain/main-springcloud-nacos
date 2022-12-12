package com.main.springcloud.interfaces.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class TestVO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date today;
}
