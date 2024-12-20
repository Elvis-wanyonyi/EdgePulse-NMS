package com.wolfcode.MikrotikNetwork.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_session", schema = "public")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String macAddress;
    private String username;
    private String routerName;
    private LocalDateTime sessionEndTime;
    private LocalDateTime sessionStartTime;

}
