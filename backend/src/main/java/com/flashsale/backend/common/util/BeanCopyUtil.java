package com.flashsale.backend.common.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 * @description BeanCopyUtil
 * @author Yang-Hsu
 * @date 2026/2/9 上午12:18
 */
public class BeanCopyUtil {

    /**
     * @description getNullPropertyNames
     * @author Yang-Hsu
     * @date 2026/2/9 上午12:18
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }
}