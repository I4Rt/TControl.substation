package com.i4rt.temperaturecontrol.databaseInterfaces;

import com.i4rt.temperaturecontrol.model.MIPMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MIPMeasurementRepo extends JpaRepository<MIPMeasurement, Long> {

}
