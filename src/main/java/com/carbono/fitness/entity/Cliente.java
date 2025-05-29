package com.carbono.fitness.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //@JsonProperty("nombre")
    @Column(name= "nombre", nullable = false, length = 50)
    private String nombre;

    //@JsonProperty("primerApellido")
    @Column(name = "primer_apellido", nullable = false, length = 100)
    private String primerApellido;

    //@JsonProperty("segundoApellido")
    @Column(name = "segundo_apellido", length = 100)
    private String segundoApellido;

    //@JsonProperty("fechaInicio")
    @Column(name = "fecha_inicio", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    //@JsonProperty("fechaFin")
    @Column(name = "fecha_fin")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    //@JsonProperty("mesesAContratar")
    @Column(name = "meses_a_contratar")
    private Integer mesesAContratar;
}
