package com.cibercare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.cibercare.model.Doctor;
import com.cibercare.model.Notificacion;
import com.cibercare.repository.INotificacionRepository;

@Service
public class NotificacionService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private INotificacionRepository notificacionRepository;

    public void enviarNotificacion(Doctor doctor, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setDoctor(doctor);
        notificacion.setMensaje(mensaje);
        notificacionRepository.save(notificacion);
        messagingTemplate.convertAndSend("/topic/notificaciones/" + doctor.getId(), notificacion);
    }
}