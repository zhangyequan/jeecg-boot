package org.jeecg.modules.visit.service;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.visit.entity.Visitinfo;

/**
 * @Description: visitinfo
 * @Author: jeecg-boot
 * @Date:   2021-05-27
 * @Version: V1.0
 */
public interface IVisitinfoService extends IService<Visitinfo> {

    public void createVisit(Visitinfo visitinfo);
}
