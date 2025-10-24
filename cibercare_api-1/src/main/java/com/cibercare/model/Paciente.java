package com.cibercare.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name = "paciente")
@Data
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
}
