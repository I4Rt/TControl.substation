package com.i4rt.temperaturecontrol.databaseInterfaces;


import com.i4rt.temperaturecontrol.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;

public interface MeasurementRepo extends JpaRepository<Measurement, Long> {

    @Query(nativeQuery = true, value="SELECT * FROM measurement where control_object_id = :searchId order by measurement_id desc limit :limit")
    ArrayList<Measurement> getMeasurementByDatetime(@Param("searchId") Long searchId, @Param("limit") Integer limit);

    @Query(nativeQuery = true, value="SELECT * FROM measurement where control_object_id = :searchId and datetime >= :begin and datetime <= :end order by measurement_id desc")
    ArrayList<Measurement> getMeasurementByDatetimeInRange(@Param("searchId") Long searchId, @Param("begin") String begin, @Param("end") String end);


    @Query(nativeQuery = true, value="SELECT * FROM measurement where control_object_id = :searchId order by measurement_id desc")
    ArrayList<Measurement> getMeasurementByAreaId(@Param("searchId") Long searchId);                               // Переписать поле на datetime!
}
