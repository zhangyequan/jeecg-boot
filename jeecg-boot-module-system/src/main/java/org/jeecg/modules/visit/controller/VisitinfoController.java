package org.jeecg.modules.visit.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.alibaba.fastjson.serializer.ValueFilter;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.visit.entity.Visitinfo;
import org.jeecg.modules.visit.service.IVisitinfoService;
import org.jeecg.modules.visit.service.impl.UploadService;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.visit.util.BASE64DecodedMultipartFile;
import org.jeecg.modules.visit.util.DateValueFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: visitinfo
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
@Api(tags="visitinfo")
@RestController
@RequestMapping("/visit/visitinfo")
@Slf4j
public class VisitinfoController extends JeecgController<Visitinfo, IVisitinfoService> {
	@Autowired
	private IVisitinfoService visitinfoService;
	@Autowired
	private UploadService uploadService;

	 @PostMapping("/uploadinfo")
	 public Result<?> uploadInfo(@RequestBody String params) throws UnsupportedEncodingException, ParseException {
		 JSONObject jsonObj = JSONObject.parseObject(params);
		 MultipartFile file = BASE64DecodedMultipartFile.base64ToMultipart(jsonObj.get("file").toString());
		 JSONObject result = uploadService.uploadInfo(file,"/Users/macpro/Desktop/imgs");
		 if (result.get("messageCode").equals("1")){//如果处理成功
			 Visitinfo visit= new Visitinfo();
			 visit.setVisitedname(jsonObj.getString("visitedname"));
			 visit.setVisitedpnum(jsonObj.getString("visitedpnum"));
			 visit.setVisitedevent(jsonObj.getString("visitedevent"));
			 visit.setVisittime(jsonObj.getDate("visittime"));
			 visit.setLefttime(jsonObj.getDate("lefttime"));
			 visit.setVisitorname(jsonObj.getString("visitorname"));
			 visit.setIdentitynum(jsonObj.getString("identitynum"));
			 visit.setPlatenum(jsonObj.getString("platenum"));
			 visit.setVisitorpnum(jsonObj.getString("visitorpnum"));
			 visit.setImgurl(result.getString("imgUrl"));
			 visit.setStatus("0");//待确认
			 visit.setCreatetime(new Date());

			 visitinfoService.save(visit);
		 }
		 return Result.OK("添加成功！");
	 }

	 /**
	  *   审批
	  *
	  * @param params
	  * @return
	  */
	 @AutoLog(value = "visitinfo-审批")
	 @ApiOperation(value="visitinfo-审批", notes="visitinfo-审批")
	 @PostMapping(value = "/approve")
	 public Result<?> approve(@RequestBody String params) {
		 JSONObject jsonObj = JSONObject.parseObject(params);
		 int change = visitinfoService.approve(jsonObj.getString("id"));
		 if(change == 1){
			 return Result.OK("审批成功！");
		 }else{
			 return Result.error("审批失败！");
		 }

	 }
	
	/**
	 * 分页列表查询
	 *
	 * @param params
	 * @return
	 */
	@AutoLog(value = "visitinfo-分页列表查询")
	@ApiOperation(value="visitinfo-分页列表查询", notes="visitinfo-分页列表查询")
	@PostMapping(value = "/list")
	public Result<IPage<Visitinfo>> queryPageList(@RequestBody String params ) {
		JSONObject jsonObj = JSONObject.parseObject(params);
		Result<IPage<Visitinfo>> result = new Result<IPage<Visitinfo>>();
		Page<Visitinfo> page = new Page<Visitinfo>(jsonObj.getLong("pageNo"), jsonObj.getLong("pageSize"));
		IPage<Visitinfo> pageList = visitinfoService.getByStatus(page, jsonObj.getString("status"));
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	/**
	 *   添加
	 *
	 * @param visitinfo
	 * @return
	 */
	@AutoLog(value = "visitinfo-添加")
	@ApiOperation(value="visitinfo-添加", notes="visitinfo-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody Visitinfo visitinfo) {
		visitinfoService.save(visitinfo);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param visitinfo
	 * @return
	 */
	@AutoLog(value = "visitinfo-编辑")
	@ApiOperation(value="visitinfo-编辑", notes="visitinfo-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody Visitinfo visitinfo) {
		visitinfoService.updateById(visitinfo);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "visitinfo-通过id删除")
	@ApiOperation(value="visitinfo-通过id删除", notes="visitinfo-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		visitinfoService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "visitinfo-批量删除")
	@ApiOperation(value="visitinfo-批量删除", notes="visitinfo-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.visitinfoService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "visitinfo-通过id查询")
	@ApiOperation(value="visitinfo-通过id查询", notes="visitinfo-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		Visitinfo visitinfo = visitinfoService.getById(id);
		if(visitinfo==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(visitinfo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param visitinfo
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Visitinfo visitinfo) {
        return super.exportXls(request, visitinfo, Visitinfo.class, "visitinfo");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, Visitinfo.class);
    }

}
