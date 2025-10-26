package com.cibercare.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class PacienteDTO {
    private Long usuarioId;
    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
}
