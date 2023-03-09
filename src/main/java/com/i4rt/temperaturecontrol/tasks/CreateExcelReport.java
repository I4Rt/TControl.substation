package com.i4rt.temperaturecontrol.tasks;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class CreateExcelReport {

    private final ControlObjectRepo controlObjectRepo;
    private final MeasurementRepo measurementRepo;
    private final WeatherMeasurementRepo weatherMeasurementRepo;
    private final MIPMeasurementRepo mipMeasurementRepo;

    public CreateExcelReport(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo,
                             WeatherMeasurementRepo weatherMeasurementRepo, MIPMeasurementRepo mipMeasurementRepo){
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.mipMeasurementRepo = mipMeasurementRepo;
    }

    public String createMainSheet(Date beginningDate, Date endingDate, int[] areaIdList) throws IOException {

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
        dateStyle.setDataFormat(format.getFormat("dd.mm.yyyy hh:mm:ss"));

        ArrayList<ControlObject> controlObjects = new ArrayList<>();

        if (!(areaIdList.length == 0)){
            for (int id: areaIdList){
                controlObjects.add(controlObjectRepo.getControlObjectById(id));
            }
//            areaInfo(book, sheet, beginningDate, endingDate, greenStyle, areaList, 1, dateStyle);
        }
        fillMainSheet(book, sheet, beginningDate, endingDate, greenStyle, redStyle, dateStyle, controlObjects);

        return createFile(book);
    }

    public void fillMainSheet(XSSFWorkbook book, XSSFSheet sheet, Date beginningDate, Date endingDate, CellStyle greenStyle,
                              CellStyle redStyle, CellStyle dateStyle, ArrayList<ControlObject> controlObjects) {

        ArrayList<ControlObject> overheatingAreaList = new ArrayList<>();

        // список всех подконтрольных объектов
        if (controlObjects.isEmpty()) controlObjects = (ArrayList<ControlObject>) this.controlObjectRepo.findAll();
//        else areaList.addAll(controlObjects);

        // строки занимаемые выводом ячеек
        int rowSize = controlObjects.size() / 5;
        int columnSize = 5;

        System.out.println("\nСоздание листа\n");

        // вывод всех подконтрольных областей и задание им стиля
        int numCellColumn;
        for (int i = 0; i < rowSize + 1; i++) {
            Row row = sheet.createRow(i);
            for (int j = i * 5; j < i * 5 + columnSize; j++) {
                if (j >= controlObjects.size()) {
                    break;
                }
                numCellColumn = j - i * 5;
                Cell cell = row.createCell(numCellColumn);
                sheet.autoSizeColumn(numCellColumn);
                ControlObject controlObject = controlObjects.get(j);
                boolean overheating = false;

                //проверка на перегретость
//                List<Measurement> measurements = controlObject.getMeasurement();
                if (!measurementRepo.getOverheatingMeasurement(controlObject.getId(),
                        (Double) controlObject.getDangerTemp(), beginningDate, endingDate).isEmpty())
                    overheating = true;
                if (!measurementRepo.getOverheatingWarningMeasurement(controlObject.getId(),
                        (Double) controlObject.getWarningTemp(), beginningDate, endingDate).isEmpty())
                    overheating = true;

                if (!overheating) cell.setCellStyle(greenStyle);
                else {
                    cell.setCellStyle(redStyle);
                    overheatingAreaList.add(controlObject);
                }
                cell.setCellValue(controlObjects.get(j).getName());
            }
        }
        areaInfo(book, sheet, beginningDate, endingDate, greenStyle, redStyle, controlObjects, overheatingAreaList, rowSize, dateStyle);
    }

    public void areaInfo(XSSFWorkbook book, XSSFSheet sheet, Date beginningDate, Date endingDate, CellStyle greenStyle,
                         CellStyle redStyle, ArrayList<ControlObject> controlObjects, ArrayList<ControlObject> overheatingAreaList,
                         int rowSize, CellStyle dateStyle) {

        System.out.println("\nВывод областей\n");

        Integer index = 0;
        for (ControlObject controlObject : controlObjects) {
            boolean overheatingArea = overheatingAreaList.contains(controlObject);
//            List<Measurement> measurements = controlObject.getMeasurement();
            index += 1;
            System.out.println("Area " + index + " of " + controlObjects.size());

            // эксель лист под каждую область с температурами
            XSSFSheet cellSheet = book.createSheet("Область #" + controlObject.getId() + "-" + controlObject.getName());

            // название области
            Row areaRow = cellSheet.createRow(0);
            Cell areaCell = areaRow.createCell(0);

            areaCell.setCellValue("Область " + controlObject.getName());
            if (overheatingArea) areaCell.setCellStyle(redStyle);
            else areaCell.setCellStyle(greenStyle);

            // номера столбцов дат и значений
            int numCellDate = 0;
            int numCellValue = 1;
            int numCellWeather = 4;
            int numCellMip = 5;

            System.out.println("\nGet total values\n");

            ArrayList<Object> allData = getTotalValues(controlObject, weatherMeasurementRepo,
                    measurementRepo, mipMeasurementRepo, beginningDate, endingDate);
            ArrayList<Date> totalDates = (ArrayList<Date>) allData.get(0);
            Collections.sort(totalDates);
            Map<Date, Object> preparedToSendData = (Map<Date, Object>) allData.get(1);

            if (preparedToSendData.isEmpty()) {
                continue;
            }

            System.out.println("\nПеред выводом измерений на отдельные листы\n");

            // вывод измерений на отдельный лист эксель
            Calendar cal = Calendar.getInstance();
            cal.setTime(totalDates.get(0));
            cal.add(Calendar.HOUR, 1);
            Date datePlusMinute = cal.getTime();

            ArrayList<Date> minuteDates = new ArrayList<>();
            int rowCount = 2;

            for (int i = 0; i < totalDates.size(); i++) {
//                System.out.println("time: " + totalDates.get(i));
//                System.out.println(datePlusMinute);
                if (!totalDates.get(i).before(datePlusMinute)) {
//                    System.out.println("check");

                    rowCount++;
                    Double weatherTemp = 0.0;
                    Double temp = 0.0;
                    Double mipPower = 0.0;
                    Double maxWeatherTemp = -273.0;
                    Double maxTemp = -273.0;
                    Double maxMipPower = -273.0;

                    int weatherCount = 0;
                    int thermalCount = 0;
                    int mipCount = 0;
                    for (int j = 0; j < minuteDates.size(); j++) {
                        if (preparedToSendData.get(minuteDates.get(j)) instanceof WeatherMeasurement) {
                            Double value = ((WeatherMeasurement) preparedToSendData.get(minuteDates.get(j))).getTemperature();
//                            System.out.println("w: " + value);
                            weatherTemp += value;
                            weatherCount++;
                            if (value >= maxWeatherTemp) maxWeatherTemp = value;
                        }
                        if (preparedToSendData.get(minuteDates.get(j)) instanceof Measurement) {
                            Double value = ((Measurement) preparedToSendData.get(minuteDates.get(j))).getTemperature();
//                            System.out.println(((Measurement) preparedToSendData.get(minuteDates.get(j))).getDatetime());
//                            System.out.println("t: " + value);
                            temp += value;
                            thermalCount++;
                            if (value >= maxTemp) maxTemp = value;
                        }
                        if (preparedToSendData.get(minuteDates.get(j)) instanceof MIPMeasurement) {
                            String voltageMeasurementChannel = controlObject.getVoltageMeasurementChannel();
                            Double value;
                            if (voltageMeasurementChannel.equals("A")) value = ((MIPMeasurement)
                                    preparedToSendData.get(minuteDates.get(j))).getPowerA();
                            else if (voltageMeasurementChannel.equals("B")) value = ((MIPMeasurement)
                                    preparedToSendData.get(minuteDates.get(j))).getPowerB();
                            else value = ((MIPMeasurement) preparedToSendData.get(minuteDates.get(j))).getPowerC();

//                            System.out.println("m: " + value);
                            mipPower += value;
                            mipCount++;
                            if (value >= maxMipPower) maxMipPower = value;
                        }
                    }
                    Row rowInf = cellSheet.createRow(rowCount);

                    Cell cellDate = rowInf.createCell(numCellDate);
                    cellDate.setCellStyle(dateStyle);
                    cellDate.setCellValue(minuteDates.get(0));
                    cellSheet.autoSizeColumn(numCellDate);

                    Cell cellValue = rowInf.createCell(numCellValue);
                    if (thermalCount != 0) cellValue.setCellValue(temp / thermalCount);
                    else cellValue.setBlank();

                    Cell cellWeather = rowInf.createCell(numCellWeather);
                    if (weatherCount != 0) cellWeather.setCellValue(weatherTemp / weatherCount);
                    else cellWeather.setBlank();

                    Cell cellMip = rowInf.createCell(numCellMip);
                    if (mipCount != 0) cellMip.setCellValue((mipPower / mipCount) / 1000);
                    else cellMip.setBlank();

                    cal.setTime(totalDates.get(i));
                    cal.add(Calendar.HOUR, 1);
                    datePlusMinute = cal.getTime();

                    minuteDates.removeAll(minuteDates.subList(0, minuteDates.size()));
//                    System.out.println(minuteDates.toString());
                }

                minuteDates.add(totalDates.get(i));
            }

            System.out.println("\nПеред перегретыми точками\n");

            // информация по перегретым точкам на главном эксель листе
            Row row = sheet.createRow(2 + rowSize + 26 * controlObjects.indexOf(controlObject));
            Cell cell = row.createCell(0);
            cell.setCellValue("Область " + controlObject.getName());
            double columnWeight = (cell.toString().length() * 0.44 + 2.24) * 450;
            sheet.setColumnWidth(0, (int) columnWeight);
            if (overheatingArea) cell.setCellStyle(redStyle);
            else cell.setCellStyle(greenStyle);

            // строка с граничными температурами
            Row tempBorders = sheet.createRow(row.getRowNum() + 1);
            tempBorders.createCell(0).setCellValue("Граничные температуры:");
            tempBorders.createCell(3).setCellValue(controlObject.getWarningTemp());
            tempBorders.createCell(4).setCellValue(controlObject.getDangerTemp());

            // график температур
            XSSFChart chart = createChart(sheet, tempBorders.getRowNum() + 2, tempBorders.getRowNum() + 21,
                    0, 13, controlObject.getName());
            XDDFLineChartData data = createData(chart);

            createDataSource(cellSheet, 2, rowCount, numCellDate, numCellDate, 2, rowCount,
                    numCellValue, numCellValue, "Область " + controlObject.getName(), data);

            createDataSource(cellSheet, 2, rowCount, numCellDate, numCellDate, 2, rowCount,
                    numCellWeather, numCellWeather, "Температура воздуха", data);

            createDataSource(cellSheet, 2, rowCount, numCellDate, numCellDate, 2, rowCount,
                    numCellMip, numCellMip, "Измерения с МИП, значение*1000", data);

            // пропуски соединяются линией
            chart.displayBlanksAs(DisplayBlanks.SPAN);

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.TOP);

            chart.plot(data);

            CTPlotArea plotArea = chart.getCTChart().getPlotArea();
            plotArea.getValAxArray()[0].addNewMajorGridlines();

            ArrayList<Measurement> overheatingMeasurements = measurementRepo.getOverheatingMeasurement(
                    controlObject.getId(), (Double) controlObject.getDangerTemp(), beginningDate, endingDate);
            ArrayList<Measurement> overheatingWarningMeasurements = measurementRepo.getOverheatingWarningMeasurement(
                    controlObject.getId(), (Double) controlObject.getWarningTemp(), beginningDate, endingDate);
            overheatingMeasurements.addAll(overheatingWarningMeasurements);
            String info = "Время превышения температур: ";
            StringBuilder sb = new StringBuilder(info);
            ArrayList<String> uniqueDates = new ArrayList<>();
            for (Measurement measurement : overheatingMeasurements) {
                String nowDate = new SimpleDateFormat("dd.MM.yyyy").format(measurement.getDatetime());
                if (!uniqueDates.contains(nowDate)) {
                    uniqueDates.add(nowDate);
                    sb.append(nowDate);
                    sb.append(", ");
                }
            }

            // вывод информации превышения температур
            if (overheatingArea){
                sheet.createRow(tempBorders.getRowNum() + 22).createCell(0).setCellValue(
                        "За выбранный период в области " + controlObject.getName() + " превышения температуры наблюдалось.");
                sheet.createRow(tempBorders.getRowNum() + 23).createCell(0).setCellValue(sb.toString());
            }
        }
        System.out.println("\nОтчет закончен\n");
    }

    public String createFile(XSSFWorkbook book) throws IOException {

        // создание файла отчета
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".xlsx";
        FileOutputStream out = new FileOutputStream(System.getProperty("user.dir") + "/src/main/upload/static/reports/" +
                date);
        book.write(out);
        out.close();

        System.out.println("Done");
        return date;
    }

    public static ArrayList<Object> getTotalValues(ControlObject controlObject,
                                                   WeatherMeasurementRepo weatherMeasurementRepo,
                                                   MeasurementRepo measurementRepo,
                                                   MIPMeasurementRepo mipMeasurementRepo, Date beginningDate,
                                                   Date endingDate){
        ArrayList<MeasurementData> measurements = new ArrayList<>();
        measurements.addAll( weatherMeasurementRepo.getWeatherMeasurementByDatetimeInRange(beginningDate, endingDate));
        measurements.addAll( measurementRepo.getMeasurementByDatetimeInRange(controlObject.getId(), beginningDate, endingDate));
        measurements.addAll( mipMeasurementRepo.getMIPMeasurementByDatetimeInRange(beginningDate, endingDate));
        Map<Date, Object> preparedToSendData = new HashMap<>();
        ArrayList<Date> totalDates = new ArrayList<>();

//        preparedToSendData.put("time", new ArrayList<Date>());
//        preparedToSendData.put("weatherTemperature", new ArrayList<>());

        for(MeasurementData obj: measurements){
            if(obj instanceof WeatherMeasurement){
                WeatherMeasurement finObj = (WeatherMeasurement) obj;
                totalDates.add(finObj.getDatetime());
                preparedToSendData.put(finObj.getDatetime(), finObj);
            }
            else if(obj instanceof Measurement){
                Measurement finObj = (Measurement) obj;
                totalDates.add(finObj.getDatetime());
                preparedToSendData.put(finObj.getDatetime(), finObj);
            }
            else if (obj instanceof MIPMeasurement){
                MIPMeasurement finObj = (MIPMeasurement) obj;
                totalDates.add(finObj.getDatetime());
                preparedToSendData.put(finObj.getDatetime(), finObj);
            }

        }

        ArrayList<Object> results = new ArrayList<>();
        results.add(totalDates);
        results.add(preparedToSendData);
//        ArrayList<Measurement> measurements = measurementRepo.getMeasurementByDatetimeInRange(controlObject.getId(),
//                beginningDate, endingDate);
//        ArrayList<WeatherMeasurement> weatherMeasurements = weatherMeasurementRepo
//                .getWeatherMeasurementByDatetimeInRange(beginningDate, endingDate);
//
//        // массив общих дат
//        ArrayList<Date> totalDates = new ArrayList<>();
//
//        // добавляем даты измерений с тепловизора
//        for (Measurement measurement: measurements) totalDates.add(measurement.getDatetime());
//
//        // добавляем даты измерений с метеостанции
//        for (WeatherMeasurement weatherMeasurement: weatherMeasurements){
//            if (!totalDates.contains(weatherMeasurement.getDatetime())) totalDates.add(weatherMeasurement.getDatetime());
//        }
//
//        Collections.sort(totalDates);

        return results;
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
