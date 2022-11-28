package com.i4rt.temperaturecontrol.databaseInterfaces;

import com.i4rt.temperaturecontrol.model.MIPMeasurement;
import com.i4rt.temperaturecontrol.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;

@Repository
public interface MIPMeasurementRepo extends JpaRepository<MIPMeasurement, Long> {
    @Query(nativeQuery = true, value="SELECT * FROM mipmeasurement where datetime = :datetime")
    ArrayList<MIPMeasurement> getByDatetime(@Param("date") String datetime);


    @Query(nativeQuery = true, value="SELECT * FROM mipmeasurement where datetime >= :begin and datetime <= :end order by mip_measurement_id desc")
    ArrayList<MIPMeasurement> getMIPMeasurementByDatetimeInRange(@Param("begin") Date begin, @Param("end") Date end);

}
