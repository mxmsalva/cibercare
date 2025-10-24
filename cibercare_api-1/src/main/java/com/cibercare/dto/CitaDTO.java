package com.cibercare.dto;

import lombok.Data;

@Data
public class CitaDTO {
	 private Long id;
	    private String pacienteNombre;
	    private String doctorNombre;
	    private String especialidad;
	    private String fecha;
	    private String hora;
	    private String estado;
}
