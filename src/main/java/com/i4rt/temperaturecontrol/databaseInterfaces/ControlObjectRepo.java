package com.i4rt.temperaturecontrol.databaseInterfaces;

import com.i4rt.temperaturecontrol.model.ControlObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ControlObjectRepo extends JpaRepository<ControlObject, Long> {

    @Query("SELECT o FROM ControlObject o WHERE o.mapX is not NULL and o.mapY is not NULL")
    List<ControlObject> getControlObjectsToDisplay();

    @Query(nativeQuery = true, value="SELECT object_id FROM control_object order by object_id desc limit 1")
    Long getLastObjectId();
}
