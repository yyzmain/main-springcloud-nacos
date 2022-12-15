package com.main.springcloud.domain.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestFeignImpl implements TestFeign{

    @Override
    public String b() {
        log.error("b error");
        return "b error";
    }

    @Override
    public String c() {
        return "c error";
    }

    @Override
    public String d() {
        return "d error";
    }
}
