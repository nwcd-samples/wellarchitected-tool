package com.aws.welltool.utils;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.handler.context.SheetWriteHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.util.CellRangeAddressList;

@Slf4j
public class CustomSheetQuestionWriteHandler implements SheetWriteHandler {
    @Override
    public void afterSheetCreate(SheetWriteHandlerContext context) {
        // 区间设置 第一列第一行和第二行的数据。由于第一行是头，所以第一、二行的数据实际上是第二三行
        CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(1, 100, 2, 2);
        DataValidationHelper helper = context.getWriteSheetHolder().getSheet().getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(new String[] {"OUT_OF_SCOPE", "BUSINESS_PRIORITIES","ARCHITECTURE_CONSTRAINTS","OTHER"});
        DataValidation dataValidation = helper.createValidation(constraint, cellRangeAddressList);
        context.getWriteSheetHolder().getSheet().addValidationData(dataValidation);
    }
    ///OUT_OF_SCOPE | BUSINESS_PRIORITIES | ARCHITECTURE_CONSTRAINTS | OTHER | NONE

}
