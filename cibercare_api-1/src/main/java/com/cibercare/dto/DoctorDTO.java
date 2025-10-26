package com.cibercare.dto;

import lombok.Data;

@Data
public class DoctorDTO {
    private Long usuarioId;
    private Long especialidadId;
    private String descripcion;
}
