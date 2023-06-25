package com.aws.welltool.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.aws.welltool.utils.FileUtils.getPath;

public  class ExcelMarge  extends AbstractMergeStrategy {
    // 自定义合并策略 该类继承了AbstractMergeStrategy抽象合并策略，需要重写merge()方法
        /**
         * 分组，每几行合并一次
         */
        private List<Integer> exportFieldGroupCountList;

        /**
         * 目标合并列index
         */
        private Integer targetColumnIndex;

        // 需要开始合并单元格的首行index
        private Integer rowIndex;

        // exportDataList为待合并目标列的值
        public ExcelMarge(List<String> exportDataList, Integer targetColumnIndex) {
            this.exportFieldGroupCountList = getGroupCountList(exportDataList);
            this.targetColumnIndex = targetColumnIndex;
        }


        @Override
        protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {

            if (null == rowIndex) {
                rowIndex = cell.getRowIndex();
            }
            // 仅从首行以及目标列的单元格开始合并，忽略其他
            if (cell.getRowIndex() == rowIndex && cell.getColumnIndex() == targetColumnIndex) {
                mergeGroupColumn(sheet);
            }
        }

        private void mergeGroupColumn(Sheet sheet) {
            int rowCount = rowIndex;
            for (Integer count : exportFieldGroupCountList) {
                if(count == 1) {
                    rowCount += count;
                    continue ;
                }
                // 合并单元格
                CellRangeAddress cellRangeAddress = new CellRangeAddress(rowCount, rowCount + count - 1, targetColumnIndex, targetColumnIndex);
                sheet.addMergedRegionUnsafe(cellRangeAddress);
                rowCount += count;
            }
        }

        // 该方法将目标列根据值是否相同连续可合并，存储可合并的行数
        private List<Integer> getGroupCountList(List<String> exportDataList){
            if (CollectionUtils.isEmpty(exportDataList)) {
                return new ArrayList<>();
            }

            List<Integer> groupCountList = new ArrayList<>();
            int count = 1;

            for (int i = 1; i < exportDataList.size(); i++) {
                if (exportDataList.get(i).equals(exportDataList.get(i - 1))) {
                    count++;
                } else {
                    groupCountList.add(count);
                    count = 1;
                }
            }
            // 处理完最后一条后
            groupCountList.add(count);
            return groupCountList;
        }
    }

    // 修改WriteSheet的代码如下
//    public static void writeExcel() {
//        String fileName = getPath();
//        ExcelWriter excelWriter = EasyExcel.write(fileName).excelType(ExcelTypeEnum.XLSX).build();
//        List<ExcelModels> demoDataList = new ArrayList<>();
//        // 写sheet的时候注册相应的自定义合并单元格策略
//        WriteSheet writeSheet = EasyExcel.writerSheet("模板1").head(ExcelModels.class)
//                .registerWriteHandler(new ExcelMarge(demoDataList.stream().map(ExcelModels::getString).collect(Collectors.toList()), 0))
//               .build(); excelWriter.write(demoDataList, writeSheet);
//        excelWriter.finish();
//    }

