package com.example.proyec_top.repository

import androidx.room.withTransaction
import com.example.proyec_top.network.ApiService
import com.example.proyec_top.network.IncidenciaDto
import com.example.proyec_top.network.RetrofitClient
import com.example.proyec_top.network.SectorDto
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centraliza el acceso a Room y sincroniza los datos con la API externa (MockAPI).
 */
class TopSecurityRepository(
    private val database: AppDatabase,
    private val api: ApiService = RetrofitClient.api
) {
    private val usuarioDao = database.usuarioDao()
    private val incidenciaDao = database.incidenciaDao()

    fun observarSesion(): Flow<UsuarioSesionEntity?> = usuarioDao.obtenerSesionActiva()

    fun observarIncidencias(): Flow<List<IncidenciaEntity>> = incidenciaDao.observarIncidencias()

    fun observarTrazabilidad(): Flow<List<TrazabilidadEntity>> = incidenciaDao.observarTrazabilidad()

    suspend fun guardarSesion(alias: String, rol: String) {
        usuarioDao.guardarSesion(UsuarioSesionEntity(alias = alias, rol = rol))
    }

    suspend fun cerrarSesion() {
        usuarioDao.eliminarSesion()
    }

    // Endpoint 1 (GET /incidencias): descarga los casos desde MockAPI y los guarda en Room
    suspend fun sincronizarIncidenciasDesdeApi() {
        try {
            val remotas = api.getIncidencias()
            database.withTransaction {
                remotas.forEach { dto ->
                    val entidad = dto.toEntity()
                    try {
                        incidenciaDao.insertar(entidad)
                    } catch (_: Exception) {
                        incidenciaDao.actualizar(entidad)
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback offline: Room mantiene los datos locales
        }
    }

    // Endpoint 2 (GET /incidencias/{id}): consulta el detalle actualizado en la API y luego en Room
    suspend fun obtenerIncidencia(id: String): IncidenciaEntity? {
        try {
            val idRemoto = resolverIdRemoto(id)
            val dto = api.getIncidenciaPorId(idRemoto)
            val entidad = dto.toEntity()
            incidenciaDao.actualizar(entidad)
        } catch (_: Exception) {
            // Si falla la red, lee directamente desde Room
        }
        return incidenciaDao.obtenerPorId(id)
    }

    // Endpoint 3 (POST /incidencias): guarda en Room y envía el nuevo caso a MockAPI
    suspend fun registrarIncidencia(incidencia: IncidenciaEntity, primeraAccion: TrazabilidadEntity) {
        database.withTransaction {
            incidenciaDao.insertar(incidencia)
            incidenciaDao.insertarAccion(primeraAccion)
        }
        try {
            api.crearIncidencia(incidencia.toDto())
        } catch (_: Exception) {
        }
    }

    // Endpoint 4 (PUT /incidencias/{id}): actualiza gestión en Room y en MockAPI
    suspend fun guardarGestion(incidencia: IncidenciaEntity, accion: TrazabilidadEntity) {
        database.withTransaction {
            incidenciaDao.actualizar(incidencia)
            incidenciaDao.insertarAccion(accion)
        }
        try {
            val idRemoto = resolverIdRemoto(incidencia.id)
            api.actualizarIncidencia(idRemoto, incidencia.toDto(idRemoto))
        } catch (_: Exception) {
        }
    }

    // Endpoint 5 (DELETE /incidencias/{id}): elimina el registro en MockAPI
    suspend fun eliminarIncidenciaRemota(id: String): Boolean {
        incidenciaDao.eliminarPorId(id)
        return try {
            val idRemoto = resolverIdRemoto(id)
            val respuesta = api.eliminarIncidencia(idRemoto)
            respuesta.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    // Endpoint 6 (GET /sectores): obtiene los sectores disponibles desde MockAPI
    suspend fun obtenerSectoresDesdeApi(): List<String> {
        return try {
            api.getSectores()
                .map { it.nombre }
                .filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Endpoint 7 (POST /sectores): registra un sector nuevo en MockAPI
    suspend fun crearSectorEnApi(nombreSector: String): Boolean {
        return try {
            api.crearSector(SectorDto(nombre = nombreSector))
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Busca el ID autogenerado por MockAPI ("1", "2", etc.) a partir del código local ("SJ-..."). */
    private suspend fun resolverIdRemoto(codigoLocal: String): String {
        val listaRemota = api.getIncidencias()
        return listaRemota.find { it.codigo == codigoLocal || it.id == codigoLocal }?.id ?: codigoLocal
    }

    private fun IncidenciaEntity.toDto(idRemoto: String? = null): IncidenciaDto = IncidenciaDto(
        id = idRemoto,
        codigo = id,
        categoria = categoria,
        sectorReferencial = sectorReferencial,
        descripcion = descripcion,
        fechaIngreso = fechaIngreso,
        estado = estado,
        prioridad = prioridad,
        unidadAsignada = unidadAsignada
    )

    private fun IncidenciaDto.toEntity(): IncidenciaEntity = IncidenciaEntity(
        id = codigo.ifBlank { id ?: "SJ-${System.currentTimeMillis()}" },
        categoria = categoria,
        sectorReferencial = sectorReferencial,
        descripcion = descripcion,
        fechaIngreso = fechaIngreso.ifBlank { formatearFecha() },
        estado = estado,
        prioridad = prioridad,
        unidadAsignada = unidadAsignada,
        creadoEn = System.currentTimeMillis()
    )

    /** Carga dos casos totalmente sintéticos solo cuando la base de datos está vacía. */
    suspend fun insertarDatosDeDemostracionSiEsNecesario() {
        database.withTransaction {
            if (incidenciaDao.contarIncidencias() > 0) return@withTransaction

            val ahora = System.currentTimeMillis()
            val casoDerivado = IncidenciaEntity(
                id = "SJ-DEMO-1001",
                categoria = "Luminaria apagada",
                sectorReferencial = "Sector simulado 01",
                descripcion = "Caso ficticio: se informa alumbrado apagado en un espacio comunitario de prueba.",
                fechaIngreso = formatearFecha(ahora - 3_600_000L),
                estado = "DERIVADO",
                prioridad = "ALTA",
                unidadAsignada = "Equipo municipal de prueba",
                creadoEn = ahora - 3_600_000L
            )
            val casoEnRevision = IncidenciaEntity(
                id = "SJ-DEMO-1002",
                categoria = "Espacio público",
                sectorReferencial = "Sector simulado 02",
                descripcion = "Caso ficticio: revisar una condición simulada en un espacio público.",
                fechaIngreso = formatearFecha(ahora - 1_800_000L),
                estado = "EN_REVISION",
                prioridad = "MEDIA",
                unidadAsignada = "Sin asignar",
                creadoEn = ahora - 1_800_000L
            )

            incidenciaDao.insertar(casoDerivado)
            incidenciaDao.insertarAccion(
                TrazabilidadEntity(
                    incidenciaId = casoDerivado.id,
                    marcaTiempo = casoDerivado.fechaIngreso,
                    autorRol = "Vecino / Residente (demo)",
                    detalleAccion = "Registro inicial ficticio.",
                    creadoEn = ahora - 3_600_000L
                )
            )
            incidenciaDao.insertarAccion(
                TrazabilidadEntity(
                    incidenciaId = casoDerivado.id,
                    marcaTiempo = formatearFecha(ahora - 1_200_000L),
                    autorRol = "Operador Municipal (demo)",
                    detalleAccion = "Caso de demostración derivado al equipo de prueba.",
                    creadoEn = ahora - 1_200_000L
                )
            )

            incidenciaDao.insertar(casoEnRevision)
            incidenciaDao.insertarAccion(
                TrazabilidadEntity(
                    incidenciaId = casoEnRevision.id,
                    marcaTiempo = casoEnRevision.fechaIngreso,
                    autorRol = "Vecino / Residente (demo)",
                    detalleAccion = "Registro inicial ficticio; pendiente de revisión.",
                    creadoEn = ahora - 1_800_000L
                )
            )
        }
    }

    companion object {
        fun formatearFecha(milisegundos: Long = System.currentTimeMillis()): String =
            SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(milisegundos))
    }
}