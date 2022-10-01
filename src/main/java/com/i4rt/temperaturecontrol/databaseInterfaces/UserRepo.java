package com.i4rt.temperaturecontrol.databaseInterfaces;

import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.login = :login")
    User getByUserLogin(@Param("login") String login);

    @Query("SELECT u FROM User u WHERE u.thermalImagerGrabbed = true")
    User getUserThatGrabbedThermalImager();
}
