package com.wolfcode.MikrotikNetwork.entity;

import com.wolfcode.MikrotikNetwork.dto.PlanDuration;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "plans")
public class PackagePlans {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private  Long id;
    private String routerName;
    private String packageName;
    private String bandwidthLimit;
    private String dataLimit;
    private Integer planValidity;
    @Enumerated(EnumType.STRING)
    private PlanDuration planDuration;
    private int price;
}
