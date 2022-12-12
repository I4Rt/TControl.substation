package com.i4rt.temperaturecontrol.databaseInterfaces;


import com.i4rt.temperaturecontrol.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.Date;

@Repository
public interface MeasurementRepo extends JpaRepository<Measurement, Long> {

    @Query(nativeQuery = true, value="SELECT * FROM measurement where control_object_id = :searchId order by measurement_id desc limit :limit")
    ArrayList<Measurement> getMeasurementByDatetime(@Param("searchId") Long searchId, @Param("limit") Integer limit);

    @Query(nativeQuery = true, value="SELECT * FROM measurement where control_object_id = :searchId and datetime >= :begin and datetime <= :end order by measurement_id desc")
    ArrayList<Measurement> getMeasurementByDatetimeInRange(@Param("searchId") Long searchId, @Param("begin") Date begin, @Param("end") Date end);


    @Query(nativeQuery = true, value="SELECT * FROM measurement where control_object_id = :searchId order by measurement_id desc limit :limit")
    ArrayList<Measurement> getMeasurementByAreaId(@Param("searchId") Long searchId, @Param("limit") Integer limit);                               // Переписать поле на datetime!

    @Query(nativeQuery = true, value ="SELECT * FROM measurement where control_object_id = :searchId and datetime >= :begin and datetime <= :end and temperature >= :dangerTemp order by measurement_id")
    ArrayList<Measurement> getOverheatingMeasurement(@Param("searchId") Long searchId, @Param("dangerTemp") Double dangerTemp, @Param("begin") Date begin,
                                                     @Param("end") Date end);

    @Query(nativeQuery = true, value ="SELECT * FROM measurement where control_object_id = :searchId and datetime >= :begin and datetime <= :end and temperature >= :warningTemp order by measurement_id")
    ArrayList<Measurement> getOverheatingWarningMeasurement(@Param("searchId") Long searchId, @Param("dangerTemp") Double warningTemp, @Param("begin") Date begin,
                                                     @Param("end") Date end);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value="delete FROM measurement where control_object_id = :id")
    void deleteByCOID(@Param("id") Long id);
}
