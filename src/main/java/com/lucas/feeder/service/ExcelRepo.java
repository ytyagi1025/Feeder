package com.lucas.feeder.service;

import com.lucas.feeder.dao.PointsRepository;
import com.lucas.feeder.model.JodidaarStatus;
import com.lucas.feeder.model.PointsData;
import com.lucas.feeder.model.RetailerStatus;
import com.lucas.feeder.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
public class ExcelRepo {

    @Autowired
    private PointsRepository pointsRepository;

    public boolean loadData() {
        try {
            ClassPathResource resource = new ClassPathResource("points.xlsx");
            InputStream inputStream = resource.getInputStream();

            try (Workbook workbook = WorkbookFactory.create(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0); // Read the first sheet
                for (Row row : sheet) {
                    Cell cell = row.getCell(0);
                    String value = cell.getStringCellValue();
                    if (value.contains("Barecode")) {
                        continue;
                    }
                    PointsData pointsData = new PointsData();
                    pointsData.setGenuineCode(value);
                    pointsData.setStatus(Status.FAILED);
                    pointsData.setJodidaarStatus(JodidaarStatus.NOT_ATTEMPTED);
                    pointsData.setRetailerStatus(RetailerStatus.NOT_ATTEMPTED);
                    pointsRepository.save(pointsData);
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to load data. Exception:-{}", e.getMessage());
            return false;
        }
    }
}
