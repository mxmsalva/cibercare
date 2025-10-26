package com.cibercare.dto;

import lombok.Data;

@Data
public class CitaRequest {
    private Long pacienteId;
    private Long doctorId;
    private Long horarioId;
    private String motivo;
}
