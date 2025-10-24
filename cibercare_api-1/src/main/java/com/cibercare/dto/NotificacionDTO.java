package com.cibercare.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificacionDTO {
    private Long id;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;
    private String doctorNombre;
}
