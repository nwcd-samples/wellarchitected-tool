package com.aws.welltool.utils;

import software.amazon.awssdk.regions.Region;

public class Config {

    public  static String workLoadId ="";
    //文件存储基础地址
    public static String basefilepath="/Users/thangzhongyan/Documents/wa/";

    //问卷模版地址，注意名字目前固定
    public static String importfilePath= basefilepath+"welltool.xlsx";
    //lens默认值
    public static String lens = "wellarchitected";
    //environment默认值
    public static String  environment = "PRODUCTION";
    public static String  reviewOwner = "yingyuliu@nwcdcloud.cn";
    //默认lens支柱默认值
   public static  String[] pillarArray = {"security","performance","reliability","costOptimization","operationalExcellence","sustainability"};
    //risk问题high列表基名称
    public static String exportResultfilepath = basefilepath+"welltoolresult-";

    public static String fileNameEnd = ".xlsx";
    public static final Region region= Region.AP_NORTHEAST_2;
    //create workload
    public static String workLoadName = "welltool-yrk-q2";
    public static String workloaddescription = "yrkq2";

}
