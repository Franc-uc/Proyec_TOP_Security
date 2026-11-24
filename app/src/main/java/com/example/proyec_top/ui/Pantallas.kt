package com.example.proyec_top.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyec_top.R
import com.example.proyec_top.model.*
import com.example.proyec_top.viewmodel.TopSecurityViewModel
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(vm: TopSecurityViewModel) {
    val sesion by vm.sesionUsuario.collectAsState()
    var pantallaActual by remember { mutableStateOf("lista") }
    var casoSeleccionadoId by remember { mutableStateOf<String?>(null) }
    var mostrarModalSalir by remember { mutableStateOf(false) }

    // Estados para la pantalla de carga y transición suave
    var cargandoSesion by remember { mutableStateOf(false) }
    var credencialesPendientes by remember { mutableStateOf<Pair<String, String>?>(null) }
    var ultimoUsuario by remember { mutableStateOf(sesion) }
    if (sesion != null) ultimoUsuario = sesion

    LaunchedEffect(cargandoSesion) {
        if (cargandoSesion && credencialesPendientes != null) {
            val (alias, rol) = credencialesPendientes!!
            delay(1500L) // Duración de la pantalla de carga (1.5 segundos)
            vm.guardarSesionLocal(alias, rol)
            cargandoSesion = false
            credencialesPendientes = null
        }
    }

    val vistaRaiz = when {
        cargandoSesion -> "carga"
        sesion == null -> "login"
        else -> "principal"
    }

    Crossfade(
        targetState = vistaRaiz,
        animationSpec = tween(durationMillis = 450),
        label = "TransicionSesion"
    ) { estado ->
        when (estado) {
            "carga" -> {
                PantallaCargaSesion(
                    alias = credencialesPendientes?.first ?: "",
                    rol = credencialesPendientes?.second ?: ""
                )
            }

            "login" -> {
                LoginScreen(
                    onIngresar = { alias, rol ->
                        credencialesPendientes = alias to rol
                        cargandoSesion = true
                    }
                )
            }

            "principal" -> {
                val usuarioActivo = sesion ?: ultimoUsuario ?: return@Crossfade
                val rolActivo = usuarioActivo.rol
                val incidencias by vm.incidencias.collectAsState()
                val filtro by vm.filtroEstado.collectAsState()
                val sectores by vm.sectores.collectAsState()

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = NavyMid,
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White
                            ),
                            title = {
                                Column {
                                    Text(
                                        "TOP Security • San Joaquín",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        "${usuarioActivo.alias} • $rolActivo",
                                        fontSize = 12.sp,
                                        color = Color(0xFF93C5FD)
                                    )
                                }
                            },
                            navigationIcon = {
                                if (pantallaActual != "lista") {
                                    IconButton(onClick = { pantallaActual = "lista" }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Volver"
                                        )
                                    }
                                }
                            },
                            actions = {
                                Surface(
                                    onClick = { mostrarModalSalir = true },
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White.copy(alpha = 0.14f),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        "Salir",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        )
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        if (pantallaActual == "lista") {
                            ExtendedFloatingActionButton(
                                onClick = { pantallaActual = "registro" },
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White,
                                icon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Nuevo Caso"
                                    )
                                },
                                text = { Text("Reportar", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        AnimatedContent(
                            targetState = pantallaActual,
                            transitionSpec = {
                                if (targetState != "lista") {
                                    (slideInHorizontally(animationSpec = tween(280)) { ancho -> ancho / 4 } +
                                            fadeIn(animationSpec = tween(280)))
                                        .togetherWith(
                                            slideOutHorizontally(animationSpec = tween(280)) { ancho -> -ancho / 4 } +
                                                    fadeOut(animationSpec = tween(220))
                                        )
                                } else {
                                    (slideInHorizontally(animationSpec = tween(280)) { ancho -> -ancho / 4 } +
                                            fadeIn(animationSpec = tween(280)))
                                        .togetherWith(
                                            slideOutHorizontally(animationSpec = tween(280)) { ancho -> ancho / 4 } +
                                                    fadeOut(animationSpec = tween(220))
                                        )
                                }
                            },
                            label = "TransicionIncidencias"
                        ) { pantalla ->
                            when (pantalla) {
                                "lista" -> ListaIncidenciasScreen(
                                    incidencias = incidencias,
                                    filtro = filtro,
                                    onFiltroSelected = { vm.aplicarFiltro(it) },
                                    onCasoClick = { id ->
                                        casoSeleccionadoId = id
                                        vm.refrescarIncidencia(id)
                                        pantallaActual = "detalle"
                                    }
                                )

                                "registro" -> FormularioRegistroScreen(
                                    sectoresDisponibles = sectores,
                                    onAgregarSector = { vm.agregarSector(it) },
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
                                                vm.gestionarIncidencia(
                                                    caso.id,
                                                    est,
                                                    prio,
                                                    uni,
                                                    obs,
                                                    rolActivo
                                                )
                                                pantallaActual = "lista"
                                            },
                                            onEliminar = {
                                                vm.eliminarIncidencia(caso.id)
                                                pantallaActual = "lista"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Advertencia de Salida al momento de cerrar sesión
                if (mostrarModalSalir) {
                    AlertDialog(
                        onDismissRequest = { mostrarModalSalir = false },
                        shape = RoundedCornerShape(20.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        title = {
                            Text(
                                text = "Cerrar Sesión",
                                fontWeight = FontWeight.Bold,
                                color = NavyMid
                            )
                        },
                        text = {
                            Text("¿Seguro que quieres Cerrar Sesión? (${usuarioActivo.alias})")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    mostrarModalSalir = false
                                    pantallaActual = "lista"
                                    vm.cerrarSesion()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarModalSalir = false }) {
                                Text("Cancelar", color = NavyMid, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            }
        }
    }
}
// 1. LOGIN REDISEÑADO
@Composable
fun LoginScreen(onIngresar: (String, String) -> Unit) {
    var alias by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolSeguridad.VECINO) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(NavyDark, NavyMid, Color(0xFF16466E))
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(top = 36.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Contenedor circular iluminado para tu imagen de referencia
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF091624))
                        .border(3.dp, CyanGlow, CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_top_security),
                        contentDescription = "Emblema TOP Security",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = CyanGlow.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "SISTEMA COMUNITARIO • SAN JOAQUÍN",
                        color = Color(0xFFE0F2FE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "TOP Security",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Gestión y seguimiento preventivo de incidencias",
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Tarjeta principal de acceso
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-18).dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Acceso al Prototipo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyMid
                )

                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it; error = null },
                    label = { Text("Alias ficticio del usuario") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    supportingText = { Text("No ingreses nombres reales, RUT ni datos personales.") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Seleccione Perfil de Demostración:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = NavyMid
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RolSeguridad.values().forEach { rol ->
                        val seleccionado = rolSeleccionado == rol
                        Surface(
                            onClick = { rolSeleccionado = rol },
                            shape = RoundedCornerShape(14.dp),
                            color = if (seleccionado) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.background
                            },
                            border = BorderStroke(
                                width = if (seleccionado) 2.dp else 1.dp,
                                color = if (seleccionado) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                RadioButton(
                                    selected = seleccionado,
                                    onClick = { rolSeleccionado = rol },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.secondary
                                    )
                                )
                                Text(
                                    text = rol.titulo,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                    color = NavyMid
                                )
                            }
                        }
                    }
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = {
                        if (alias.isBlank() || alias.length < 3) {
                            error = "Ingrese un alias válido (mínimo 3 letras)."
                        } else {
                            onIngresar(alias.trim(), rolSeleccionado.titulo)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyMid,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        "Ingresar al Sistema",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "Prototipo académico con almacenamiento local en Room y datos ficticios.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// 2. LISTA CON RESUMEN Y FILTROS
@Composable
fun ListaIncidenciasScreen(
    incidencias: List<IncidenciaSeguridad>,
    filtro: EstadoIncidencia?,
    onFiltroSelected: (EstadoIncidencia?) -> Unit,
    onCasoClick: (String) -> Unit
) {
    val listaFiltrada =
        if (filtro == null) incidencias else incidencias.filter { it.estado == filtro }
    val totalCerrados = incidencias.count { it.estado == EstadoIncidencia.CERRADO }
    val totalActivos = incidencias.size - totalCerrados

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResumenStatCard(
                titulo = "Total Casos",
                valor = incidencias.size.toString(),
                colorAcento = NavyMid,
                modifier = Modifier.weight(1f)
            )
            ResumenStatCard(
                titulo = "Activos",
                valor = totalActivos.toString(),
                colorAcento = AmberWarn,
                modifier = Modifier.weight(1f)
            )
            ResumenStatCard(
                titulo = "Cerrados",
                valor = totalCerrados.toString(),
                colorAcento = EmeraldOk,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filtro == null,
                onClick = { onFiltroSelected(null) },
                label = { Text("Todos") }
            )
            EstadoIncidencia.values().forEach { est ->
                FilterChip(
                    selected = filtro == est,
                    onClick = { onFiltroSelected(est) },
                    label = { Text(est.display) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (listaFiltrada.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No hay incidencias para mostrar",
                        fontWeight = FontWeight.Bold,
                        color = NavyMid
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Presiona '+ Reportar' para registrar un caso de prueba.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listaFiltrada) { caso ->
                val esCerrado = caso.estado == EstadoIncidencia.CERRADO
                val colorEstado = if (esCerrado) EmeraldOk else AmberWarn

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCasoClick(caso.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${caso.id} • ${caso.categoria}",
                                fontWeight = FontWeight.Bold,
                                color = NavyMid,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = colorEstado.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = caso.estado.display,
                                    color = colorEstado,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Sector: ${caso.sectorReferencial}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            caso.descripcion,
                            maxLines = 2,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenStatCard(
    titulo: String,
    valor: String,
    colorAcento: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorAcento
            )
            Text(
                text = titulo,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 3. FORMULARIO CON CÁMARA NATIVA
@Composable
fun FormularioRegistroScreen(
    sectoresDisponibles: List<String> = emptyList(),
    onAgregarSector: (String) -> Unit = {},
    onGuardar: (String, String, String, Bitmap?) -> Unit
) {
    val categorias = listOf(
        "Luminaria apagada",
        "Espacio público",
        "Vandalismo (simulado)",
        "Ruido molesto (simulado)",
        "Otra situación"
    )
    var categoria by remember { mutableStateOf(categorias.first()) }
    var mostrarCategorias by remember { mutableStateOf(false) }
    var mostrarSectores by remember { mutableStateOf(false) }
    var sector by remember { mutableStateOf("") }
    var avisoSector by remember { mutableStateOf<String?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp -> if (bmp != null) fotoBitmap = bmp }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Registrar Nueva Incidencia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NavyMid
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { mostrarCategorias = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Categoría: $categoria", fontWeight = FontWeight.SemiBold)
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

                // Selector conectado a Endpoint 6 (GET /sectores)
                if (sectoresDisponibles.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { mostrarSectores = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Elegir sector de API (${sectoresDisponibles.size} disponibles)",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        DropdownMenu(
                            expanded = mostrarSectores,
                            onDismissRequest = { mostrarSectores = false }
                        ) {
                            sectoresDisponibles.forEach { opcionSector ->
                                DropdownMenuItem(
                                    text = { Text(opcionSector) },
                                    onClick = {
                                        sector = opcionSector
                                        error = null
                                        mostrarSectores = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = sector,
                    onValueChange = { sector = it; error = null; avisoSector = null },
                    label = { Text("Sector ficticio / ubicación referencial") },
                    placeholder = { Text("Ej.: Sector simulado 03") },
                    supportingText = { Text("No ingreses domicilios reales ni datos sensibles.") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Botón conectado a Endpoint 7 (POST /sectores)
                if (sector.trim().length >= 3 && !sectoresDisponibles.contains(sector.trim())) {
                    OutlinedButton(
                        onClick = {
                            onAgregarSector(sector.trim())
                            avisoSector = "Sector '${sector.trim()}' enviado a MockAPI."
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Guardar sector en servidor")
                    }
                }

                avisoSector?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it; error = null },
                    label = { Text("Descripción de los antecedentes") },
                    placeholder = { Text("Describe una situación completamente ficticia.") },
                    supportingText = { Text("No incluyas nombres, teléfonos ni información real.") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        "Capturar Evidencia (Cámara Nativa)",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                fotoBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Foto capturada",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        when {
                            sector.trim().length < 3 -> error =
                                "Ingrese un sector referencial ficticio de al menos 3 caracteres."

                            descripcion.trim().length < 8 -> error =
                                "La descripción debe contener al menos 8 caracteres."

                            else -> onGuardar(
                                categoria,
                                sector.trim(),
                                descripcion.trim(),
                                fotoBitmap
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Reporte", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 4. DETALLE CON ANIMACIÓN Y MODAL DE CIERRE
@Composable
fun DetalleIncidenciaScreen(
    caso: IncidenciaSeguridad,
    rolActual: String,
    onGestionar: (EstadoIncidencia, NivelPrioridad, String, String) -> Unit,
    onEliminar: () -> Unit = {}
) {
    var mostrarModalCierre by remember { mutableStateOf(false) }
    var mostrarModalEliminar by remember { mutableStateOf(false) }
    var estadoSel by remember { mutableStateOf(caso.estado) }
    var prioridadSel by remember { mutableStateOf(caso.prioridad) }
    var mostrarEstados by remember { mutableStateOf(false) }
    var mostrarPrioridades by remember { mutableStateOf(false) }
    var unidad by remember { mutableStateOf(caso.unidadAsignada) }
    var observacion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Caso #${caso.id}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NavyMid
                )
                Text("Categoría: ${caso.categoria}", fontWeight = FontWeight.SemiBold)
                Text("Sector: ${caso.sectorReferencial}")
                Text("Estado actual: ${caso.estado.display}")
                Text("Prioridad: ${caso.prioridad.nivel}")
                Text("Responsable: ${caso.unidadAsignada}")
                Text("Fecha de ingreso: ${caso.fechaIngreso}")
                Spacer(Modifier.height(4.dp))
                Text("Descripción: ${caso.descripcion}")
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Historial de Trazabilidad", fontWeight = FontWeight.Bold, color = NavyMid)
                caso.trazabilidad.forEach { t ->
                    Text("• ${t.marcaTiempo} [${t.autorRol}]: ${t.detalleAccion}", fontSize = 12.sp)
                }
            }
        }

        AnimatedVisibility(
            visible = rolActual != "Vecino / Residente" && caso.estado != EstadoIncidencia.CERRADO,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Gestión Municipal ($rolActual)",
                        fontWeight = FontWeight.Bold,
                        color = NavyMid
                    )

                    OutlinedTextField(
                        value = unidad,
                        onValueChange = { unidad = it },
                        label = { Text("Asignar Unidad / Responsable") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { mostrarEstados = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Estado: ${estadoSel.display}", fontSize = 12.sp)
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
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Prioridad: ${prioridadSel.nivel}", fontSize = 12.sp)
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar avance")
                        }

                        Button(
                            onClick = { mostrarModalCierre = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cerrar caso")
                        }
                    }
                }
            }
        }

        // Botón conectado a Endpoint 5 (DELETE /incidencias/{id})
        OutlinedButton(
            onClick = { mostrarModalEliminar = true },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Eliminar registro (Servidor + Local)",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (mostrarModalCierre) {
            AlertDialog(
                onDismissRequest = { mostrarModalCierre = false },
                title = { Text("Confirmar Cierre de Caso") },
                text = { Text("¿Desea dar por resuelto este requerimiento y registrar el cierre en la trazabilidad?") },
                confirmButton = {
                    Button(onClick = {
                        onGestionar(
                            EstadoIncidencia.CERRADO,
                            prioridadSel,
                            unidad,
                            "Cierre de caso finalizado."
                        )
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

        if (mostrarModalEliminar) {
            AlertDialog(
                onDismissRequest = { mostrarModalEliminar = false },
                title = { Text("Eliminar Incidencia") },
                text = { Text("¿Desea eliminar este registro tanto de MockAPI como de la base de datos local?") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        onClick = {
                            mostrarModalEliminar = false
                            onEliminar()
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarModalEliminar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// 5. PANTALLA DE CARGA INICIO DE SESIÓN
@Composable
fun PantallaCargaSesion(alias: String, rol: String) {
    val transicionInfinita = rememberInfiniteTransition(label = "PulsoCarga")
    val escalaLogo by transicionInfinita.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "EscalaLogo"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(NavyDark, NavyMid, Color(0xFF16466E))
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = escalaLogo
                        scaleY = escalaLogo
                    }
                    .clip(CircleShape)
                    .background(Color(0xFF091624))
                    .border(3.dp, CyanGlow, CircleShape)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_top_security),
                    contentDescription = "Cargando TOP Security",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Iniciando Sesión...",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Perfil: $rol",
                color = CyanGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Usuario: $alias",
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(28.dp))

            LinearProgressIndicator(
                color = CyanGlow,
                trackColor = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .width(200.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}