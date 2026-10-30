package com.example.proyec_top.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyec_top.data.IncidenciaEntity
import com.example.proyec_top.data.TopSecurityRepository
import com.example.proyec_top.data.TrazabilidadEntity
import com.example.proyec_top.model.EstadoIncidencia
import com.example.proyec_top.model.IncidenciaSeguridad
import com.example.proyec_top.model.NivelPrioridad
import com.example.proyec_top.model.RegistroTrazabilidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Mantiene el estado que muestra la interfaz y coordina las acciones del usuario.
 * La lectura y escritura persistente se delega al repositorio.
 */
class TopSecurityViewModel(private val repository: TopSecurityRepository) : ViewModel() {

    val sesionUsuario = repository.observarSesion().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val incidencias: StateFlow<List<IncidenciaSeguridad>> = combine(
        repository.observarIncidencias(),
        repository.observarTrazabilidad()
    ) { casosGuardados, accionesGuardadas ->
        casosGuardados.map { caso ->
            val historial = accionesGuardadas
                .filter { it.incidenciaId == caso.id }
                .sortedBy { it.creadoEn }
                .map { accion ->
                    RegistroTrazabilidad(
                        marcaTiempo = accion.marcaTiempo,
                        autorRol = accion.autorRol,
                        detalleAccion = accion.detalleAccion
                    )
                }

            IncidenciaSeguridad(
                id = caso.id,
                categoria = caso.categoria,
                sectorReferencial = caso.sectorReferencial,
                descripcion = caso.descripcion,
                fechaIngreso = caso.fechaIngreso,
                estado = EstadoIncidencia.values().firstOrNull { it.name == caso.estado }
                    ?: EstadoIncidencia.INGRESADO,
                prioridad = NivelPrioridad.values().firstOrNull { it.name == caso.prioridad }
                    ?: NivelPrioridad.POR_EVALUAR,
                unidadAsignada = caso.unidadAsignada,
                trazabilidad = historial
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _filtroEstado = MutableStateFlow<EstadoIncidencia?>(null)
    val filtroEstado: StateFlow<EstadoIncidencia?> = _filtroEstado.asStateFlow()

    init {
        viewModelScope.launch {
            repository.insertarDatosDeDemostracionSiEsNecesario()
        }
    }

    fun guardarSesionLocal(alias: String, rol: String) {
        viewModelScope.launch { repository.guardarSesion(alias.trim(), rol) }
    }

    fun cerrarSesion() {
        viewModelScope.launch { repository.cerrarSesion() }
    }

    fun aplicarFiltro(estado: EstadoIncidencia?) {
        _filtroEstado.value = estado
    }

    fun registrarIncidencia(
        categoria: String,
        sector: String,
        descripcion: String,
        foto: Bitmap?
    ) {
        val ahora = System.currentTimeMillis()
        val hora = TopSecurityRepository.formatearFecha(ahora)
        val idNuevo = "SJ-$ahora"
        val incidencia = IncidenciaEntity(
            id = idNuevo,
            categoria = categoria.trim(),
            sectorReferencial = sector.trim(),
            descripcion = descripcion.trim(),
            fechaIngreso = hora,
            estado = EstadoIncidencia.INGRESADO.name,
            prioridad = NivelPrioridad.POR_EVALUAR.name,
            unidadAsignada = "Sin asignar",
            creadoEn = ahora
        )
        val detalleInicial = if (foto != null) {
            "Incidencia ingresada. Se capturó una evidencia de demostración; la imagen no se conserva al cerrar el formulario."
        } else {
            "Incidencia ingresada correctamente con datos ficticios."
        }
        val primeraAccion = TrazabilidadEntity(
            incidenciaId = idNuevo,
            marcaTiempo = hora,
            autorRol = "Vecino / Residente",
            detalleAccion = detalleInicial,
            creadoEn = ahora
        )

        viewModelScope.launch {
            repository.registrarIncidencia(incidencia, primeraAccion)
        }
    }

    fun gestionarIncidencia(
        id: String,
        nuevoEstado: EstadoIncidencia,
        nuevaPrioridad: NivelPrioridad,
        unidad: String,
        observacion: String,
        autorRol: String
    ) {
        viewModelScope.launch {
            val casoActual = repository.obtenerIncidencia(id) ?: return@launch
            val ahora = System.currentTimeMillis()
            val hora = TopSecurityRepository.formatearFecha(ahora)
            val unidadFinal = unidad.trim().ifBlank { casoActual.unidadAsignada }
            val observacionFinal = observacion.trim().ifBlank { "Sin observaciones adicionales." }
            val actualizado = casoActual.copy(
                estado = nuevoEstado.name,
                prioridad = nuevaPrioridad.name,
                unidadAsignada = unidadFinal
            )
            val accion = TrazabilidadEntity(
                incidenciaId = id,
                marcaTiempo = hora,
                autorRol = autorRol,
                detalleAccion = "Estado: ${nuevoEstado.display}. Prioridad: ${nuevaPrioridad.nivel}. " +
                    "Responsable: $unidadFinal. Observación: $observacionFinal",
                creadoEn = ahora
            )
            repository.guardarGestion(actualizado, accion)
        }
    }
}

class TopSecurityViewModelFactory(
    private val repository: TopSecurityRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopSecurityViewModel::class.java)) {
            return TopSecurityViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
