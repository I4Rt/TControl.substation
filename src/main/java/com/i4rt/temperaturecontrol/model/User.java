package com.i4rt.temperaturecontrol.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "all_users")

public class User  {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String login;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String passwordRepeat;

    @Column(nullable = false)
    private String role;
    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean thermalImagerGrabbed = false;
}
