package ljw.comicviewer.util;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ljw on 2017-10-31 031.
 */

public class StringUtil {
    private static boolean DEBUG = false;

    /**
     * 正则表达式获得内容
     * @param reg 正则表达式
     * @param str 需要匹配的文字
     * @param i   结果下标
     * @return
     */
    public static String getPattern(String reg,String str,int i){
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()){
            return matcher.group(i);
        }
        return null;
    }

    public static boolean isExits(String reg, String str){
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        boolean res = matcher.matches();
        if(DEBUG) Log.d("StringUtil----", "isExits: {"+reg+","+str+"}"+res);
        return res;
    }
}