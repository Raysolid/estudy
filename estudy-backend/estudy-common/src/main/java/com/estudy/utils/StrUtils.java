package com.estudy.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StrUtils {
    /**
     * 首字母大写
     */
    public static String upperCaseFirstLetter(String field) {
        if (isEmpty(field)) {
            return field;
        }
        // 如果第二个字母是大写，第一个字母不大写
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty()
                || "null".equals(str)
                || "\u0000".equals(str)
                || str.trim().isEmpty();
    }

    /**
     * 生成随机字符串
     */
    public static String randomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }
    public static String randomString(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    /**
     * md5加密
     */
    public static String md5(String originStr) {
        return isEmpty(originStr) ? null : DigestUtils.md5Hex(originStr);
    }

    /**
     * 获取文件后缀
     */
    public static String getFileSuffix(String fileName) {
        if (isEmpty(fileName)) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 是否为数字
     */
    public static boolean isNumber(String str) {
        String checkNumber = "^[0-9]+$";
        return str != null && str.matches(checkNumber);
    }
}
