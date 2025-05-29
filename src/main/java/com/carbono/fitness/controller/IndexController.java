package com.carbono.fitness.controller;

import com.carbono.fitness.entity.Cliente;
import com.carbono.fitness.services.ClienteServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import lombok.Data;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

@Controller
@Data
@ViewScoped
public class IndexController {

    @Autowired
    ClienteServiceImpl clienteService;

    private List<Cliente> clientes;
    private Cliente clienteSeleccionado;

    // Agregar esta propiedad a tu clase IndexController
    private List<Cliente> clientesFiltrados;

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @PostConstruct
    public void init() {
        cargarDatos();
    }

    public void cargarDatos() {
        this.clientes = clienteService.listarClientes();
        clientes.forEach((cliente) -> logger.info(cliente.toString()));
    }

    public void agregarCliente() {
        logger.info("Se crea objeto Cliente seleccionado");
        this.clienteSeleccionado = new Cliente();
    }

    public void guardarCliente() {
        logger.info("cliente a guardar: "+this.clienteSeleccionado);
        if(this.clienteSeleccionado.getId() == null) {
            //Agregar nuevo
            LocalDate fechaInicio = this.clienteSeleccionado.getFechaInicio();
            Integer mesesAContratar = this.clienteSeleccionado.getMesesAContratar();

            if (fechaInicio != null && mesesAContratar != null) {
                LocalDate fechaFin = fechaInicio.plusMonths(mesesAContratar);
                this.clienteSeleccionado.setFechaFin(fechaFin);
            }

            this.clienteService.guardarCliente(this.clienteSeleccionado);
            logger.info("Se ha guardado el cliente correctamente");
            this.clientes.add(this.clienteSeleccionado);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Cliente agregado correctamente"));
        }else{
            //Modificar existente

            LocalDate fechaInicio = this.clienteSeleccionado.getFechaInicio();
            Integer mesesAContratar = this.clienteSeleccionado.getMesesAContratar();

            if (fechaInicio != null && mesesAContratar != null) {
                LocalDate fechaFin = fechaInicio.plusMonths(mesesAContratar);
                this.clienteSeleccionado.setFechaFin(fechaFin);
            }

            this.clienteService.guardarCliente(this.clienteSeleccionado);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Cliente actualizado correctamente"));
        }

        //Ocultamos la ventana
        PrimeFaces.current().executeScript("PF('ventanaModalCliente').hide()");

        //Actualizamos la tabla
        PrimeFaces.current().ajax().update("forma-clientes:mensajes",
                "forma-clientes:clientes-tabla");

        // Reset del objeto cliente seleccionado
        this.clienteSeleccionado = null;
    }

    public void eliminarCliente() {
        logger.info("cliente a eliminar: "+this.clienteSeleccionado);
        this.clienteService.eliminarCliente(this.clienteSeleccionado);

        // Eliminar el registro de la lista de cuentas
        this.clientes.remove(this.clienteSeleccionado);

        // Reset del objeto seleccionado de la tabla
        this.clienteSeleccionado = null;

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Cliente eliminado correctamente"));
        PrimeFaces.current().ajax().update("forma-clientes:mensajes",
                "forma-clientes:clientes-tabla");
    }

    public String formatearLocalDate(LocalDate fecha) {
        if (fecha == null) return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy", new Locale("es"));
        return fecha.format(formatter);
    }

    // logica para monitoreo de suscripciones
    public void monitoriarSuscripcion() {
        logger.info("Iniciando monitoreo de suscripciones...");

        LocalDate fechaActual = LocalDate.now();
        List<Cliente> clientesVencidos = new ArrayList<>();
        List<Cliente> clientesPorVencer = new ArrayList<>();

        // Buscar clientes con suscripciones vencidas o por vencer
        for (Cliente cliente : this.clientes) {
            if (cliente.getFechaFin() != null) {
                // Suscripción vencida (fecha fin es igual o anterior a hoy)
                if (cliente.getFechaFin().isBefore(fechaActual) || cliente.getFechaFin().isEqual(fechaActual)) {
                    clientesVencidos.add(cliente);
                    logger.warn("Cliente con suscripción vencida: {} - Vencimiento: {}",
                            cliente.getNombre() + " " + cliente.getPrimerApellido(),
                            cliente.getFechaFin());
                }
                // Suscripción por vencer en los próximos 7 días
                else if (cliente.getFechaFin().isBefore(fechaActual.plusDays(7))) {
                    clientesPorVencer.add(cliente);
                    logger.info("Cliente con suscripción por vencer: {} - Vencimiento: {}",
                            cliente.getNombre() + " " + cliente.getPrimerApellido(),
                            cliente.getFechaFin());
                }
            }
        }

        // Mostrar alertas para suscripciones vencidas
        if (!clientesVencidos.isEmpty()) {
            StringBuilder mensaje = new StringBuilder("⚠️ SUSCRIPCIONES VENCIDAS:<br/><br/>");
            for (Cliente cliente : clientesVencidos) {
                mensaje.append("• ")
                        .append(cliente.getNombre()).append(" ")
                        .append(cliente.getPrimerApellido())
                        .append(" - Venció: ")
                        .append(formatearLocalDate(cliente.getFechaFin()))
                        .append("<br/>");
            }

            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Suscripciones Vencidas (" + clientesVencidos.size() + ")",
                    mensaje.toString());
            facesMessage.setDetail(mensaje.toString());
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        }

        // Mostrar alertas para suscripciones por vencer
        if (!clientesPorVencer.isEmpty()) {
            StringBuilder mensaje = new StringBuilder("🔔 SUSCRIPCIONES POR VENCER (próximos 7 días):<br/><br/>");
            for (Cliente cliente : clientesPorVencer) {
                long diasRestantes = ChronoUnit.DAYS.between(fechaActual, cliente.getFechaFin());
                mensaje.append("• ")
                        .append(cliente.getNombre()).append(" ")
                        .append(cliente.getPrimerApellido())
                        .append(" - Vence en ")
                        .append(diasRestantes)
                        .append(" día(s): ")
                        .append(formatearLocalDate(cliente.getFechaFin()))
                        .append("<br/>");
            }

            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Suscripciones por Vencer (" + clientesPorVencer.size() + ")",
                    mensaje.toString());
            facesMessage.setDetail(mensaje.toString());
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        }

        // Si no hay problemas, mostrar mensaje informativo
        if (clientesVencidos.isEmpty() && clientesPorVencer.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Estado de Suscripciones",
                            "✅ Todas las suscripciones están al día"));
        }

        // Actualizar los mensajes en la interfaz
        PrimeFaces.current().ajax().update("forma-clientes:mensajes");

        logger.info("Monitoreo completado - Vencidas: {}, Por vencer: {}",
                clientesVencidos.size(), clientesPorVencer.size());
    }

    // Agregar este método para filtro personalizado (opcional, si quieres más control)
    public boolean filtroGlobalPersonalizado(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        if (value == null) {
            return false;
        }

        Cliente cliente = (Cliente) value;

        // Buscar en nombre, primer apellido y segundo apellido
        String nombreCompleto = (cliente.getNombre() + " " +
                cliente.getPrimerApellido() + " " +
                cliente.getSegundoApellido()).toLowerCase();

        return nombreCompleto.contains(filterText);
    }
    
}
