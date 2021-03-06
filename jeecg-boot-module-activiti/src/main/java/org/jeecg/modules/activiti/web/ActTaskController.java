package org.jeecg.modules.activiti.web;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.ComboModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.entity.*;
import org.jeecg.modules.activiti.service.Impl.ActBusinessServiceImpl;
import org.jeecg.modules.activiti.service.Impl.ActZprocessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pmc
 */
@Slf4j
@RestController
@RequestMapping("/actTask")
@Transactional
public class ActTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private ActZprocessServiceImpl actZprocessService;

    @Autowired
    private ActBusinessServiceImpl actBusinessService;
    @Autowired
    ISysBaseAPI sysBaseAPI;

    /*????????????*/
    @RequestMapping(value = "/todoList" )
    public Result<Object> todoList(String name, String categoryId, Integer priority, HttpServletRequest request){
        List<TaskVo> list = new ArrayList<>();
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = sysUser.getUsername();
        TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(userId);
        // ???????????????
        query.orderByTaskPriority().desc();
        query.orderByTaskCreateTime().desc();
        if(StrUtil.isNotBlank(name)){
            query.taskNameLike("%"+name+"%");
        }
        if(StrUtil.isNotBlank(categoryId)){
            query.taskCategory(categoryId);
        }
        if(priority!=null){
            query.taskPriority(priority);
        }
        String createTime_begin = request.getParameter("createTime_begin");
        String createTime_end = request.getParameter("createTime_end");
        if(StrUtil.isNotBlank(createTime_begin)&&StrUtil.isNotBlank(createTime_end)){
            Date start = DateUtil.parse(createTime_begin);
            Date end = DateUtil.parse(createTime_end);
            query.taskCreatedAfter(start);
            query.taskCreatedBefore(DateUtil.endOfDay(end));
        }

        List<Task> taskList = query.list();

        // ??????vo
        taskList.forEach(e -> {
            TaskVo tv = new TaskVo(e);

            // ???????????????
            if(StrUtil.isNotBlank(tv.getOwner())){
                String realname = sysBaseAPI.getUserByName(tv.getOwner()).getRealname();
                tv.setOwner(realname);
            }
            List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(tv.getProcInstId());
            for(IdentityLink ik : identityLinks){
                // ???????????????
                if("starter".equals(ik.getType())&&StrUtil.isNotBlank(ik.getUserId())){
                    tv.setApplyer(sysBaseAPI.getUserByName(ik.getUserId()).getRealname());
                }
            }
            // ??????????????????
            ActZprocess actProcess = actZprocessService.getById(tv.getProcDefId());
            if(actProcess!=null){
                tv.setProcessName(actProcess.getName());
                tv.setRouteName(actProcess.getRouteName());
            }
            // ????????????key
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(tv.getProcInstId()).singleResult();
            tv.setBusinessKey(pi.getBusinessKey());
            ActBusiness actBusiness = actBusinessService.getById(pi.getBusinessKey());
            if(actBusiness!=null){
                tv.setTableId(actBusiness.getTableId());
                tv.setTableName(actBusiness.getTableName());
            }

            list.add(tv);
        });
        return Result.ok(list);
    }
    /*????????????????????????*/
    @RequestMapping(value = "/getBackList/{procInstId}", method = RequestMethod.GET)
    public Result<Object> getBackList(@PathVariable String procInstId){

        List<HistoricTaskVo> list = new ArrayList<>();
        List<HistoricTaskInstance> taskInstanceList = historyService.createHistoricTaskInstanceQuery().processInstanceId(procInstId)
                .finished().list();

        taskInstanceList.forEach(e -> {
            HistoricTaskVo htv = new HistoricTaskVo(e);
            list.add(htv);
        });

        // ??????
        LinkedHashSet<String> set = new LinkedHashSet<String>(list.size());
        List<HistoricTaskVo> newList = new ArrayList<>();
        list.forEach(e->{
            if(set.add(e.getName())){
                newList.add(e);
            }
        });

        return Result.ok(newList);
    }
    /*?????????????????? ??????????????????*/
    @RequestMapping(value = "/back", method = RequestMethod.POST)
    public Result<Object> back(@ApiParam("??????id") @RequestParam String id,
                               @ApiParam("????????????id") @RequestParam String procInstId,
                               @ApiParam("????????????") @RequestParam(required = false) String comment,
                               @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendMessage,
                               @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendSms,
                               @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendEmail){


        if(StrUtil.isBlank(comment)){
            comment = "";
        }
        taskService.addComment(id, procInstId, comment);
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        // ??????????????????
        runtimeService.deleteProcessInstance(procInstId, "backed");
        ActBusiness actBusiness = actBusinessService.getById(pi.getBusinessKey());
        actBusiness.setStatus(ActivitiConstant.STATUS_FINISH);
        actBusiness.setResult(ActivitiConstant.RESULT_FAIL);
        actBusinessService.updateById(actBusiness);
        // ???????????????
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        actZprocessService.sendMessage(sysUser,sysBaseAPI.getUserByName(actBusiness.getUserId()),ActivitiConstant.MESSAGE_BACK_CONTENT,
                String.format("?????? ???%s??? ?????????????????????",actBusiness.getTitle()),sendMessage, sendSms, sendEmail);
        // ????????????????????????
        actBusinessService.insertHI_IDENTITYLINK(IdUtil.simpleUUID(),
                ActivitiConstant.EXECUTOR_TYPE, sysUser.getUsername(), id, procInstId);
        //??????????????????????????????
        actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"??????");
        return Result.ok("????????????");
    }
    /*??????????????????*/
    @RequestMapping(value = "/historicFlow", method = RequestMethod.GET)
    public Result<Object> historicFlow(@RequestParam String id){

        List<HistoricTaskVo> list = new ArrayList<>();

        List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(id).orderByHistoricTaskInstanceEndTime().asc().list();

        // ??????vo
        taskList.forEach(e -> {
            HistoricTaskVo htv = new HistoricTaskVo(e);
            List<Assignee> assignees = new ArrayList<>();
            // ????????????????????????????????????????????????
            if(StrUtil.isNotBlank(htv.getAssignee())){
                String assignee = sysBaseAPI.getUserByName(htv.getAssignee()).getRealname();
                String owner = sysBaseAPI.getUserByName(htv.getOwner()).getRealname();
                assignees.add(new Assignee(assignee+"(???"+owner+"??????)", true));
            }
            List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForTask(e.getId());
            // ????????????????????????id
            String userId = actBusinessService.findUserIdByTypeAndTaskId(ActivitiConstant.EXECUTOR_TYPE, e.getId());
            for(HistoricIdentityLink hik : identityLinks){
                // ??????????????????????????????????????????????????????
                if("candidate".equals(hik.getType())&& StrUtil.isNotBlank(hik.getUserId())){
                    String username = sysBaseAPI.getUserByName(hik.getUserId()).getRealname();
                    Assignee assignee = new Assignee(username, false);
                    if(StrUtil.isNotBlank(userId)&&userId.equals(hik.getUserId())){
                        assignee.setIsExecutor(true);
                    }
                    assignees.add(assignee);
                }
            }
            htv.setAssignees(assignees);
            // ??????????????????
            List<Comment> comments = taskService.getTaskComments(htv.getId(), "comment");
            if(comments!=null&&comments.size()>0){
                htv.setComment(comments.get(0).getFullMessage());
            }
            list.add(htv);
        });
        return Result.ok(list);
    }
    @RequestMapping(value = "/pass", method = RequestMethod.POST)
    @ApiOperation(value = "????????????????????????")
    public Result<Object> pass(@ApiParam("??????id") @RequestParam String id,
                               @ApiParam("????????????id") @RequestParam String procInstId,
                               @ApiParam("?????????????????????") @RequestParam(required = false) String assignees,
                               @ApiParam("?????????") @RequestParam(required = false) Integer priority,
                               @ApiParam("????????????") @RequestParam(required = false) String comment,
                               @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendMessage,
                               @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendSms,
                               @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendEmail){

        if(StrUtil.isBlank(comment)){
            comment = "";
        }
        taskService.addComment(id, procInstId, comment);
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        Task task = taskService.createTaskQuery().taskId(id).singleResult();
        if(StrUtil.isNotBlank(task.getOwner())&&!("RESOLVED").equals(task.getDelegationState().toString())){
            // ???????????????????????? ???resolve
            String oldAssignee = task.getAssignee();
            taskService.resolveTask(id);
            taskService.setAssignee(id, oldAssignee);
        }
        taskService.complete(id);
        ActBusiness actBusiness = actBusinessService.getById(pi.getBusinessKey());
        //??????????????????????????????
        actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"?????????-"+task.getName());

        task.getName();
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInstId).list();
        // ?????????????????????
        if(tasks!=null&&tasks.size()>0){
            for(Task t : tasks){
                if(StrUtil.isBlank(assignees)){
                    // ?????????????????????????????????????????? ??????????????????
                    List<LoginUser> users = actZprocessService.getNode(t.getTaskDefinitionKey()).getUsers();
                    if(users==null||users.size()==0){
                        runtimeService.deleteProcessInstance(procInstId, "canceled-?????????????????????????????????????????????????????????");
                        actBusiness.setStatus(ActivitiConstant.STATUS_CANCELED);
                        actBusiness.setResult(ActivitiConstant.RESULT_TO_SUBMIT);
                        actBusinessService.updateById(actBusiness);
                        //??????????????????????????????
                        actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"????????????-"+task.getName()+"-?????????????????????????????????????????????????????????");

                        break;
                    }else{
                        // ??????????????????
                        List<String> list = actBusinessService.selectIRunIdentity(t.getId(), "candidate");
                        if(list==null||list.size()==0) {
                            // ???????????????????????????????????????
                            for (LoginUser user : users) {
                                taskService.addCandidateUser(t.getId(), user.getId());
                                // ???????????????
                                actZprocessService.sendActMessage(loginUser,user,actBusiness,task.getName(),  sendMessage, sendSms, sendEmail);
                            }
                            taskService.setPriority(t.getId(), task.getPriority());
                        }
                    }
                }else{
                    // ??????????????????
                    List<String> list = actBusinessService.selectIRunIdentity(t.getId(), "candidate");
                    if(list==null||list.size()==0) {

                        for(String assignee : assignees.split(",")){
                            taskService.addCandidateUser(t.getId(), assignee);
                            // ???????????????
                            LoginUser user = sysBaseAPI.getUserByName(assignee);
                            actZprocessService.sendActMessage(loginUser,user,actBusiness,task.getName(),  sendMessage, sendSms, sendEmail);
                            taskService.setPriority(t.getId(), priority);
                        }
                    }
                }
            }
        } else {
            actBusiness.setStatus(ActivitiConstant.STATUS_FINISH);
            actBusiness.setResult(ActivitiConstant.RESULT_PASS);
            actBusinessService.updateById(actBusiness);
            // ???????????????
            LoginUser user = sysBaseAPI.getUserByName(actBusiness.getUserId());
            actZprocessService.sendMessage(loginUser,user,ActivitiConstant.MESSAGE_PASS_CONTENT,
                    String.format("?????? ???%s??? ??????????????????",actBusiness.getTitle()),sendMessage, sendSms, sendEmail);
            //??????????????????????????????
            actBusinessService.updateBusinessStatus(actBusiness.getTableName(), actBusiness.getTableId(),"????????????");

        }
        // ????????????????????????
        actBusinessService.insertHI_IDENTITYLINK(IdUtil.simpleUUID(),
                ActivitiConstant.EXECUTOR_TYPE, loginUser.getUsername(), id, procInstId);
        return Result.ok("????????????");
    }
    @RequestMapping(value = "/delegate", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????")
    public Result<Object> delegate(@ApiParam("??????id") @RequestParam String id,
                                   @ApiParam("????????????id") @RequestParam String userId,
                                   @ApiParam("????????????id") @RequestParam String procInstId,
                                   @ApiParam("????????????") @RequestParam(required = false) String comment,
                                   @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendMessage,
                                   @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendSms,
                                   @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendEmail){

        if(StrUtil.isBlank(comment)){
            comment = "";
        }
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        taskService.addComment(id, procInstId, comment);
        taskService.delegateTask(id, userId);
        taskService.setOwner(id, sysUser.getUsername());
        // ???????????????
        actZprocessService.sendMessage(sysUser,sysBaseAPI.getUserByName(userId),ActivitiConstant.MESSAGE_DELEGATE_CONTENT,
                String.format("?????????????????? %s ????????????????????????",sysUser.getRealname()),sendMessage, sendSms, sendEmail);
        return Result.ok("????????????");
    }
    @RequestMapping(value = "/backToTask", method = RequestMethod.POST)
    @ApiOperation(value = "?????????????????????????????????????????????")
    public Result<Object> backToTask(@ApiParam("??????id") @RequestParam String id,
                                     @ApiParam("??????????????????key") @RequestParam String backTaskKey,
                                     @ApiParam("????????????id") @RequestParam String procInstId,
                                     @ApiParam("????????????id") @RequestParam String procDefId,
                                     @ApiParam("??????????????????") @RequestParam(required = false) String assignees,
                                     @ApiParam("?????????") @RequestParam(required = false) Integer priority,
                                     @ApiParam("????????????") @RequestParam(required = false) String comment,
                                     @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendMessage,
                                     @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendSms,
                                     @ApiParam("????????????????????????") @RequestParam(defaultValue = "false") Boolean sendEmail){


        if(StrUtil.isBlank(comment)){
            comment = "";
        }
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        taskService.addComment(id, procInstId, comment);
        // ??????????????????
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(procDefId);
        // ?????????????????????Activity
        ActivityImpl hisActivity = definition.findActivity(backTaskKey);
        // ????????????
        managementService.executeCommand(new JumpTask(procInstId, hisActivity.getId()));
        // ??????????????????????????????
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInstId).list();
        if(tasks!=null&&tasks.size()>0){
            tasks.forEach(e->{
                for(String assignee:assignees.split(",")){
                    taskService.addCandidateUser(e.getId(), assignee);
                    // ???????????????
                    actZprocessService.sendMessage(loginUser,sysBaseAPI.getUserByName(assignee),ActivitiConstant.MESSAGE_TODO_CONTENT
                    ,"????????????????????????????????????????????????",sendMessage, sendSms, sendEmail);
                }
                if(priority!=null){
                    taskService.setPriority(e.getId(), priority);
                }
            });
        }
        // ????????????????????????
        actBusinessService.insertHI_IDENTITYLINK(IdUtil.simpleUUID(),
                ActivitiConstant.EXECUTOR_TYPE, loginUser.getUsername(), id, procInstId);
        return Result.ok("????????????");
    }

    public class JumpTask implements Command<ExecutionEntity> {

        private String procInstId;
        private String activityId;

        public JumpTask(String procInstId, String activityId) {
            this.procInstId = procInstId;
            this.activityId = activityId;
        }

        @Override
        public ExecutionEntity execute(CommandContext commandContext) {

            ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findExecutionById(procInstId);
            executionEntity.destroyScope("backed");
            ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
            ActivityImpl activity = processDefinition.findActivity(activityId);
            executionEntity.executeActivity(activity);

            return executionEntity;
        }

    }
    /*????????????*/
    @RequestMapping(value = "/doneList")
    public Result<Object> doneList(String name,
                                   String categoryId,
                                   Integer priority,
                                   HttpServletRequest req){

        List<HistoricTaskVo> list = new ArrayList<>();
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = loginUser.getUsername();
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().or().taskCandidateUser(userId).
                taskAssignee(userId).endOr().finished();

        // ???????????????
        query.orderByTaskCreateTime().desc();
        if(StrUtil.isNotBlank(name)){
            query.taskNameLike("%"+name+"%");
        }
        if(StrUtil.isNotBlank(categoryId)){
            query.taskCategory(categoryId);
        }
        if(priority!=null){
            query.taskPriority(priority);
        }
        List<HistoricTaskInstance> taskList = query.list();
        // ??????vo
        List<ComboModel> allUser = (List<ComboModel>) (sysBaseAPI.queryAllUser("",1,10)).get("list");
        Map<String, String> userMap = allUser.stream().collect(Collectors.toMap(ComboModel::getUsername, ComboModel::getTitle));
        taskList.forEach(e -> {
            HistoricTaskVo htv = new HistoricTaskVo(e);
            // ???????????????
            if(StrUtil.isNotBlank(htv.getOwner())){
                htv.setOwner(userMap.get(htv.getOwner()));
            }
            List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForProcessInstance(htv.getProcInstId());
            for(HistoricIdentityLink hik : identityLinks){
                // ???????????????
                if("starter".equals(hik.getType())&&StrUtil.isNotBlank(hik.getUserId())){
                    htv.setApplyer(userMap.get(hik.getUserId()));
                }
            }
            // ??????????????????
            List<Comment> comments = taskService.getTaskComments(htv.getId(), "comment");
            if(comments!=null&&comments.size()>0){
                htv.setComment(comments.get(0).getFullMessage());
            }
            // ??????????????????
            ActZprocess actProcess = actZprocessService.getById(htv.getProcDefId());
            if(actProcess!=null){
                htv.setProcessName(actProcess.getName());
                htv.setRouteName(actProcess.getRouteName());
            }
            // ????????????key
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(htv.getProcInstId()).singleResult();
            htv.setBusinessKey(hpi.getBusinessKey());
            ActBusiness actBusiness = actBusinessService.getById(hpi.getBusinessKey());
            if(actBusiness!=null){
                htv.setTableId(actBusiness.getTableId());
                htv.setTableName(actBusiness.getTableName());
            }

            list.add(htv);
        });
        return Result.ok(list);
    }
    /*??????????????????*/
    @RequestMapping(value = "/deleteHistoric/{ids}", method = RequestMethod.POST)
    public Result<Object> deleteHistoric( @PathVariable String ids){

        for(String id : ids.split(",")){
            historyService.deleteHistoricTaskInstance(id);
        }
        return Result.ok("????????????");
    }
}
