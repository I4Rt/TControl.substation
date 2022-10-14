package com.i4rt.temperaturecontrol.databaseInterfaces;

import com.i4rt.temperaturecontrol.device.WeatherStation;
import com.i4rt.temperaturecontrol.model.WeatherMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherMeasurementRepo extends JpaRepository<WeatherMeasurement, Long> {
    @Query(nativeQuery = true, value="SELECT * FROM weather_measurement order by weather_measurement_id DESC limit 1")
    WeatherMeasurement getLastWeatherMeasurement();

    @Query(nativeQuery = true, value="SELECT * FROM weather_measurement order by weather_measurement_id DESC limit :limit")
    List<WeatherMeasurement> getLastWeatherMeasurements(@Param("limit") Long limit);
}
