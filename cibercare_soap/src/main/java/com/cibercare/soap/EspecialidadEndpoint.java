package com.cibercare.soap;

import com.cibercare.model.Especialidad;
import com.cibercare.repository.IEspecialidadRepository;
import com.cibercare.especialidad.*; // ← Clases generadas a partir de tu XSD

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;

@Endpoint
public class EspecialidadEndpoint {

    private static final String NAMESPACE_URI = "http://www.cibercare.com/especialidad";

    @Autowired
    private IEspecialidadRepository especialidadRepository;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createEspecialidadRequest")
    @ResponsePayload
    public CreateEspecialidadResponse crear(@RequestPayload CreateEspecialidadRequest request) {
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(request.getNombre());
        especialidadRepository.save(especialidad);

        CreateEspecialidadResponse response = new CreateEspecialidadResponse();
        com.cibercare.especialidad.Especialidad esp = new com.cibercare.especialidad.Especialidad();
        esp.setId(especialidad.getId());
        esp.setNombre(especialidad.getNombre());
        response.setEspecialidad(esp);

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getAllEspecialidadesRequest")
    @ResponsePayload
    public GetAllEspecialidadesResponse listar(@RequestPayload GetAllEspecialidadesRequest request) {
        List<Especialidad> lista = especialidadRepository.findAll();

        GetAllEspecialidadesResponse response = new GetAllEspecialidadesResponse();
        for (Especialidad e : lista) {
            com.cibercare.especialidad.Especialidad esp = new com.cibercare.especialidad.Especialidad();
            esp.setId(e.getId());
            esp.setNombre(e.getNombre());
            response.getEspecialidades().add(esp);
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getEspecialidadByIdRequest")
    @ResponsePayload
    public GetEspecialidadByIdResponse buscarPorId(@RequestPayload GetEspecialidadByIdRequest request) {
        Especialidad especialidad = especialidadRepository.findById(request.getId()).orElse(null);

        GetEspecialidadByIdResponse response = new GetEspecialidadByIdResponse();
        if (especialidad != null) {
            com.cibercare.especialidad.Especialidad esp = new com.cibercare.especialidad.Especialidad();
            esp.setId(especialidad.getId());
            esp.setNombre(especialidad.getNombre());
            response.setEspecialidad(esp);
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateEspecialidadRequest")
    @ResponsePayload
    public UpdateEspecialidadResponse actualizar(@RequestPayload UpdateEspecialidadRequest request) {
        Especialidad especialidad = especialidadRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        especialidad.setNombre(request.getNombre());
        especialidadRepository.save(especialidad);

        UpdateEspecialidadResponse response = new UpdateEspecialidadResponse();
        com.cibercare.especialidad.Especialidad esp = new com.cibercare.especialidad.Especialidad();
        esp.setId(especialidad.getId());
        esp.setNombre(especialidad.getNombre());
        response.setEspecialidad(esp);

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteEspecialidadRequest")
    @ResponsePayload
    public DeleteEspecialidadResponse eliminar(@RequestPayload DeleteEspecialidadRequest request) {
        especialidadRepository.deleteById(request.getId());

        DeleteEspecialidadResponse response = new DeleteEspecialidadResponse();
        response.setMensaje("Especialidad eliminada correctamente");
        return response;
    }
}
