package com.fct.web.pay.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jon on 2017/6/4.
 */
public class Constants {

    public Constants()
    {

    }

    public static final Logger logger = LoggerFactory.getLogger("EX");

    public static final String domain = "http://www.fangcuntang.com";

    public static final String payDomain = "http://pay.fangcuntang.com";

    /**
     * 获取请求参数中所有的信息
     *
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParam(final HttpServletRequest request) {
        Map<String, String> res = new HashMap<String, String>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);

                //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
                //value = new String(value.getBytes("ISO-8859-1"), "utf-8");

                res.put(en, value);
                //在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
                //System.out.println("ServletUtil类247行  temp数据的键=="+en+"     值==="+value);
                if (null == res.get(en) || "".equals(res.get(en))) {
                    res.remove(en);
                }
            }
        }
        return res;
    }
}