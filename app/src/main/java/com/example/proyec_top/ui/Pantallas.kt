package com.example.proyec_top.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import com.example.proyec_top.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyec_top.model.*
import com.example.proyec_top.viewmodel.TopSecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(vm: TopSecurityViewModel) {
    val sesion by vm.sesionUsuario.collectAsState()
    var pantallaActual by remember { mutableStateOf("lista") }
    var casoSeleccionadoId by remember { mutableStateOf<String?>(null) }

    if (sesion == null) {
        LoginScreen(onIngresar = { alias, rol -> vm.guardarSesionLocal(alias, rol) })
    } else {
        val rolActivo = sesion!!.rol
        val incidencias by vm.incidencias.collectAsState()
        val filtro by vm.filtroEstado.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("TOP Security - San Joaquín", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Usuario: ${sesion!!.alias} (${rolActivo})", fontSize = 12.sp)
                        }
                    },
                    navigationIcon = {
                        if (pantallaActual != "lista") {
                            IconButton(onClick = { pantallaActual = "lista" }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    },
                    actions = {
                        TextButton(onClick = { vm.cerrarSesion() }) {
                            Text("Salir")
                        }
                    }
                )
            },
            floatingActionButton = {
                if (pantallaActual == "lista") {
                    FloatingActionButton(onClick = { pantallaActual = "registro" }) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo Caso")
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (pantallaActual) {
                    "lista" -> ListaIncidenciasScreen(
                        incidencias = incidencias,
                        filtro = filtro,
                        onFiltroSelected = { vm.aplicarFiltro(it) },
                        onCasoClick = { id ->
                            casoSeleccionadoId = id
                            pantallaActual = "detalle"
                        }
                    )
                    "registro" -> FormularioRegistroScreen(
                        onGuardar = { cat, sec, desc, foto ->
                            vm.registrarIncidencia(cat, sec, desc, foto)
                            pantallaActual = "lista"
                        }
                    )
                    "detalle" -> {
                        val caso = incidencias.find { it.id == casoSeleccionadoId }
                        if (caso != null) {
                            DetalleIncidenciaScreen(
                                caso = caso,
                                rolActual = rolActivo,
                                onGestionar = { est, prio, uni, obs ->
                                    vm.gestionarIncidencia(caso.id, est, prio, uni, obs, rolActivo)
                                    pantallaActual = "lista"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 1. LOGIN CON VALIDACIÓN
@Composable
fun LoginScreen(onIngresar: (String, String) -> Unit) {
    var alias by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolSeguridad.VECINO) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Imagen de TOP Security",
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("TOP Security - San Joaquín", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Gestión y seguimiento comunitario", style = MaterialTheme.typography.bodyMedium)
        Text("Prototipo académico con datos ficticios", style = MaterialTheme.typography.labelMedium)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it; error = null },
            label = { Text("Alias ficticio del usuario") },
            supportingText = { Text("No ingreses nombres reales, RUT ni datos personales.") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text("Seleccione Perfil para la demostración:", fontWeight = FontWeight.SemiBold)
        RolSeguridad.values().forEach { rol ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { rolSeleccionado = rol }
            ) {
                RadioButton(selected = rolSeleccionado == rol, onClick = { rolSeleccionado = rol })
                Text(rol.titulo)
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (alias.isBlank() || alias.length < 3) {
                    error = "Ingrese un alias válido (mínimo 3 letras)."
                } else {
                    onIngresar(alias.trim(), rolSeleccionado.titulo)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar al Sistema (Guardar en Room)")
        }
    }
}

// 2. LISTA CON FILTROS
@Composable
fun ListaIncidenciasScreen(
    incidencias: List<IncidenciaSeguridad>,
    filtro: EstadoIncidencia?,
    onFiltroSelected: (EstadoIncidencia?) -> Unit,
    onCasoClick: (String) -> Unit
) {
    val listaFiltrada = if (filtro == null) incidencias else incidencias.filter { it.estado == filtro }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filtro == null, onClick = { onFiltroSelected(null) }, label = { Text("Todos") })
            EstadoIncidencia.values().forEach { est ->
                FilterChip(selected = filtro == est, onClick = { onFiltroSelected(est) }, label = { Text(est.display) })
            }
        }

        Spacer(Modifier.height(12.dp))

        if (listaFiltrada.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No hay incidencias para mostrar", fontWeight = FontWeight.SemiBold)
                Text("Registra una incidencia ficticia para probar el flujo.", fontSize = 13.sp)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listaFiltrada) { caso ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onCasoClick(caso.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${caso.id} • ${caso.categoria}", fontWeight = FontWeight.Bold)
                            Text(caso.estado.display, fontWeight = FontWeight.Bold)
                        }
                        Text("Sector: ${caso.sectorReferencial}", fontSize = 13.sp)
                        Text(caso.descripcion, maxLines = 1, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// 3. FORMULARIO CON CÁMARA NATIVA
@Composable
fun FormularioRegistroScreen(onGuardar: (String, String, String, Bitmap?) -> Unit) {
    val categorias = listOf(
        "Luminaria apagada",
        "Espacio público",
        "Vandalismo (simulado)",
        "Ruido molesto (simulado)",
        "Otra situación"
    )
    var categoria by remember { mutableStateOf(categorias.first()) }
    var mostrarCategorias by remember { mutableStateOf(false) }
    var sector by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp -> if (bmp != null) fotoBitmap = bmp }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Registrar Nueva Incidencia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { mostrarCategorias = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Categoría: $categoria")
            }
            DropdownMenu(
                expanded = mostrarCategorias,
                onDismissRequest = { mostrarCategorias = false }
            ) {
                categorias.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            categoria = opcion
                            mostrarCategorias = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = sector,
            onValueChange = { sector = it; error = null },
            label = { Text("Sector ficticio / ubicación referencial") },
            placeholder = { Text("Ej.: Sector simulado 03") },
            supportingText = { Text("No ingreses domicilios reales ni datos sensibles.") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it; error = null },
            label = { Text("Descripción de los antecedentes") },
            placeholder = { Text("Describe una situación completamente ficticia.") },
            supportingText = { Text("No incluyas nombres, teléfonos ni información real.") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { cameraLauncher.launch(null) }) {
            Text("Capturar Evidencia (Cámara Nativa)")
        }

        fotoBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Foto capturada", modifier = Modifier.size(100.dp))
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }

        Button(
            onClick = {
                when {
                    sector.trim().length < 3 -> error = "Ingrese un sector referencial ficticio de al menos 3 caracteres."
                    descripcion.trim().length < 8 -> error = "La descripción debe contener al menos 8 caracteres."
                    else -> onGuardar(categoria, sector.trim(), descripcion.trim(), fotoBitmap)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Reporte")
        }
    }
}

// 4. DETALLE CON ANIMACIÓN Y MODAL DE CIERRE
@Composable
fun DetalleIncidenciaScreen(
    caso: IncidenciaSeguridad,
    rolActual: String,
    onGestionar: (EstadoIncidencia, NivelPrioridad, String, String) -> Unit
) {
    var mostrarModalCierre by remember { mutableStateOf(false) }
    var estadoSel by remember { mutableStateOf(caso.estado) }
    var prioridadSel by remember { mutableStateOf(caso.prioridad) }
    var mostrarEstados by remember { mutableStateOf(false) }
    var mostrarPrioridades by remember { mutableStateOf(false) }
    var unidad by remember { mutableStateOf(caso.unidadAsignada) }
    var observacion by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Caso #${caso.id}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Categoría: ${caso.categoria}")
        Text("Sector: ${caso.sectorReferencial}")
        Text("Estado actual: ${caso.estado.display}")
        Text("Prioridad: ${caso.prioridad.nivel}")
        Text("Responsable: ${caso.unidadAsignada}")
        Text("Fecha de ingreso: ${caso.fechaIngreso}")
        Text("Descripción: ${caso.descripcion}")

        Spacer(Modifier.height(16.dp))
        Text("Historial de Trazabilidad:", fontWeight = FontWeight.Bold)
        caso.trazabilidad.forEach { t ->
            Text("• ${t.marcaTiempo} [${t.autorRol}]: ${t.detalleAccion}", fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(
            visible = rolActual != "Vecino / Residente" && caso.estado != EstadoIncidencia.CERRADO,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider()
                Text("Gestión Municipal (${rolActual})", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = unidad,
                    onValueChange = { unidad = it },
                    label = { Text("Asignar Unidad / Responsable") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { mostrarEstados = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Estado: ${estadoSel.display}")
                        }
                        DropdownMenu(
                            expanded = mostrarEstados,
                            onDismissRequest = { mostrarEstados = false }
                        ) {
                            EstadoIncidencia.values()
                                .filter { it != EstadoIncidencia.CERRADO }
                                .forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion.display) },
                                        onClick = {
                                            estadoSel = opcion
                                            mostrarEstados = false
                                        }
                                    )
                                }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { mostrarPrioridades = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Prioridad: ${prioridadSel.nivel}")
                        }
                        DropdownMenu(
                            expanded = mostrarPrioridades,
                            onDismissRequest = { mostrarPrioridades = false }
                        ) {
                            NivelPrioridad.values().forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion.nivel) },
                                    onClick = {
                                        prioridadSel = opcion
                                        mostrarPrioridades = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onGestionar(estadoSel, prioridadSel, unidad, observacion)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar avance")
                    }

                    Button(
                        onClick = { mostrarModalCierre = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar caso")
                    }
                }
            }
        }

        if (mostrarModalCierre) {
            AlertDialog(
                onDismissRequest = { mostrarModalCierre = false },
                title = { Text("Confirmar Cierre de Caso") },
                text = { Text("¿Desea dar por resuelto este requerimiento y registrar el cierre en la trazabilidad?") },
                confirmButton = {
                    Button(onClick = {
                        onGestionar(EstadoIncidencia.CERRADO, prioridadSel, unidad, "Cierre de caso finalizado.")
                        mostrarModalCierre = false
                    }) {
                        Text("Confirmar Cierre")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarModalCierre = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}