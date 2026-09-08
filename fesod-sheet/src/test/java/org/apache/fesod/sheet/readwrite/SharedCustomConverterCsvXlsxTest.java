/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fesod.sheet.readwrite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.converters.WriteConverterContext;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.event.AnalysisEventListener;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.support.ExcelTypeEnum;
import org.apache.fesod.sheet.testkit.Tags;
import org.apache.fesod.sheet.testkit.base.AbstractExcelTest;
import org.apache.fesod.sheet.testkit.enums.ExcelFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(Tags.ROUND_TRIP)
@Slf4j
public class SharedCustomConverterCsvXlsxTest extends AbstractExcelTest {

    private List<BooleanWriteData> dataList() {
        List<BooleanWriteData> dataList = new ArrayList<>();
        BooleanWriteData trueRow = new BooleanWriteData();
        trueRow.setFlag(Boolean.TRUE);
        BooleanWriteData falseRow = new BooleanWriteData();
        falseRow.setFlag(Boolean.FALSE);
        dataList.add(trueRow);
        dataList.add(falseRow);
        return dataList;
    }

    @Test
    public void testSharedCustomConverterInXlsx() throws Exception {
        File xlsxFile = createTempFile(ExcelFormat.XLSX);
        FesodSheet.write(xlsxFile, BooleanWriteData.class)
                .registerConverter(new BooleanYesNoConverter())
                .sheet()
                .doWrite(dataList());
        List<StringReadData> rows = FesodSheet.read(xlsxFile, StringReadData.class, new StringReadListener())
                .sheet()
                .doReadSync();
        Assertions.assertEquals(2, rows.size());
        Assertions.assertEquals("yes", rows.get(0).getFlag());
        Assertions.assertEquals("no", rows.get(1).getFlag());
    }

    @Test
    public void testSharedCustomConverterInCsv() throws Exception {
        File csvFile = createTempFile(ExcelFormat.CSV);
        FesodSheet.write(csvFile, BooleanWriteData.class)
                .excelType(ExcelTypeEnum.CSV)
                .registerConverter(new BooleanYesNoConverter())
                .sheet()
                .doWrite(dataList());
        List<StringReadData> rows = FesodSheet.read(csvFile, StringReadData.class, new StringReadListener())
                .sheet()
                .doReadSync();
        Assertions.assertEquals(2, rows.size());
        Assertions.assertEquals("yes", rows.get(0).getFlag());
        Assertions.assertEquals("no", rows.get(1).getFlag());
    }

    public static class BooleanYesNoConverter implements Converter<Boolean> {

        @Override
        public Class<?> supportJavaTypeKey() {
            return Boolean.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            // null means the converter matches every cell type
            return null;
        }

        @Override
        public WriteCellData<?> convertToExcelData(WriteConverterContext<Boolean> context) throws Exception {
            Boolean value = context.getValue();
            return new WriteCellData<>(Boolean.TRUE.equals(value) ? "yes" : "no");
        }
    }

    @Getter
    @Setter
    public static class BooleanWriteData {

        @ExcelProperty("flag")
        private Boolean flag;
    }

    @Getter
    @Setter
    public static class StringReadData {

        @ExcelProperty("flag")
        private String flag;
    }

    public static class StringReadListener extends AnalysisEventListener<StringReadData> {

        @Override
        public void invoke(StringReadData data, AnalysisContext context) {}

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }
}
