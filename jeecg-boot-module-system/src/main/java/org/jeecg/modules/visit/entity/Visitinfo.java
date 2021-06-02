package org.jeecg.modules.visit.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: visitinfo
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
@Data
@TableName("visitinfo")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="visitinfo对象", description="visitinfo")
public class Visitinfo implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**visitedname*/
	@Excel(name = "visitedname", width = 15)
    @ApiModelProperty(value = "visitedname")
    private String visitedname;
	/**visitedpnum*/
	@Excel(name = "visitedpnum", width = 15)
    @ApiModelProperty(value = "visitedpnum")
    private String visitedpnum;
	/**visitedevent*/
	@Excel(name = "visitedevent", width = 15)
    @ApiModelProperty(value = "visitedevent")
    private String visitedevent;
	/**visittime*/
	@Excel(name = "visittime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "visittime")
    private Date visittime;
	/**lefttime*/
	@Excel(name = "lefttime", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "lefttime")
    private Date lefttime;
	/**visitorname*/
	@Excel(name = "visitorname", width = 15)
    @ApiModelProperty(value = "visitorname")
    private String visitorname;
	/**visitorpnum*/
	@Excel(name = "visitorpnum", width = 15)
    @ApiModelProperty(value = "visitorpnum")
    private String visitorpnum;
	/**identitynum*/
	@Excel(name = "identitynum", width = 15)
    @ApiModelProperty(value = "identitynum")
    private String identitynum;
	/**platenum*/
	@Excel(name = "platenum", width = 15)
    @ApiModelProperty(value = "platenum")
    private String platenum;
	/**imgurl*/
	@Excel(name = "imgurl", width = 15)
    @ApiModelProperty(value = "imgurl")
    private String imgurl;
	/**status*/
	@Excel(name = "status", width = 15)
    @ApiModelProperty(value = "status")
    private String status;
    /**createtime*/
    @Excel(name = "createtime", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "create_ime")
    private Date createtime;
}
