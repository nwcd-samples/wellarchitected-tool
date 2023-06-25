package com.aws.welltool.utils;


import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

public class TranslateUtils {
    // snippet-start:[translate.java2._text.main]
    public static String textTranslate(TranslateClient translateClient,String  text) {
        String translatedText = "";
            TranslateTextRequest textRequest = TranslateTextRequest.builder()
                    .sourceLanguageCode("en")
                    .targetLanguageCode("zh")
                    .text(text)
                    .build();
        long a = System.currentTimeMillis();
        TranslateTextResponse textResponse = translateClient.translateText(textRequest);
            System.out.println(text);
            long b = System.currentTimeMillis();
        System.out.println("..["+(b-a)+"]......"+textResponse.translatedText());


        translatedText =  textResponse.translatedText();
        return translatedText;
    }


    public TranslateClient  gettranslateClient() {
        Region region = Region.AP_NORTHEAST_2;
        TranslateClient translateClient = TranslateClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
      return translateClient;
    }

}

