package org.jeecg.modules.visit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.visit.entity.Visitinfo;

/**
 * @Description: visitinfo
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
public interface VisitinfoMapper extends BaseMapper<Visitinfo> {

    void createvisit(Visitinfo visitinfo);

}
