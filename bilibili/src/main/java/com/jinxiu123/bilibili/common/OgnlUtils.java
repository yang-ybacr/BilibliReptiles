package com.jinxiu123.bilibili.common;

import ognl.Ognl;
import ognl.OgnlException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OgnlUtils {
    /**
     * 获取字符串
     * @param ognl
     * @param root
     * @return
     */
    public static String getstring(String ognl, Map root){

        try {
           Object object=Ognl.getValue(ognl,root);
            if(object!=null){
                return object.toString();
            }else{
                return "";
            }

        } catch (OgnlException e) {
            throw new RuntimeException();
        }
    }

    /**
     * 返回数字类型
     * @param ognl
     * @param root
     * @return
     */
    public static Number getNumber(String ognl,Map root){
        Number number=null;
        try {
            Object val=Ognl.getValue(ognl,root);
            if(val!=null){
                if(val instanceof Number){
                    number= (Number) val;
                }else if(val instanceof String){
                    number=new BigDecimal((String) val);
                }
            }
        } catch (OgnlException e) {
            throw new RuntimeException();
        }
        return  number;
    }

    /**
     * 获取boolean类型
     * @param ognl
     * @param root
     * @return
     */
    public static boolean getboolean(String ognl,Map root){
        Boolean result=null;
        try {
            Object val=Ognl.getValue(ognl,root);
            if(val instanceof Boolean){
                result=(Boolean) val;
            }else if (val instanceof String){
                result=((String) val).equalsIgnoreCase("true")?true:false;
            }else if(val instanceof  Number){
                if(((Number)val).intValue()==1){
                    result =true;
                }else {
                    result =false;
                }
            }
        } catch (OgnlException e) {
            throw new RuntimeException();
        }
        return  result;
    }

    /**
     * 获取list(里面每个元素都是map)
     * @param ognl
     * @param root
     * @return
     */
    public static List<Map<String,Object>> getlistmap(String ognl,Map root){
        List<Map<String,Object>> list=null;
        try {
            list= (List) Ognl.getValue(ognl,root);
            if(list==null){
                list=new ArrayList<>();
            }
        } catch (OgnlException e) {
            throw new RuntimeException();
        }
        return list;
    }

    /**
     * 获取list（里面每个元素都是string）
     * @param ognl
     * @param root
     * @return
     */
    public static List<String> getlistString(String ognl,Map root){
        List<String> list=null;
        try {
            list= (List) Ognl.getValue(ognl,root);
            if(list==null){
                list=new ArrayList<>();
            }
        } catch (OgnlException e) {
            throw new RuntimeException();
        }
        return list;
    }


}
