package org.jeecg.modules.songdd.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.songdd.entity.Visitinfo2;
import org.jeecg.modules.songdd.service.IVisitinfo2Service;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: visitinfo2
 * @Author: jeecg-boot
 * @Date:   2021-06-01
 * @Version: V1.0
 */
@Api(tags="visitinfo2")
@RestController
@RequestMapping("/songdd/visitinfo2")
@Slf4j
public class Visitinfo2Controller extends JeecgController<Visitinfo2, IVisitinfo2Service> {
	@Autowired
	private IVisitinfo2Service visitinfo2Service;
	
	/**
	 * 分页列表查询
	 *
	 * @param visitinfo2
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "visitinfo2-分页列表查询")
	@ApiOperation(value="visitinfo2-分页列表查询", notes="visitinfo2-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(Visitinfo2 visitinfo2,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<Visitinfo2> queryWrapper = QueryGenerator.initQueryWrapper(visitinfo2, req.getParameterMap());
		Page<Visitinfo2> page = new Page<Visitinfo2>(pageNo, pageSize);
		IPage<Visitinfo2> pageList = visitinfo2Service.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param visitinfo2
	 * @return
	 */
	@AutoLog(value = "visitinfo2-添加")
	@ApiOperation(value="visitinfo2-添加", notes="visitinfo2-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody Visitinfo2 visitinfo2) {
		visitinfo2Service.save(visitinfo2);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param visitinfo2
	 * @return
	 */
	@AutoLog(value = "visitinfo2-编辑")
	@ApiOperation(value="visitinfo2-编辑", notes="visitinfo2-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody Visitinfo2 visitinfo2) {
		visitinfo2Service.updateById(visitinfo2);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "visitinfo2-通过id删除")
	@ApiOperation(value="visitinfo2-通过id删除", notes="visitinfo2-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		visitinfo2Service.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "visitinfo2-批量删除")
	@ApiOperation(value="visitinfo2-批量删除", notes="visitinfo2-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.visitinfo2Service.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "visitinfo2-通过id查询")
	@ApiOperation(value="visitinfo2-通过id查询", notes="visitinfo2-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		Visitinfo2 visitinfo2 = visitinfo2Service.getById(id);
		if(visitinfo2==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(visitinfo2);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param visitinfo2
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Visitinfo2 visitinfo2) {
        return super.exportXls(request, visitinfo2, Visitinfo2.class, "visitinfo2");
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
        return super.importExcel(request, response, Visitinfo2.class);
    }

}
