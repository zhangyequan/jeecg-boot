package org.jeecg.modules.visit.service.impl;
import org.jeecg.modules.visit.mapper.VisitinfoMapper;
import org.jeecg.modules.visit.service.IVisitinfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.jeecg.modules.visit.entity.Visitinfo;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: visitinfo
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
@Service
public class VisitinfoServiceImpl extends ServiceImpl<VisitinfoMapper, Visitinfo> implements IVisitinfoService {

    @Autowired
    private VisitinfoMapper visitinfoMapper;
    @Override
    public void createVisit(Visitinfo visitinfo) {
        visitinfoMapper.createvisit(visitinfo);
    }
}
