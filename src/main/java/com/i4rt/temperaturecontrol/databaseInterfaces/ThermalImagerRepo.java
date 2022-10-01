package com.i4rt.temperaturecontrol.databaseInterfaces;

import com.i4rt.temperaturecontrol.device.ThermalImager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ThermalImagerRepo extends JpaRepository<ThermalImager, Long> {
    /*
    @Override
    @Transactional(propagation= Propagation.REQUIRED)
    @Query("SELECT ti FROM ThermalImager ti")
    List<ThermalImager> findAll();

     */
}
