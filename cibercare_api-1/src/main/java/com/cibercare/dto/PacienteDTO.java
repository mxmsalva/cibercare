// PacienteDTO.java
package com.cibercare.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PacienteDTO {
    private Long id;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
}
