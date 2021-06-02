package org.jeecg.modules.songdd.entity;

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
 * @Description: visitinfo2
 * @Author: jeecg-boot
 * @Date:   2021-06-01
 * @Version: V1.0
 */
@Data
@TableName("visitinfo2")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="visitinfo2对象", description="visitinfo2")
public class Visitinfo2 implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**visitedname*/
	@Excel(name = "visitedname", width = 15)
    @ApiModelProperty(value = "visitedname")
    private String visitedname;
	/**visitednum*/
	@Excel(name = "visitednum", width = 15)
    @ApiModelProperty(value = "visitednum")
    private String visitednum;
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
	/**visitname*/
	@Excel(name = "visitname", width = 15)
    @ApiModelProperty(value = "visitname")
    private String visitname;
	/**visitnum*/
	@Excel(name = "visitnum", width = 15)
    @ApiModelProperty(value = "visitnum")
    private String visitnum;
	/**identitynum*/
	@Excel(name = "identitynum", width = 15)
    @ApiModelProperty(value = "identitynum")
    private String identitynum;
	/**carnum*/
	@Excel(name = "carnum", width = 15)
    @ApiModelProperty(value = "carnum")
    private String carnum;
	/**imgurl*/
	@Excel(name = "imgurl", width = 15)
    @ApiModelProperty(value = "imgurl")
    private String imgurl;
}
