package org.jeecg.modules.activiti.web;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.entity.ActNode;
import org.jeecg.modules.activiti.entity.ActZprocess;
import org.jeecg.modules.activiti.entity.ProcessNodeVo;
import org.jeecg.modules.activiti.service.Impl.ActBusinessServiceImpl;
import org.jeecg.modules.activiti.service.Impl.ActNodeServiceImpl;
import org.jeecg.modules.activiti.service.Impl.ActZprocessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/activiti_process")
@Slf4j
@Transactional
public class ActivitiProcessController {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ActZprocessServiceImpl actZprocessService;
    @Autowired
    private ActNodeServiceImpl actNodeService;
    @Autowired
    private ActBusinessServiceImpl actBusinessService;

    @RequestMapping("/listData")
    public Result listData(@RequestParam(value = "status",required = false) String status,@RequestParam(value = "roles",required = false) String roles,
                           @RequestParam(value = "lcmc",required = false) String lcmc,@RequestParam(value = "lckey",required = false) String lckey,
                           @RequestParam(value = "zx",required = false) String zx){
        log.info("-------------????????????-------------");
        LambdaQueryWrapper<ActZprocess> wrapper = new LambdaQueryWrapper<ActZprocess>();
        wrapper.orderByAsc(ActZprocess::getProcessKey).orderByDesc(ActZprocess::getVersion);
        if (StrUtil.isNotBlank(lcmc)){
            wrapper.like(ActZprocess::getName, lcmc);
        }
        if (StrUtil.isNotBlank(lckey)){
            wrapper.like(ActZprocess::getProcessKey, lckey);
        }
        if (StrUtil.equals(zx,"true")){
            wrapper.eq(ActZprocess::getLatest, 1);
        }
        if (StrUtil.isNotBlank(status)){
            wrapper.eq(ActZprocess::getStatus, status);
        }
        List<ActZprocess> list = actZprocessService.list(wrapper);
        if (StrUtil.isNotBlank(roles)){ //????????????
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            List<String> roleByUserName = actNodeService.getRoleByUserName(sysUser.getUsername());
            list = list.stream().filter(p->{
                String roles2 = p.getRoles();
                if (StrUtil.isBlank(roles2)) {
                    return true; //????????????????????????????????????
                }else {
                    String[] split = roles2.split(",");
                    for (String role : split) {
                        if (roleByUserName.contains(role)){
                            return true;
                        }
                    }
                }
                return false;
            }).collect(Collectors.toList());

        }
        return Result.ok(list);
    }

    /*???????????????????????????*/
    @RequestMapping(value = "/updateStatus")
    public Result updateStatus( @RequestParam(name = "id", required = true) String id, @RequestParam(name = "status", required = true) Integer status){

        ActZprocess actProcess = actZprocessService.getById(id);
        if(status==1){
            //????????????????????? ???????????????????????????
            String routeName = actProcess.getRouteName();
            String businessTable = actProcess.getBusinessTable();
            if (StrUtil.isBlank(routeName)||StrUtil.isBlank(businessTable)){
                return Result.error("?????????????????????????????????????????????");
            }

            repositoryService.activateProcessDefinitionById(id, true, new Date());
        }else {
            repositoryService.suspendProcessDefinitionById(id, true, new Date());
        }
        actProcess.setStatus(status);
        actZprocessService.updateById(actProcess);
        return Result.ok("???????????????");
    }
    /*??????id????????????*/
    @RequestMapping(value = "/delByIds")
    public Result<Object> delByIds(String ids){
        for(String id : ids.split(",")){
            if(CollectionUtil.isNotEmpty(actBusinessService.findByProcDefId(id))){
                return Result.error("?????????????????????????????????????????????");
            }
            ActZprocess actProcess = actZprocessService.getById(id);
            // ?????????????????????????????? ??????????????????
            if (actProcess==null) return Result.error("?????????????????????");
            if(actProcess.getVersion()==1){
                deleteNodeUsers(id);
            }
            // ????????????
            repositoryService.deleteDeployment(actProcess.getDeploymentId(), true);
            actZprocessService.removeById(id);
            // ??????????????????
            actZprocessService.setLatestByProcessKey(actProcess.getProcessKey());
        }
        return Result.ok("????????????");
    }
    public void deleteNodeUsers(String processId){

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processId);
        List<Process> processes = bpmnModel.getProcesses();
        for(Process process : processes){
            Collection<FlowElement> elements = process.getFlowElements();
            for(FlowElement element : elements) {
                actNodeService.deleteByNodeId(element.getId());
            }
        }
    }
    /**
     * ?????????????????????
     * @param id
     * @return
     */
    @RequestMapping(value = "/convertToModel")
    public Result convertToModel( String id){

        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        InputStream bpmnStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
        ActZprocess actProcess = actZprocessService.getById(id);

        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
            BpmnJsonConverter converter = new BpmnJsonConverter();

            ObjectNode modelNode = converter.convertToJson(bpmnModel);
            Model modelData = repositoryService.newModel();
            modelData.setKey(pd.getKey());
            modelData.setName(pd.getResourceName());

            ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, actProcess.getName());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, actProcess.getDescription());
            modelData.setMetaInfo(modelObjectNode.toString());

            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));

        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.error("???????????????????????????");
        }
        return Result.ok("????????????");
    }
    @RequestMapping(value = "/updateInfo", method = RequestMethod.POST)
    public Result<Object> updateInfo(@RequestBody ActZprocess actProcess){

        ProcessDefinition pd = repositoryService.getProcessDefinition(actProcess.getId());
        if(pd==null){
            return Result.error("?????????????????????");
        }
        if(StrUtil.isNotBlank(actProcess.getCategoryId())){
            repositoryService.setProcessDefinitionCategory(actProcess.getId(), actProcess.getCategoryId());
            repositoryService.setDeploymentCategory(pd.getDeploymentId(), actProcess.getCategoryId());
        }
        actZprocessService.updateById(actProcess);
        return Result.ok("????????????");
    }
    /*??????????????????id??????????????????*/
    @RequestMapping(value = "/getProcessNode")
    public Result getProcessNode(String id){

        BpmnModel bpmnModel = repositoryService.getBpmnModel(id);

        List<ProcessNodeVo> list = new ArrayList<>();

        List<Process> processes = bpmnModel.getProcesses();
        if(processes==null||processes.size()==0){
            return Result.ok();
        }
        for(Process process : processes){
            Collection<FlowElement> elements = process.getFlowElements();
            for(FlowElement element : elements){
                ProcessNodeVo node = new ProcessNodeVo();
                node.setId(element.getId());
                node.setTitle(element.getName());
                if(element instanceof StartEvent){
                    // ????????????
                    node.setType(0);
                }else if(element instanceof UserTask){
                    // ????????????
                    node.setType(1);
                    // ??????????????????
                    node.setUsers(actNodeService.findUserByNodeId(element.getId()));
                    // ??????????????????
                    node.setRoles(actNodeService.findRoleByNodeId(element.getId()));
                    // ??????????????????
                    node.setDepartments(actNodeService.findDepartmentByNodeId(element.getId()));
                    // ????????????????????????????????????
                    node.setChooseDepHeader(actNodeService.hasChooseDepHeader(element.getId()));
                    // ?????????????????????
                    node.setChooseSponsor(actNodeService.hasChooseSponsor(element.getId()));
                }else if(element instanceof EndEvent){
                    // ??????
                    node.setType(2);
                }else{
                    // ???????????????????????????
                    continue;
                }
                list.add(node);
            }
        }
        list.sort(Comparator.comparing(ProcessNodeVo::getType));
        return Result.ok(list);
    }

    /**
     * ????????????????????????
     * @param nodeId
     * @param userIds
     * @param roleIds
     * @param departmentIds
     * @param chooseDepHeader ???????????????????????????????????????
     * @return
     */
    @GetMapping(value = "/editNodeUser")
    public Result editNodeUser(String nodeId, String userIds, String roleIds, String departmentIds, Boolean chooseDepHeader, Boolean chooseSponsor){

        // ?????????????????????
        actNodeService.deleteByNodeId(nodeId);
        // ???????????????
        for(String userId : userIds.split(",")){
            ActNode actNode = new ActNode();
            actNode.setNodeId(nodeId);
            actNode.setRelateId(userId);
            actNode.setType(1);
            actNodeService.save(actNode);
        }
        // ???????????????
        for(String roleId : roleIds.split(",")){
            ActNode actNode = new ActNode();
            actNode.setNodeId(nodeId);
            actNode.setRelateId(roleId);
            actNode.setType(0);
            actNodeService.save(actNode);
        }
        // ???????????????
        for(String departmentId : departmentIds.split(",")){
            ActNode actNode = new ActNode();
            actNode.setNodeId(nodeId);
            actNode.setRelateId(departmentId);
            actNode.setType(2);
            actNodeService.save(actNode);
        }
        if(chooseDepHeader!=null&&chooseDepHeader){
            ActNode actNode = new ActNode();
            actNode.setNodeId(nodeId);
            actNode.setType(4);
            actNodeService.save(actNode);
        }
        if(chooseSponsor!=null&&chooseSponsor){
            ActNode actNode = new ActNode();
            actNode.setNodeId(nodeId);
            actNode.setType(3);
            actNodeService.save(actNode);
        }
        return Result.ok("????????????");
    }
    @RequestMapping(value = "/getNextNode", method = RequestMethod.GET)
    @ApiOperation(value = "????????????????????????id?????????????????????")
    public Result getNextNode(@ApiParam("??????????????????id")  String procDefId,
                                             @ApiParam("??????????????????id")  String currActId){
        ProcessNodeVo node = actZprocessService.getNextNode(procDefId, currActId);
        return Result.ok(node);
    }
    @RequestMapping(value = "/getNode/{nodeId}", method = RequestMethod.GET)
    @ApiOperation(value = "????????????nodeId???????????????")
    public Result getNode(@ApiParam("??????nodeId") @PathVariable String nodeId){

        ProcessNodeVo node = actZprocessService.getNode(nodeId);
        return Result.ok(node);
    }

}
