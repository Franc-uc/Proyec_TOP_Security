package com.example.proyec_top.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tabla_sesion")
data class UsuarioSesionEntity(
    @PrimaryKey val id: Int = 1,
    val alias: String,
    val rol: String
)

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM tabla_sesion WHERE id = 1 LIMIT 1")
    fun obtenerSesionActiva(): Flow<UsuarioSesionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarSesion(usuario: UsuarioSesionEntity)

    @Query("DELETE FROM tabla_sesion")
    suspend fun eliminarSesion()
}

@Entity(tableName = "tabla_incidencias")
data class IncidenciaEntity(
    @PrimaryKey val id: String,
    val categoria: String,
    val sectorReferencial: String,
    val descripcion: String,
    val fechaIngreso: String,
    val estado: String,
    val prioridad: String,
    val unidadAsignada: String,
    val creadoEn: Long
)

@Entity(
    tableName = "tabla_trazabilidad",
    foreignKeys = [
        ForeignKey(
            entity = IncidenciaEntity::class,
            parentColumns = ["id"],
            childColumns = ["incidenciaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["incidenciaId"])]
)
data class TrazabilidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val incidenciaId: String,
    val marcaTiempo: String,
    val autorRol: String,
    val detalleAccion: String,
    val creadoEn: Long
)

@Dao
interface IncidenciaDao {
    @Query("SELECT * FROM tabla_incidencias ORDER BY creadoEn DESC")
    fun observarIncidencias(): Flow<List<IncidenciaEntity>>

    @Query("SELECT * FROM tabla_trazabilidad ORDER BY creadoEn ASC")
    fun observarTrazabilidad(): Flow<List<TrazabilidadEntity>>

    @Query("SELECT COUNT(*) FROM tabla_incidencias")
    suspend fun contarIncidencias(): Int

    @Query("SELECT * FROM tabla_incidencias WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): IncidenciaEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(incidencia: IncidenciaEntity)

    @Update
    suspend fun actualizar(incidencia: IncidenciaEntity)

    @Insert
    suspend fun insertarAccion(accion: TrazabilidadEntity)
}

@Database(
    entities = [UsuarioSesionEntity::class, IncidenciaEntity::class, TrazabilidadEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun incidenciaDao(): IncidenciaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        /** Mantiene una sola instancia de Room durante la vida de la aplicación. */
        fun obtenerInstancia(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "top_security_db"
                )
                    .addMigrations(MIGRACION_1_2)
                    .build()
                    .also { instancia = it }
            }

        /** Agrega las tablas nuevas sin eliminar la sesión local de la versión anterior. */
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tabla_incidencias` (
                        `id` TEXT NOT NULL,
                        `categoria` TEXT NOT NULL,
                        `sectorReferencial` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `fechaIngreso` TEXT NOT NULL,
                        `estado` TEXT NOT NULL,
                        `prioridad` TEXT NOT NULL,
                        `unidadAsignada` TEXT NOT NULL,
                        `creadoEn` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tabla_trazabilidad` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `incidenciaId` TEXT NOT NULL,
                        `marcaTiempo` TEXT NOT NULL,
                        `autorRol` TEXT NOT NULL,
                        `detalleAccion` TEXT NOT NULL,
                        `creadoEn` INTEGER NOT NULL,
                        FOREIGN KEY(`incidenciaId`) REFERENCES `tabla_incidencias`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tabla_trazabilidad_incidenciaId` " +
                        "ON `tabla_trazabilidad` (`incidenciaId`)"
                )
            }
        }
    }
}
