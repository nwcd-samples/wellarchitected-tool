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
@ContentFontStyle(fontHeightInPoints = 10)
public class ExcelModels {

// 将第6-7行的2-3列合并成一个单元格
// @OnceAbsoluteMerge(firstRowIndex = 5, lastRowIndex = 6, firstColumnIndex = 1, lastColumnIndex = 2)

        // 这一列 每隔2行 合并单元格
        @ColumnWidth(8)
        @ExcelProperty("序号")
        private int number;
        @ColumnWidth(15)
        @ExcelProperty("问题")
        private String question;
        @ColumnWidth(15)
        @ExcelProperty("此问题不适用")
        private String notapplicable;
        @ColumnWidth(10)
        @ExcelProperty("选项")
        private String choice;
        @ColumnWidth(10)
        @ExcelProperty("答案")
        private String select;
        @ColumnWidth(80)
        @ExcelProperty("备注")
        private String desc;
        @ColumnWidth(30)
        @ExcelProperty("问题id")
        private String questionid;
        @ColumnWidth(30)
        @ExcelProperty("选项id")
        private String choiceid;

    }

