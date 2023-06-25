package com.aws.welltool.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.wellarchitected.WellArchitectedClient;
import software.amazon.awssdk.services.wellarchitected.model.UpdateAnswerRequest;
import software.amazon.awssdk.services.wellarchitected.model.UpdateAnswerResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
     * 模板的读取类
     *
     * @author Jiaju Zhuang
     */
// 有个很重要的点 ExcelReadLinstener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去AnalysisEventListener
@Slf4j
public class ExcelReadLinstener implements ReadListener<ExcelModels> {

    private String workloadid;

   public  ExcelReadLinstener(String workloadid){
       this.workloadid = workloadid;

    }

    Map<String,String> isApplicableMap = new HashMap<>();
    Map<String, String[]> answerMap = new HashMap<String, String[]>();
    List<String> answerList= new ArrayList<>();

        @Override
        public void invoke(ExcelModels excelModels, AnalysisContext context) {
          //  for (Map.Entry<Integer, ReadCellData<String>> map: rowMap.entrySet()){
           // System.out.println("解析到一条数据:{}"+context.readSheetHolder().getSheetName()+"....."+excelModels.getQuestionid()+"...."+excelModels.getChoiceid()+"....."+   excelModels.getNotapplicable()+"......"+   excelModels.getSelect());
            makeAnswer(excelModels);
           //如果getQuestionid不在已知不作答的列表里面

        }
                    //JSON.toJSONString(data));

        /**
         * 所有数据解析完成了 都会来调用
         *
         * @param context
         */
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            System.out.println("读完了.........."+context.readSheetHolder().getSheetName());
            if(isApplicableMap!=null&&!isApplicableMap.isEmpty()){
                batchUpdateAnswerForIsApplicable(isApplicableMap,workloadid,Config.lens);
            }
            if(answerMap!=null&&!answerMap.isEmpty()){
                batchupdateAnswer(answerMap,workloadid,Config.lens);
            }
            isApplicableMap = new HashMap<>();
            answerMap = new HashMap<>();
            answerList= new ArrayList<>();
            System.out.println("更新完了.........."+context.readSheetHolder().getSheetName());

        }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        System.out.println("读取到了一条额外信息:{}"+JSON.toJSONString(extra));

    }

    public String updateanswer(String workloadid,String lens,String questionid,String[] choiceidArr,String notapplicable){
        String answerResult="";
        if(StringUtils.isNotBlank(notapplicable)){
            answerResult = updateAnswer(workloadid,questionid,lens,choiceidArr);

        }else{
            answerResult = updateAnswerForIsApplicable(workloadid,questionid,lens,notapplicable);

        }
        return answerResult;
    }
    private String updateAnswer(String workloadid,String questionid,String lens ,String[] selsectValue){
        UpdateAnswerRequest updateAnswerRequest = UpdateAnswerRequest.builder()
                .workloadId(workloadid)
                .questionId(questionid)
                .lensAlias(lens)
                .selectedChoices(selsectValue)
                //.choiceUpdates()
                .build();
        WellArchitectedClient wc = WellArchitectedClient.builder().region(Region.AP_NORTHEAST_2).build();
        UpdateAnswerResponse answer = wc.updateAnswer(updateAnswerRequest);
         System.out.println("updateAnswer更新结果："+answer.answer().toString());
        return  answer.answer().toString();

    }
    private String updateAnswerForIsApplicable(String workloadid,String questionid,String lens ,String notapplicable){
        UpdateAnswerRequest updateAnswerRequest = UpdateAnswerRequest.builder()
                .workloadId(workloadid)
                .questionId(questionid)
                .lensAlias(lens)
                .isApplicable(Boolean.FALSE)
                .reason(notapplicable)
                .build();
        WellArchitectedClient wc = WellArchitectedClient.builder().region(Region.AP_NORTHEAST_2).build();
        UpdateAnswerResponse answer = wc.updateAnswer(updateAnswerRequest);
        //System.out.println("updateAnswerForIsApplicable更新结果："+answer.answer().toString());
        return  answer.answer().toString();

    }

    private void batchUpdateAnswerForIsApplicable(Map<String, String> answerMap,String workloadid,String lens ) {
        for (Map.Entry<String, String> answer : answerMap.entrySet()) {
            updateAnswerForIsApplicable(workloadid,answer.getKey(),lens,answer.getValue());
            System.out.println("更新答案IsApplicable......「+"+workloadid+"+」"+"「"+lens+"」"+"「"+answer.getKey()+"」"+"「"+answer.getValue()+"」");
        }
    }

    private void batchupdateAnswer(Map<String, String[]> answerMap,String workloadid,String lens ) {
        for (Map.Entry<String, String[]> answer : answerMap.entrySet()) {
            updateAnswer(workloadid,answer.getKey(),lens,answer.getValue());
            System.out.println("更新答案「+"+workloadid+"+」"+"「"+lens+"」"+"「"+answer.getKey()+"」"+"「"+answer.getValue().toString()+"」");
        }
    }

    private void makeAnswer(ExcelModels excelModels){
        if(!isApplicableMap.containsKey(excelModels.getQuestionid())){
            //如果此题为不作答题则加入map
            if(StringUtils.isNotBlank(excelModels.getNotapplicable())){
                isApplicableMap.put(excelModels.getQuestionid(),excelModels.getNotapplicable());
            }else{
                if(StringUtils.isNotBlank(excelModels.getSelect())){
                    if("是".equals(excelModels.getSelect())){
                        if(!answerMap.containsKey(excelModels.getQuestionid())){
                            answerList = ListUtils.newArrayList();
                        }
                        answerList.add(excelModels.getChoiceid());
                        String[] strArray = answerList.toArray(new String[answerList.size()]);
                        answerMap.put(excelModels.getQuestionid(),strArray);
                    }

                }

            }
        }


    }

    }

