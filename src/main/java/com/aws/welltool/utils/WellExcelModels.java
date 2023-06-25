package com.aws.welltool.utils;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
@ContentFontStyle(fontHeightInPoints = 15)
public class WellExcelModels {
    @ColumnWidth(10)
    @ExcelProperty("序号")
    private int number;
    @ColumnWidth(30)
    @ExcelProperty("支柱")
    private String pillarId;
    @ColumnWidth(30)
    @ExcelProperty("问题ID")
    private String questionid;
    @ColumnWidth(80)
    @ExcelProperty("问题")
    private String question;
    @ColumnWidth(10)
    @ExcelProperty("风险等级")
    private String level;
    @ColumnWidth(20)
    @ExcelProperty("处理办法")
    private String treatment;



}
