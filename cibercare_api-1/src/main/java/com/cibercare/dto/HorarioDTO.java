// HorarioDTO.java
package com.cibercare.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class HorarioDTO {
    private Long id;
    private String doctorNombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean disponible;
}
