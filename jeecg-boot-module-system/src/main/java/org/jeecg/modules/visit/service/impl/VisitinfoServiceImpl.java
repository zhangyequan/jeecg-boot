package org.jeecg.modules.visit.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public IPage<Visitinfo> getByStatus(Page<Visitinfo> page, String status) {
        return visitinfoMapper.getByStatus(page, status);
    }
}
