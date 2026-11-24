package com.example.proyec_top.network

data class IncidenciaDto(
    val id: String? = null,
    val codigo: String = "",
    val categoria: String = "",
    val sectorReferencial: String = "",
    val descripcion: String = "",
    val fechaIngreso: String = "",
    val estado: String = "",
    val prioridad: String = "",
    val unidadAsignada: String = ""
)

data class SectorDto(
    val id: String? = null,
    val nombre: String = ""
)

data class AuditoriaDto(
    val id: String? = null,
    val usuario: String = "",
    val rol: String = "",
    val accion: String = "",
    val fecha: String = ""
)