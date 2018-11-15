package fr.layer4.hhsl;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

public class SpringUtils {

    public static <T> T getTargetObject(Object proxy) throws Exception {
        while (AopUtils.isJdkDynamicProxy(proxy)) {
            return getTargetObject(((Advised) proxy).getTargetSource().getTarget());
        }
        return (T) proxy;
    }
}
