package com.main.springcloud.domain.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Component
@FeignClient(name = "nl-bomc-springcloud",fallback = TestFeignImpl.class)
public interface TestFeign {
    @GetMapping("/nl-bomc-springcloud/test/b")
    public String b();

    @GetMapping("/nl-bomc-springcloud/test/c")
    public String c();

    @GetMapping("/nl-bomc-springcloud/test/d")
    public String d();
}
