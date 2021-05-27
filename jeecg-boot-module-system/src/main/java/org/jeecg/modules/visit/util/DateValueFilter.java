package org.jeecg.modules.visit.util;

import com.alibaba.fastjson.serializer.ValueFilter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class DateValueFilter implements ValueFilter {


    @Override
    public Object process(Object o, String s, Object o1) {
        if(StringUtils.isNotBlank(s)
                && StringUtils.endsWithIgnoreCase(s,"time")
                && o1 instanceof Long){
            return new Date((Long)o1);
        }
        return o1;
    }
}
