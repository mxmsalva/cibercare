// DoctorDTO.java
package com.cibercare.dto;

import lombok.Data;

@Data
public class DoctorDTO {
    private Long id;
    private String nombreCompleto;
    private String especialidad;
    private String descripcion;
}
