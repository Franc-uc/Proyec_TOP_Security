package com.example.proyec_top.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // 1. GET: Obtener todas las incidencias
    @GET("incidencias")
    suspend fun getIncidencias(): List<IncidenciaDto>

    // 2. GET: Obtener una incidencia por ID
    @GET("incidencias/{id}")
    suspend fun getIncidenciaPorId(@Path("id") id: String): IncidenciaDto

    // 3. POST: Registrar nueva incidencia
    @POST("incidencias")
    suspend fun crearIncidencia(@Body incidencia: IncidenciaDto): IncidenciaDto

    // 4. PUT: Actualizar gestión de una incidencia
    @PUT("incidencias/{id}")
    suspend fun actualizarIncidencia(
        @Path("id") id: String,
        @Body incidencia: IncidenciaDto
    ): IncidenciaDto

    // 5. DELETE: Eliminar una incidencia
    @DELETE("incidencias/{id}")
    suspend fun eliminarIncidencia(@Path("id") id: String): Response<Unit>

    // 6. GET: Obtener listado de sectores
    @GET("sectores")
    suspend fun getSectores(): List<SectorDto>

    // 7. POST: Registrar un nuevo sector
    @POST("sectores")
    suspend fun crearSector(@Body sector: SectorDto): SectorDto
}