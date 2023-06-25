package com.aws.welltool.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.aws.welltool.utils.Config;
import com.aws.welltool.utils.ExcelModels;
import com.aws.welltool.utils.ExcelReadLinstener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;

@RestController
public class WelltoolImportController {

    /**
     * 导出支柱问题excel
     * @return
     */
    @GetMapping("/welltoolImport")
    public String WelltoolImport(){
        //CreateWorkloadResponse(WorkloadId=a9a9ae9a902e4dedb3fb0ad8a2dc847e, WorkloadArn=arn:aws:wellarchitected:ap-northeast-2:564535962140:workload/a9a9ae9a902e4dedb3fb0ad8a2dc847e)
        String filename = "/Users/thangzhongyan/Documents/wa/dailaoshi/welltool-20230621.xlsx";
        ExcelReader excelReader = EasyExcel.read(filename, ExcelModels.class,new ExcelReadLinstener(Config.workLoadId)).headRowNumber(1).build();
        List<ReadSheet> sheetList = excelReader.excelExecutor().sheetList();

        for(int i = 0;i<sheetList.size();i++){
            ReadSheet readSheet = sheetList.get(i);
            String sheetName = readSheet.getSheetName();
            System.out.println(readSheet.getSheetName());
              excelReader.read(readSheet);

        }

        //String workload =  (workloadid,lens);
            return "workload";
        }




        private void readExcel(String filepath){

        }

        private void  getS3File(){
            ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
            Region region = Region.AP_NORTHEAST_2;
            S3Client s3 = S3Client.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .build();

        }

    public static void
    getObjectBytes (S3Client s3, String bucketName, String keyName, String path) {

        try {
            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(keyName)
                    .bucket(bucketName)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            objectBytes.asInputStream();
           // EasyExcel.read(file.getInputStream(), UploadData.class, new UploadDataListener(uploadDAO)).sheet().doRead();

            byte[] data = objectBytes.asByteArray();


            // Write the data to a local file.
//            File myFile = new File(path );
//            OutputStream os = new FileOutputStream(myFile);
//            os.write(data);
//            System.out.println("Successfully obtained bytes from an S3 object");
//            os.close();


        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }



    //=========================================


}


