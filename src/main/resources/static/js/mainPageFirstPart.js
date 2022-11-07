let needShowFirstTIError = true;
let needShowSecondTIError = true;
let needShowThirdTIError = true;
let needShowFourthTIError = true;
let needShowWeatherStationError = true;
let needBeep = false;
let firstTIError = false;
let secondTIError = false;
let thirdTIError = false;
let fourthTIError = false;
let weatherStationError = false;



let myJsonString = "[" +
    "[" +
    "{\"id\": 1, \"name\": \"Зона 1\", \"temperatureClass\": \"normal\", \"mapX\": 210, \"mapY\": 195}," +
    "{\"id\": 2, \"name\": \"Зона 2\", \"temperatureClass\": \"normal\", \"mapX\": 180, \"mapY\": 195}," +
    "{\"id\": 3, \"name\": \"Зона 3\", \"temperatureClass\": \"danger\", \"mapX\": 150, \"mapY\": 195}," +
    "{\"id\": 4, \"name\": \"Зона 3\", \"temperatureClass\": \"warning\", \"mapX\": null, \"mapY\": null}" +
    "], " +
    "[" +
    "{\"id\": 1, \"name\": \"Зона 1\", \"temperatureClass\": \"normal\", \"mapX\": 210, \"mapY\": 195}," +
    "{\"id\": 2, \"name\": \"Зона 2\", \"temperatureClass\": \"normal\", \"mapX\": 180, \"mapY\": 195}," +
    "{\"id\": 3, \"name\": \"Зона 3\", \"temperatureClass\": \"danger\", \"mapX\": 150, \"mapY\": 195}" +
    "]" +
    "]";