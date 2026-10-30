package com.example.proyec_top.model

enum class RolSeguridad(val titulo: String) {
    VECINO("Vecino / Residente"),
    OPERADOR("Operador Municipal"),
    INSPECTOR("Inspector en Terreno")
}

enum class EstadoIncidencia(val display: String) {
    INGRESADO("Ingresado"),
    EN_REVISION("En revisión"),
    DERIVADO("Derivado a inspector"),
    CERRADO("Cerrado / resuelto")
}

enum class NivelPrioridad(val nivel: String) {
    POR_EVALUAR("Por evaluar"),
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta")
}

data class RegistroTrazabilidad(
    val marcaTiempo: String,
    val autorRol: String,
    val detalleAccion: String
)

/** Modelo que consume la interfaz; la persistencia vive en las entidades Room. */
data class IncidenciaSeguridad(
    val id: String,
    val categoria: String,
    val sectorReferencial: String,
    val descripcion: String,
    val fechaIngreso: String,
    val estado: EstadoIncidencia = EstadoIncidencia.INGRESADO,
    val prioridad: NivelPrioridad = NivelPrioridad.POR_EVALUAR,
    val unidadAsignada: String = "Sin asignar",
    val trazabilidad: List<RegistroTrazabilidad> = emptyList()
)
