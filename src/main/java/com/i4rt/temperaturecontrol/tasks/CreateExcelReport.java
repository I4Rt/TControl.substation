package com.i4rt.temperaturecontrol.tasks;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.Measurement;
import com.i4rt.temperaturecontrol.model.WeatherMeasurement;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class CreateExcelReport {

    @Autowired
    private final ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;
    @Autowired
    private final ThermalImagerRepo thermalImagerRepo;
    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;


    public CreateExcelReport(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo,
                      UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo){
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
    }

    public String createMainSheet(Map<String, Object> preparedToSendData) throws IOException {

        // создание главного эксель листа
        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = book.createSheet("Области контроля");

        // задание стилей для ячеек
        DataFormat format = book.createDataFormat();
        CellStyle greenStyle = book.createCellStyle();
        greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        greenStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());

        CellStyle redStyle = book.createCellStyle();
        redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        redStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());

        CellStyle dateStyle = book.createCellStyle();
        dateStyle.setDataFormat(format.getFormat("dd.mm.yyyy hh:mm:ss.000"));

        // список всех подконтрольных объектов
        List<ControlObject> controlObjects = this.controlObjectRepo.findAll();

        // строки занимаемые выводом ячеек
        int rowSize = controlObjects.size() / 5;
        int columnSize = 5;

        ArrayList<ControlObject> overheatedAreaList = new ArrayList<>();

        // вывод всех подконтрольных областей и задание им стиля
        for (int i = 0; i < rowSize + 1; i++) {
            Row row = sheet.createRow(i);
            for (int j = i * 5; j < i * 5 + columnSize; j++) {
                if (j >= controlObjects.size()){
                    break;
                }
                Cell cell = row.createCell(j - i * 5);

                ControlObject controlObject = controlObjects.get(j);
                boolean overheating = false;

                //проверка на перегретость
//                List<Measurement> measurements = controlObject.getMeasurement();
                if (!measurementRepo.getOverheatingMeasurement(controlObject.getId(),
                        (Double) controlObject.getWarningTemp()).isEmpty()) overheating = true;

                if (!overheating) cell.setCellStyle(greenStyle);
                else {
                    cell.setCellStyle(redStyle);
                    overheatedAreaList.add(controlObject);
                }
                cell.setCellValue(controlObjects.get(j).getName());
            }
        }
        Integer index = 0;
        for (ControlObject controlObject : overheatedAreaList) {

            List<Measurement> measurements = controlObject.getMeasurement();
            index += 1;
            System.out.println("Area " + index + " of " + overheatedAreaList.size());

            // эксель лист под каждую область с температурами
            XSSFSheet cellSheet = book.createSheet("Область #" + controlObject.getId()+ "-" + controlObject.getName() );

            // название области
            Row areaRow = cellSheet.createRow(0);
            Cell areaCell = areaRow.createCell(0);
            areaCell.setCellValue("Область " + controlObject.getName());
            areaCell.setCellStyle(redStyle);

            // номера столбцов дат и значений
            int numCellDate = 0;
            int numCellValue = 1;
            int numCellWeather = 4;

            ArrayList<Date> totalDates = (ArrayList<Date>) preparedToSendData.get("time");

            // index (0) - температуры тепловизора, index (1) - температуры метеостанции
//            ArrayList<ArrayList<Double>> totalTemperatures = getTotalTemperatures(controlObject,
//                    this.weatherMeasurementRepo, this.measurementRepo, totalDates, beginningDate, endingDate);
            if(((ArrayList<Object>)preparedToSendData.get("weatherTemperature")).isEmpty()){
                continue;
            }
            // вывод измерений на отдельный лист эксель
            for (int i = 0; i < totalDates.size(); i++) {
                Row rowInf = cellSheet.createRow(i + 2);

                // столбец дат
                Cell cellDate = rowInf.createCell(numCellDate);
                cellDate.setCellStyle(dateStyle);
                cellDate.setCellValue(totalDates.get(i));
                cellSheet.autoSizeColumn(numCellDate);

                // столбец значений
                Cell cellValue = rowInf.createCell(numCellValue);
                if (((ArrayList<Object>) preparedToSendData.get("weatherTemperature")).get(i).getClass()
                        == WeatherMeasurement.class) cellValue.setBlank();
                else cellValue.setCellValue(((ArrayList<Measurement>) preparedToSendData.get("weatherTemperature"))
                        .get(i).getTemperature());

                Cell cellWeather = rowInf.createCell(numCellWeather);
                if (((ArrayList<Object>) preparedToSendData.get("weatherTemperature")).get(i).getClass()
                        == Measurement.class) cellWeather.setBlank();
                else cellWeather.setCellValue(((ArrayList<WeatherMeasurement>) preparedToSendData.get("weatherTemperature"))
                        .get(i).getTemperature());

            }

            // информация по перегретым точкам на главном эксель листе
            Row row = sheet.createRow(2 + rowSize + 26 * overheatedAreaList.indexOf(controlObject));
            Cell cell = row.createCell(0);
            cell.setCellValue("Область " + controlObject.getName());
            cell.setCellStyle(redStyle);
//            sheet.autoSizeColumn(0);

            // строка с граничными температурами
            Row tempBorders = sheet.createRow(row.getRowNum() + 1);
            tempBorders.createCell(0).setCellValue("Граничные температуры:");
            tempBorders.createCell(3).setCellValue(controlObject.getWarningTemp());
            tempBorders.createCell(4).setCellValue(controlObject.getDangerTemp());

            // график температур
            XSSFChart chart = createChart(sheet, tempBorders.getRowNum() + 2, tempBorders.getRowNum() + 21,
                    0, 13, controlObject.getName());
            XDDFLineChartData data = createData(chart);

            createDataSource(cellSheet, 2, totalDates.size()+1, numCellDate, numCellDate, 2, totalDates.size()+1,
                    numCellValue, numCellValue, "Область " + controlObject.getName(), data);

            createDataSource(cellSheet, 2, totalDates.size()+1, numCellDate, numCellDate, 2, totalDates.size()+1,
                    numCellWeather, numCellWeather, "Температура воздуха", data);

            // пропуски соединяются линией
            chart.displayBlanksAs(DisplayBlanks.SPAN);

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.TOP);

            chart.plot(data);

            CTPlotArea plotArea = chart.getCTChart().getPlotArea();
            plotArea.getValAxArray()[0].addNewMajorGridlines();

            ArrayList<Measurement> overheatingMeasurements = measurementRepo.getOverheatingMeasurement(
                    controlObject.getId(), (Double) controlObject.getWarningTemp());
            String info = "Время превышения температур: ";
            StringBuilder sb = new StringBuilder(info);
            ArrayList<String> uniqueDates = new ArrayList<>();
            for (Measurement measurement: overheatingMeasurements){
                String nowDate = new SimpleDateFormat("dd.MM.yyyy").format(measurement.getDatetime());
                if (!uniqueDates.contains(nowDate)){
                    uniqueDates.add(nowDate);
                    sb.append(nowDate);
                    sb.append(", ");
                }
            }

            // вывод информации превышения температур
            sheet.createRow(tempBorders.getRowNum() + 22).createCell(0).setCellValue(
                    "За выбранный период в области " + controlObject.getName() + " превышения температуры наблюдалось.");
            sheet.createRow(tempBorders.getRowNum() + 23).createCell(0).setCellValue(sb.toString());
        }

        // создание файла отчета
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date()) + ".xlsx";
        FileOutputStream out = new FileOutputStream(System.getProperty("user.dir") + "/src/main/upload/static/reports/" +
                date);
        book.write(out);
        out.close();

        System.out.println("Done");
        return date;
    }

    public static ArrayList<Date> getTotalDates(ControlObject controlObject,
                                                WeatherMeasurementRepo weatherMeasurementRepo,
                                                MeasurementRepo measurementRepo, Date beginningDate,
                                                Date endingDate){
        ArrayList<Measurement> measurements = measurementRepo.getMeasurementByDatetimeInRange(controlObject.getId(),
                beginningDate, endingDate);
        ArrayList<WeatherMeasurement> weatherMeasurements = weatherMeasurementRepo
                .getWeatherMeasurementByDatetimeInRange(beginningDate, endingDate);

        // массив общих дат
        ArrayList<Date> totalDates = new ArrayList<>();

        // добавляем даты измерений с тепловизора
        for (Measurement measurement: measurements) totalDates.add(measurement.getDatetime());

        // добавляем даты измерений с метеостанции
        for (WeatherMeasurement weatherMeasurement: weatherMeasurements){
            if (!totalDates.contains(weatherMeasurement.getDatetime())) totalDates.add(weatherMeasurement.getDatetime());
        }

        Collections.sort(totalDates);

        return totalDates;
    }

    public static ArrayList<ArrayList<Double>> getTotalTemperatures(ControlObject controlObject,
                                                                    WeatherMeasurementRepo weatherMeasurementRepo,
                                                                    MeasurementRepo measurementRepo,
                                                                    ArrayList<Date> totalDates,
                                                                    Date beginningDate, Date endingDate){
        // массивы измерений
        ArrayList<Measurement> measurements = measurementRepo.getMeasurementByDatetimeInRange(controlObject.getId(),
                beginningDate, endingDate);
        if(measurements.size() == 0){
            return new ArrayList<ArrayList<Double>>();
        }
        ArrayList<WeatherMeasurement> weatherMeasurements = weatherMeasurementRepo
                .getWeatherMeasurementByDatetimeInRange(beginningDate, endingDate);

        ArrayList<Double> temperatures = new ArrayList<>();
        ArrayList<Double> weatherTemperatures = new ArrayList<>();

        // проверка массивов на наличие даты и взятие с них соответственных температур
        for (Date date: totalDates){
            for (int i = 0; i < measurements.size(); i++){
                if (measurements.get(i).getDatetime() == date){
                    temperatures.add(measurements.get(i).getTemperature());
                    weatherTemperatures.add(null);
                }
            }
            for (int i = 0; i < weatherMeasurements.size(); i++){
                if (weatherMeasurements.get(i).getDatetime() == date){
                    weatherTemperatures.add(weatherMeasurements.get(i).getTemperature());
                    temperatures.add(null);
                }
            }

        }
        ArrayList<ArrayList<Double>> totalTemperatures = new ArrayList<>();
        totalTemperatures.add(temperatures);
        totalTemperatures.add(weatherTemperatures);

        return totalTemperatures;
    }

    public static XSSFChart createChart(XSSFSheet sheet, int row1, int row2, int col1, int col2, String name){
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("График температуры на точке " + name);
        chart.setTitleOverlay(false);

        return chart;
    }

    public static XDDFLineChartData createData(XSSFChart chart) {

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Дата");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Температура");

        return (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

    }

    public static void createDataSource(XSSFSheet sheet, int rowDat1, int rowDat2, int colDat1, int colDat2,
                                        int rowVal1, int rowVal2, int colVal1, int colVal2,
                                        String name, XDDFLineChartData data){
        XDDFDataSource<String> dates = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(rowDat1, rowDat2, colDat1, colDat2));

        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(rowVal1, rowVal2, colVal1, colVal2));

        XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(dates, values);
        series.setTitle(name, null);
        series.setSmooth(false);
        series.setMarkerStyle(MarkerStyle.CIRCLE);

    }
}
