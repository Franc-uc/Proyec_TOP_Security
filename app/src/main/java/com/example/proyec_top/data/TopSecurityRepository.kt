package com.example.proyec_top.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centraliza el acceso a Room. Las pantallas no ejecutan consultas SQL directamente.
 */
class TopSecurityRepository(private val database: AppDatabase) {
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

    suspend fun obtenerIncidencia(id: String): IncidenciaEntity? = incidenciaDao.obtenerPorId(id)

    suspend fun registrarIncidencia(incidencia: IncidenciaEntity, primeraAccion: TrazabilidadEntity) {
        database.withTransaction {
            incidenciaDao.insertar(incidencia)
            incidenciaDao.insertarAccion(primeraAccion)
        }
    }

    suspend fun guardarGestion(incidencia: IncidenciaEntity, accion: TrazabilidadEntity) {
        database.withTransaction {
            incidenciaDao.actualizar(incidencia)
            incidenciaDao.insertarAccion(accion)
        }
    }

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
