package com.zs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public final static String cNumber1 = "<N1>";
    public final static String cNumber2 = "<N2>";
    public final static String cNumber3 = "<N3>";
    public final static String cNumber4 = "<N4>";
    public final static String cNumber5 = "<N5>";
    public final static String cNumber6 = "<N6>";
    public final static String cNumber7 = "<N7>";
    public final static String cNumber8 = "<N8>";
    public final static String cNumber9 = "<N9>";
    public final static String cNumber10 = "<N10>";
    public final static String cNumber11 = "<N11>";
    public final static String cNumber12 = "<N12>";
    public final static String cNumber13 = "<N13>";
    public final static String cNumber14 = "<N14>";
    public final static String cNumber15 = "<N15>";

    public final static String cWORD = "<WORD>";
    public final static String patternWord = "[A-Za-z0-9-]+";

    public static String patternNumber = "\\d{NUMBER}";

    public String find (String input, String regex) {
        if (regex.indexOf(cNumber1) != -1 
        || regex.indexOf(cNumber2) != -1 
        || regex.indexOf(cNumber3) != -1
        || regex.indexOf(cNumber4) != -1
        || regex.indexOf(cNumber5) != -1
        || regex.indexOf(cNumber6) != -1
        || regex.indexOf(cNumber7) != -1
        || regex.indexOf(cNumber8) != -1
        || regex.indexOf(cNumber9) != -1
        || regex.indexOf(cNumber10) != -1
        || regex.indexOf(cNumber11) != -1
        || regex.indexOf(cNumber12) != -1
        || regex.indexOf(cNumber13) != -1
        || regex.indexOf(cNumber14) != -1
        || regex.indexOf(cNumber15) != -1) {
            return findWordWithNumber(input, regex);    
        } 
        if (regex.indexOf(cWORD) != -1) {
            return findAfter(input, regex);
        }
        return null;
    }
    public String findAfter (String input, String regex) {
        String praefix = regex.substring(0, regex.indexOf(cWORD));
        input = input.replaceAll("/", "-");
        regex = regex.replace(cWORD, patternWord);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String result = input.substring(matcher.start(), matcher.end());
            return result.substring(praefix.length());
        }

        return null;
    }
    public String findWordWithNumber (String input, String regex) {
        String praefix = regex.substring(0, regex.indexOf("<"));
        String suffix = regex.substring(regex.lastIndexOf(">") + 1);

        regex = maskRegex(regex);
        
        // -- start with präfix 
        int i = input.indexOf(praefix);
        if (i > 0) {
            input = input.substring(i);
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String result = input.substring(matcher.start(), matcher.end());
            result = result.replaceAll("/", "-");
            
            //-- delete präfix
            result = result.substring(praefix.length());
            
            //-- delete suffix
            if (suffix != null && suffix.trim().length() > 0)
                result = result.substring(0, result.indexOf(suffix));

            return result;
        }
        return null;
    }

    private String maskRegex (String regex) {
        regex = "^" + regex;
        regex = regex.replace(cNumber1, patternNumber.replace("NUMBER", "1"));
        regex = regex.replace(cNumber2, patternNumber.replace("NUMBER", "2"));
        regex = regex.replace(cNumber3, patternNumber.replace("NUMBER", "3"));
        regex = regex.replace(cNumber4, patternNumber.replace("NUMBER", "4"));
        regex = regex.replace(cNumber5, patternNumber.replace("NUMBER", "5"));
        regex = regex.replace(cNumber6, patternNumber.replace("NUMBER", "6"));
        regex = regex.replace(cNumber7, patternNumber.replace("NUMBER", "7"));
        regex = regex.replace(cNumber8, patternNumber.replace("NUMBER", "8"));
        regex = regex.replace(cNumber9, patternNumber.replace("NUMBER", "9"));
        regex = regex.replace(cNumber10, patternNumber.replace("NUMBER", "10"));
        regex = regex.replace(cNumber11, patternNumber.replace("NUMBER", "11"));
        regex = regex.replace(cNumber12, patternNumber.replace("NUMBER", "12"));
        regex = regex.replace(cNumber13, patternNumber.replace("NUMBER", "13"));
        regex = regex.replace(cNumber14, patternNumber.replace("NUMBER", "14"));
        regex = regex.replace(cNumber15, patternNumber.replace("NUMBER", "15"));
        regex = regex.replace("/", "\\/");
        regex = regex.replace("(", "\\(");
        regex = regex.replace(")", "\\)");
        regex = regex.replace("<", "");
        regex = regex.replace(">", "");
        return regex;
    }
}
