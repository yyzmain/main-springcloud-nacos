package com.main.springcloud.interfaces.controller;

import com.main.springcloud.application.service.TestService;
import com.main.springcloud.domain.feign.TestFeign;
import com.main.springcloud.interfaces.vo.TestVO;
import com.main.utils.MainSysUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Date;

@RestController
@RequestMapping("test")
@Slf4j
public class TestController {

    @Autowired
    private TestFeign testFeign;

    @Value("${abc:aaa}")
    private String abc;

    @GetMapping("/a")
    public String a() {
        log.info("11111111111111111111");
        testFeign.b();
        log.info("aaaaaaaaaaaaaaaaaaaa");
        testFeign.d();
        log.info("aaaaaaaaaa2222222");
        return "a";
    }

    @GetMapping("/b")
    public String b() throws InterruptedException {

        log.info("bbbbbbbbbbbbbbbbbbbb:{}", MainSysUtil.getUserId());
        Thread.sleep(30000);
        log.info("bbbbbbbbbbbb end");
        return "b";
    }

    @GetMapping("/c")
    public String c() throws InterruptedException {
        log.info("cccccccccccccccccccccc");
        Thread.sleep(2000);
        if (true) {
            throw new RuntimeException("test");
        }
        log.info("c end");
        return "c";
    }

    @GetMapping("/d")
    public String d() throws InterruptedException {
        testFeign.c();
        log.info("dddddddddddd");
        return "d";
    }

    @GetMapping("/f")
    public Date f(@RequestParam(value = "today") Date today, @RequestParam(value = "today2") @DateTimeFormat(pattern = "yyyy-MM-dd") Date today2) {

        log.info("today:{},today2:{}", today, today2);
        return today;
    }

    @Autowired
    private TestService testService;

    @GetMapping("/g")
    public TestVO g() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        log.trace("g trace");
        log.debug("g debug");
        log.info("g info");
        log.warn("g warn");
        log.error("g error");
        TestVO vo = new TestVO();
        vo.setToday(new Date());
        test();
        log.info("g end");
        testService.test();
        log.info("g end 2");
        return vo;
    }

    @Async
    void test() {
        log.info("test begin");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("test end");
    }
}
