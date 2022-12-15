package com.main.springcloud.domain.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@Component
@FeignClient(name = "main-springcloud",fallback = TestFeignImpl.class)
public interface TestFeign {

    @GetMapping("/main-springcloud/test/b")
    String b();

    @GetMapping("/main-springcloud/test/c")
    String c();

    @GetMapping("/main-springcloud/test/d")
    String d();
}
