//
// Este archivo ha sido generado por Eclipse Implementation of JAXB v3.0.0 
// Visite https://eclipse-ee4j.github.io/jaxb-ri 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2025.10.19 a las 11:19:43 AM PET 
//


package com.cibercare.especialidad;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para anonymous complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="especialidad" type="{http://www.cibercare.com/especialidad}Especialidad"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "especialidad"
})
@XmlRootElement(name = "updateEspecialidadResponse")
public class UpdateEspecialidadResponse {

    @XmlElement(required = true)
    protected Especialidad especialidad;

    /**
     * Obtiene el valor de la propiedad especialidad.
     * 
     * @return
     *     possible object is
     *     {@link Especialidad }
     *     
     */
    public Especialidad getEspecialidad() {
        return especialidad;
    }

    /**
     * Define el valor de la propiedad especialidad.
     * 
     * @param value
     *     allowed object is
     *     {@link Especialidad }
     *     
     */
    public void setEspecialidad(Especialidad value) {
        this.especialidad = value;
    }

}
