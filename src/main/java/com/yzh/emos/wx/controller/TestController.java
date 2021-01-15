package com.yzh.emos.wx.controller;

import com.yzh.emos.wx.controller.form.TestSayHelloForm;
import com.yzh.emos.wx.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
@Api("测试接口") //可以让Swagger扫描此类
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("测试yzh")
    public R sayHello(@Valid @RequestBody TestSayHelloForm testSayHelloForm){
        return R.ok().put("message","hello"+testSayHelloForm.getName());
    }
}
