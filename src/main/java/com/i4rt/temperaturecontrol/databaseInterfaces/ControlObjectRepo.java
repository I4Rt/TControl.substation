package com.i4rt.temperaturecontrol.databaseInterfaces;

import com.i4rt.temperaturecontrol.model.ControlObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ControlObjectRepo extends JpaRepository<ControlObject, Long> {

    @Query("SELECT o FROM ControlObject o WHERE o.mapX is not NULL and o.mapY is not NULL")
    List<ControlObject> getControlObjectsToDisplay();

    @Query(nativeQuery = true, value="SELECT object_id FROM control_object order by object_id desc limit 1")
    Long getLastObjectId();

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value="delete FROM control_object where object_id = :id")
    void deleteByID(@Param("id") Long id);


    @Query(nativeQuery = true, value="SELECT * FROM control_object where thermal_imager_id = :ti_id")
    List<ControlObject> getControlObjectByTIID(@Param("ti_id") Long id);
}
