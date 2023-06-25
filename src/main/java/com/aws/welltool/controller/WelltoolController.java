package com.aws.welltool.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.aws.welltool.utils.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.wellarchitected.WellArchitectedClient;
import software.amazon.awssdk.services.wellarchitected.model.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class WelltoolController {

    private WellArchitectedClient wc;
    private CreateWorkloadRequest createWorkloadReques;
    private ListAnswersRequest listAnswersRequest;
    //String workloadid="8389f11a0f3863859e668e07741adef1";
    @GetMapping("/welltoolallweb")//@RequestParam("name") String name
    public String wellroolallweb(@RequestParam("workloadname") String workloadname,@RequestParam("workloaddescription") String workloaddescription,@RequestParam("reviewowner") String reviewowner,@RequestParam("region") String region) {
        Region  regionName = Region.of(region);
        String workloadid = CreateWorkload(workloadname,workloaddescription,Config.lens,Config.environment,reviewowner, regionName);
        String importString = welltoolImportprivate(workloadid);
        String reportpdffilename =  getLensReviewReport(workloadid,workloadname,Config.lens);
        String result = exportwellresultprivate(workloadid,Config.lens);
        return "workloadid:{"+workloadid+"}"+"import:{"+importString+"}result:{"+result+"}reportpdffilename:{"+reportpdffilename+"}";
    }
    public String wellroolall() {
        String workloadid = CreateWorkload(Config.workLoadName,Config.workloaddescription,Config.lens,Config.environment,Config.reviewOwner, Region.AP_NORTHEAST_2);
        String importString = welltoolImportprivate(workloadid);
        String reportpdffilename =  getLensReviewReport(workloadid,Config.workLoadName,Config.lens);
        String result = exportwellresultprivate(workloadid,Config.lens);
        return "workloadid:{"+workloadid+"}"+"import:{"+importString+"}result:{"+result+"}reportpdffilename:{"+reportpdffilename+"}";
    }


    /**
     * 导出支柱问题excel
     * @return
     */
    @GetMapping("/exportAnswers")
    public String getAnswer() {
        String workloadId =  exportAnswersToExcel(Config.workLoadId,Config.lens);
        return workloadId;
    }


    /**
     * 创建workload
     * @return
     */
    @GetMapping("/createworkloadid")
    public String createworkloadid() {
        String workload = CreateWorkload(Config.workLoadName,Config.workloaddescription,Config.lens,Config.environment,Config.reviewOwner, Region.AP_NORTHEAST_2);
        return workload;
    }
//    private String CreateWorkload(String workloadName,String description,String lenses,String environment, String reviewOwner ,Region region){
    @GetMapping("/updateanswer-test")
    public String updateanswer(){
       String questionid = "securely-operate";
       String values[] = {"sec_securely_operate_control_objectives", "sec_securely_operate_updated_threats", "sec_securely_operate_updated_recommendations"};
       String answer = updateAnswer(Config.workLoadId,questionid,Config.lens,values);
       return answer;
    }


    @GetMapping("/getlensreviewreport")
    public String getlensreviewreport(){
       String rr =  getLensReviewReport(Config.workLoadId,Config.workLoadName,Config.lens);
        return rr;
    }


    @GetMapping("/getlensreview")
    public String  getlensreview(){
        String lensview = getLensReview(Config.workLoadId,Config.lens);
        return lensview;
    }

    @GetMapping("/getListLensReviewImprovements")
    public String  getListLensReviewImprovements(){
        String pillarid = "security";
        List<ImprovementSummary>  islist = getListLensReviewImprovements(Config.workLoadId,Config.lens,pillarid);

        return islist.toString();
    }

    @GetMapping("/exportwellresult")
    public String  exportwellresult(){
       String filenamesize =  exportwellresultprivate(Config.workLoadId,Config.lens);
       return filenamesize;
    }

     // 创建workload
    private String CreateWorkload(String workloadName,String description,String lenses,String environment, String reviewOwner ,Region region){
       // region = Region.AP_NORTHEAST_2;
        createWorkloadReques = CreateWorkloadRequest.builder().
                workloadName(workloadName)
                .awsRegions(region.toString())
                .description(description)
                .lenses(lenses)
                .environment(environment)//possible values: ["PRODUCTION","PREPRODUCTION"])]
                .reviewOwner(reviewOwner)
                .build();
        wc = WellArchitectedClient.builder().region(region).build();

        CreateWorkloadResponse cwl =   wc.createWorkload(createWorkloadReques);
        String resop =  cwl.workloadId();
        //getWorkloadRequest = GetWorkloadRequest.builder().workloadId("yrk-wa").build();
        // GetWorkloadResponse gkr = wc.getWorkload(getWorkloadRequest);
        //String  workloadid = gkr.toString();
        // gr = GetAnswerRequest.builder().workloadId("yrk-wa").lensAlias().questionId().milestoneNumber().build();
        System.out.println("workloadid----------"+resop);
        return resop;
    }


    private String listanswers(){
        listAnswersRequest = ListAnswersRequest.builder()
                .workloadId(Config.workLoadId)
                .lensAlias(Config.lens)
                //.pillarId("costOptimization")
                .maxResults(50)
                .build();
        wc = WellArchitectedClient.builder().region(Region.AP_NORTHEAST_2).build();

        ListAnswersResponse listAnswersResponse = wc.listAnswers(listAnswersRequest);
        List<AnswerSummary> answerSummaries =  listAnswersResponse.answerSummaries();

        Map<String,List<AnswerSummary>> answerSummariesMap =  answerSummaries.stream().collect(Collectors.groupingBy(AnswerSummary::pillarId));
        Map<String,List<ExcelModels>> excelMap = new HashMap<>();
        for (Map.Entry<String,List<AnswerSummary>> vo: answerSummariesMap.entrySet()){

            System.out.println(vo.getKey()+"------"+vo.getValue().size());
            List<ExcelModels> excelModelsList = new ArrayList<>();
            int num=1;
            for (AnswerSummary answerSummary:vo.getValue()) {

               // String pillarId =  answerSummary.pillarId();
                // System.out.println("answerSummary---------"+answerSummary.pillarId()+"------"+answerSummary.questionId()+"-----"+answerSummary.questionTitle());
                List<Choice> choices = answerSummary.choices();
                for (Choice choice:choices) {
                    ExcelModels excelModels = new ExcelModels();
                    excelModels.setNumber(num);
                    excelModels.setQuestion(answerSummary.questionTitle());
                    excelModels.setChoice(choice.title());
                    excelModelsList.add(excelModels);

                    System.out.println(choice.choiceId()+"-------"+ choice.title()+"--------"+choice.description()+"-------");
                }
                num++;
                excelMap.put(vo.getKey(),excelModelsList);
                // List<ChoiceAnswerSummary> choiceAnswerSummaries = answerSummary.choiceAnswerSummaries();
//            for(ChoiceAnswerSummary choiceAnswerSummary:choiceAnswerSummaries){
//                System.out.println("choiceAnswerSummary---------"+choiceAnswerSummary.choiceId()+"....."+"--------"+choiceAnswerSummary.reason()+"-------"+choiceAnswerSummary.status()+"-------"+choiceAnswerSummary);
            }
        }


        excleSheet("welltool.xlsx",excelMap);
       String listAnswers = listAnswersResponse.toString();
       // System.out.println("listAnswers---------"+listAnswers);
        return listAnswers;
    }

    private void excel(String fileName,String sheetName,List<ExcelModels> dataList) {
        ExcelWriter excelWriter = EasyExcel.write(fileName).excelType(ExcelTypeEnum.XLSX).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).head(ExcelModels.class).registerWriteHandler(new ExcelMarge(dataList.stream().map(ExcelModels::getQuestion).collect(Collectors.toList()), 1)).build();

//                .registerWriteHandler(new CustomMergeStrategy(demoDataList.stream().map(DemoData::getString).collect(Collectors.toList()), 0))
//                .build();
        excelWriter.write(dataList, writeSheet);
        excelWriter.finish();


        }

        private void excleSheet(String fileName,Map<String,List<ExcelModels>> excelMapList){
            try (ExcelWriter excelWriter = EasyExcel.write(fileName).build()) {
                for (Map.Entry<String,List<ExcelModels>> excelMap: excelMapList.entrySet()){
                    WriteSheet writeSheet = EasyExcel.writerSheet(excelMap.getKey()).head(ExcelModels.class)
                            .registerWriteHandler(new CustomSheetWriteHandler())
                            .registerWriteHandler(new ExcelMarge(excelMap.getValue().stream().map(ExcelModels::getQuestion).collect(Collectors.toList()), 0))
                            .registerWriteHandler(new ExcelMarge(excelMap.getValue().stream().map(ExcelModels::getQuestion).collect(Collectors.toList()), 1))
                            .registerWriteHandler(new ExcelMarge(excelMap.getValue().stream().map(ExcelModels::getQuestion).collect(Collectors.toList()), 2))
                            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                            .registerWriteHandler(new CustomSheetQuestionWriteHandler())
                            .registerWriteHandler(new MyHorizontalCellStyleStrategy().setStyle())
                            .build();


                    // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                    excelWriter.write(excelMap.getValue(), writeSheet);
                }
                excelWriter.finish();

            }
        }


    /**
     * 到处默认lens的全部支柱问题
     * @param workloadid
     * @param lensAlias
     * @return
     */
    private String exportAnswersToExcel(String workloadid,String lensAlias){
        Map<String,List<ExcelModels>> excelMap = new HashMap<>();
        for(int i=0;i<Config.pillarArray.length;i++){
            List<AnswerSummary> answerSummaries = getanswerSummaries(workloadid,lensAlias,Config.pillarArray[i]);
            List<ExcelModels> excelModelsList =  excelModelsList(answerSummaries);
            excelMap.put(Config.pillarArray[i],excelModelsList);
        }
        excleSheet("welltool.xlsx",excelMap);
        return String.valueOf(excelMap.size());
    }

    /**
     * 获取特定支柱的Summaries
     * @param workloadid
     * @param lensAlias
     * @param pillarId
     * @return
     */
    private List<AnswerSummary> getanswerSummaries(String workloadid,String lensAlias,String pillarId){
        listAnswersRequest = ListAnswersRequest.builder()
                .workloadId(workloadid)
                .lensAlias(lensAlias)
                .pillarId(pillarId)
                .maxResults(50)
                .build();
        wc = WellArchitectedClient.builder().region(Config.region).build();
        ListAnswersResponse listAnswersResponse = wc.listAnswers(listAnswersRequest);
        List<AnswerSummary> answerSummaries =  listAnswersResponse.answerSummaries();
        return answerSummaries;
    }

    /**
     * 组装ExcelModels，以choices粒度拉平setQuestion和choices
     * @param answerSummaries
     * @return
     */
    private List<ExcelModels> excelModelsList (List<AnswerSummary> answerSummaries){
        String PREFIX_TURN = "\r\n";
        List<ExcelModels> excelModelsList = new ArrayList<>();
        TranslateUtils translateUtils = new TranslateUtils();
        Map<String,String> translatemap = new HashMap<>();
        for(int i=0;i<answerSummaries.size();i++){
            AnswerSummary answerSummary = answerSummaries.get(i);
            List<Choice> choices = answerSummary.choices();
            for (Choice choice:choices) {
                ExcelModels excelModels = new ExcelModels();
                excelModels.setNumber(i+1);
                String questionTitleZH = "";
               String questionTitle  =  answerSummary.questionTitle();
                if(StringUtils.isNotBlank(questionTitle)){
                  if(!translatemap.containsKey(questionTitle)){
                      questionTitleZH= translateUtils.textTranslate(translateUtils.gettranslateClient(),questionTitle);
                      translatemap.put(questionTitle,questionTitleZH);
                  }else{
                      questionTitleZH = translatemap.get(questionTitle);
                  }
                }
                excelModels.setQuestion(questionTitle+PREFIX_TURN+questionTitleZH);
                String choicetitleZH = "";
                if(StringUtils.isNotBlank(choice.title())){
                    choicetitleZH= TranslateUtils.textTranslate(translateUtils.gettranslateClient(),choice.title());
                }
                excelModels.setChoice(choice.title()+PREFIX_TURN+choicetitleZH);
                String choicedescriptionZH = "";
                if(StringUtils.isNotBlank(choice.description())){
                    choicedescriptionZH= TranslateUtils.textTranslate(translateUtils.gettranslateClient(),choice.description());
                }
                excelModels.setDesc(choice.description()+PREFIX_TURN+choicedescriptionZH);
                excelModels.setQuestionid(answerSummary.questionId());
                excelModels.setChoiceid(choice.choiceId());
                excelModels.setSelect("否");
                excelModels.setNotapplicable("");
                excelModelsList.add(excelModels);
            }
        }
        return excelModelsList;
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

      return  answer.answer().toString();

    }


    private String getLensReviewReport(String workloadid,String workloadname ,String lens){
        GetLensReviewReportRequest grrr = GetLensReviewReportRequest.builder().lensAlias(lens).workloadId(workloadid).build();
        WellArchitectedClient wa = WellArchitectedClient.builder().region(Region.AP_NORTHEAST_2).build();
        GetLensReviewReportResponse glrResponse = wa.getLensReviewReport(grrr);
        String report =  glrResponse.lensReviewReport().base64String();
        String filename = base64topdf(report,workloadname);
        return filename;

    }

    private String getLensReview(String workloadid,String lens){
        GetLensReviewRequest grrr = GetLensReviewRequest.builder().lensAlias(lens).workloadId(workloadid).build();
        WellArchitectedClient wa = WellArchitectedClient.builder().region(Region.AP_NORTHEAST_2).build();
        GetLensReviewResponse glrResponse = wa.getLensReview(grrr);
        Iterator<PillarReviewSummary> it = glrResponse.lensReview().pillarReviewSummaries().iterator();
        // 输出集合中的所有元素
        while(it.hasNext()) {
           System.out.println(it.next());
        }

        return "999999";

    }


   //根据支柱获取详细信息
    private List<ImprovementSummary> getListLensReviewImprovements(String workloadid,String lens,String pillarId){
        ListLensReviewImprovementsRequest lrir =  ListLensReviewImprovementsRequest.builder().lensAlias(lens).workloadId(workloadid).pillarId(pillarId).build();
        WellArchitectedClient wa = WellArchitectedClient.builder().region(Region.AP_NORTHEAST_2).build();
       ListLensReviewImprovementsResponse llrir =  wa.listLensReviewImprovements(lrir);
        List<ImprovementSummary>  islist= llrir.improvementSummaries();
//        for(int i = 0;i<islist.size();i++){
//            ImprovementSummary improvementSummary =  islist.get(i);
//            System.out.println("........."+improvementSummary.questionId()+"....."+improvementSummary.risk());
//         //   System.out.println("........."+improvementSummary.questionTitle());
//
//        }
        return islist;

    }

    private List<WellExcelModels> getListLensReviewImprovementsForAll(String workloadid,String lens){
        List<WellExcelModels> wellExcelModelsListForAll = new ArrayList<>();
        for (int i=0;i<Config.pillarArray.length;i++){
            String  pillarId = Config.pillarArray[i];
            List<ImprovementSummary>  improvementSummarylist = getListLensReviewImprovements(workloadid,lens,pillarId);
            List<WellExcelModels> wellExcelModelsList = makeImprovementSummaryExcelDate(pillarId,improvementSummarylist);
            wellExcelModelsListForAll.addAll(wellExcelModelsList);
        }
        for (int j = 0;j<wellExcelModelsListForAll.size();j++)  {
            wellExcelModelsListForAll.get(j).setNumber(j+1);
        }
    return wellExcelModelsListForAll;
    }

        public  String base64topdf(String base64String,String workloadname) {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            String filename = Config.basefilepath+workloadname+Config.lens+".pdf";

            try {
                OutputStream outputStream = new FileOutputStream(filename);
                outputStream.write(decodedBytes);
                outputStream.close();
                System.out.println("PDF file created successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return filename;
        }

        private List<WellExcelModels> makeImprovementSummaryExcelDate(String pillarId,List<ImprovementSummary> improvementSummaryList){
            List<WellExcelModels> wellExcelModelsList = new ArrayList<>();
         for(int i=0;i<improvementSummaryList.size();i++){
             ImprovementSummary improvementSummary =   improvementSummaryList.get(i);
             WellExcelModels wem = new WellExcelModels();
             wem.setQuestionid(improvementSummary.questionId());
             wem.setQuestion(improvementSummary.questionTitle());
             wem.setLevel(improvementSummary.risk().toString());
             wem.setPillarId(pillarId);


             wellExcelModelsList.add(wem);
         }

         return wellExcelModelsList;
        }

    private void excleWellSheet(String fileName,List<WellExcelModels> wellExcelModelsList) {
        ExcelWriter excelWriter = EasyExcel.write(fileName, WellExcelModels.class).build();
        // 这里注意 如果同一个sheet只要创建一次
        WriteSheet writeSheet = EasyExcel.writerSheet("risk").registerWriteHandler(new WellResultWriteHandler()).build();
        excelWriter.write(wellExcelModelsList, writeSheet);
        //.registerWriteHandler(new CustomSheetWriteHandler())
        excelWriter.finish();
    }


    private String welltoolImportprivate(String workloadid){
        //CreateWorkloadResponse(WorkloadId=a9a9ae9a902e4dedb3fb0ad8a2dc847e, WorkloadArn=arn:aws:wellarchitected:ap-northeast-2:564535962140:workload/a9a9ae9a902e4dedb3fb0ad8a2dc847e)
        ExcelReader excelReader = EasyExcel.read(Config.importfilePath, ExcelModels.class,new ExcelReadLinstener(workloadid)).headRowNumber(1).build();
        List<ReadSheet> sheetList = excelReader.excelExecutor().sheetList();

        for(int i = 0;i<sheetList.size();i++){
            ReadSheet readSheet = sheetList.get(i);
            String sheetName = readSheet.getSheetName();
            System.out.println(readSheet.getSheetName());
            excelReader.read(readSheet);

        }

        //String workload =  (workloadid,lens);
        System.out.println("welltoolImportprivate success!");
        return "workload";
    }


    private String exportwellresultprivate(String workloadid,String lens){
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        String date = df.format(new Date());// new Date()为获取当前系统时间
        String filename  = Config.exportResultfilepath+"-"+date+"-"+Config.fileNameEnd;
        List<WellExcelModels> wellExcelModelsForAll =  getListLensReviewImprovementsForAll(workloadid, lens);
        excleWellSheet(filename,wellExcelModelsForAll);
        System.out.println("exportwellresultprivate......"+filename+"_"+wellExcelModelsForAll.size());
        return filename+"_"+wellExcelModelsForAll.size();
    }





}