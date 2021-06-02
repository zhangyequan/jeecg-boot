package org.jeecg.modules.visit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.visit.entity.Visitinfo;

/**
 * @Description: visitinfo
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
public interface VisitinfoMapper extends BaseMapper<Visitinfo> {

    IPage<Visitinfo> getByStatus(Page<Visitinfo> page, String status);
    int approve(String id);

}
