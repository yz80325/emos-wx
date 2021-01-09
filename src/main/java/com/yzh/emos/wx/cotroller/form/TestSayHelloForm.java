package com.yzh.emos.wx.cotroller.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class TestSayHelloForm {

    //@Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$")//汉字为2-15个长度
    //@NotBlank
    @ApiModelProperty("姓名")
    private String name;
}
