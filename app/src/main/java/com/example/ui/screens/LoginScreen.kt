package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.CompanyEnvironment
import com.example.data.model.Employee
import com.example.data.model.SystemRole
import com.example.ui.theme.*

enum class AuthTab {
    LOGIN,
    CREATE_COMPANY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    companies: List<CompanyEnvironment> = emptyList(),
    currentCompanyCode: String = "S1-CORP",
    employees: List<Employee> = emptyList(),
    onSelectCompany: (String) -> Unit = {},
    onLoginWithCredentials: (companyCode: String, email: String, password: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onCreateCompany: (companyName: String, companyCode: String, adminName: String, adminEmail: String, adminPassword: String, adminPhone: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onGoogleSignIn: (email: String, name: String, companyCode: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onQuickLogin: (Employee) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(AuthTab.LOGIN) }

    // Form inputs - Login
    var loginCompanyCode by remember { mutableStateOf(currentCompanyCode) }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Form inputs - Crear Entorno Empresarial (Solo Administradores)
    var companyNameInput by remember { mutableStateOf("") }
    var companyCodeInput by remember { mutableStateOf("") }
    var adminNameInput by remember { mutableStateOf("") }
    var adminEmailInput by remember { mutableStateOf("") }
    var adminPasswordInput by remember { mutableStateOf("") }
    var showAdminPassword by remember { mutableStateOf(false) }
    var adminPhoneInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(currentCompanyCode) {
        if (loginCompanyCode.isBlank()) {
            loginCompanyCode = currentCompanyCode
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Logo Header
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S1",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "S1",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Control Horario, Gestión de Turnos y Personal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Info Card: Opacidad y Aislamiento Multi-Empresa
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Entorno Multi-Empresarial Seguro",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cada organización opera en un entorno privado y opaco. Los empleados acceden con el código de su empresa y las credenciales facilitadas por su Administrador.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Auth Tabs: Acceso a Empresa vs Nueva Empresa
            TabRow(
                selectedTabIndex = if (activeTab == AuthTab.LOGIN) 0 else 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = activeTab == AuthTab.LOGIN,
                    onClick = {
                        activeTab = AuthTab.LOGIN
                        errorMessage = null
                        infoMessage = null
                    },
                    text = {
                        Text(
                            text = "Acceso a Empresa",
                            fontWeight = if (activeTab == AuthTab.LOGIN) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == AuthTab.CREATE_COMPANY,
                    onClick = {
                        activeTab = AuthTab.CREATE_COMPANY
                        errorMessage = null
                        infoMessage = null
                    },
                    text = {
                        Text(
                            text = "Nueva Empresa",
                            fontWeight = if (activeTab == AuthTab.CREATE_COMPANY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alert Banners
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = infoMessage != null) {
                infoMessage?.let { msg ->
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 1: LOGIN (ACCESO A EMPRESA CON CÓDIGO Y CREDENCIALES)
            // =========================================================================
            if (activeTab == AuthTab.LOGIN) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Identificación de Personal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Introduce el código de tu empresa y tus credenciales asignadas:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Selector / Input de Código de Empresa
                        OutlinedTextField(
                            value = loginCompanyCode,
                            onValueChange = {
                                loginCompanyCode = it.uppercase()
                                onSelectCompany(it.uppercase())
                            },
                            label = { Text("Código de Empresa") },
                            placeholder = { Text("ej: S1-CORP") },
                            leadingIcon = {
                                Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryBlue)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_company_code_input")
                        )

                        // Chips de empresas guardadas en este dispositivo
                        if (companies.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Empresas:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                companies.forEach { comp ->
                                    val isSelected = loginCompanyCode.equals(comp.code, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            loginCompanyCode = comp.code
                                            onSelectCompany(comp.code)
                                        },
                                        label = {
                                            Text(
                                                text = comp.code,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Correo electrónico asignado") },
                            placeholder = { Text("tu.nombre@empresa.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                    Icon(
                                        imageVector = if (showLoginPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showLoginPassword) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            },
                            visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (loginCompanyCode.isBlank()) {
                                    errorMessage = "Por favor indica el código de empresa"
                                    return@Button
                                }
                                if (loginEmail.isBlank()) {
                                    errorMessage = "Introduce tu correo electrónico"
                                    return@Button
                                }
                                if (loginPassword.isBlank()) {
                                    errorMessage = "Introduce tu contraseña de acceso"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                infoMessage = null
                                onLoginWithCredentials(
                                    loginCompanyCode.trim().uppercase(),
                                    loginEmail.trim(),
                                    loginPassword.trim()
                                ) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        infoMessage = msg
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Entrar a ${loginCompanyCode.ifBlank { "la Empresa" }}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(14.dp))

                        // Google Sign In Button (validando que el admin haya dado de alta al empleado)
                        OutlinedButton(
                            onClick = { showGoogleDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_google_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Acceder con Cuenta de Google",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "¿Eres nuevo? El Administrador de tu empresa debe darte de alta previamente para poder ingresar.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 2: NUEVA EMPRESA (SOLO PARA ADMINISTRADORES)
            // =========================================================================
            if (activeTab == AuthTab.CREATE_COMPANY) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Registrar Nueva Organización",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Crea un entorno exclusivo para tu empresa. Como Administrador Maestro podrás registrar a tus empleados y asignarles sus claves de acceso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Nombre de la Empresa
                        OutlinedTextField(
                            value = companyNameInput,
                            onValueChange = { companyNameInput = it },
                            label = { Text("Nombre de la Empresa") },
                            placeholder = { Text("ej: Tech Logistics S.L.") },
                            leadingIcon = {
                                Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryBlue)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_company_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Código identificador de la Empresa
                        OutlinedTextField(
                            value = companyCodeInput,
                            onValueChange = { companyCodeInput = it.uppercase() },
                            label = { Text("Código de Empresa (Mín. 3 letras)") },
                            placeholder = { Text("ej: TLOG, ACME, CORP") },
                            leadingIcon = {
                                Icon(Icons.Default.Pin, contentDescription = null, tint = PrimaryBlue)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_company_code_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Datos del Administrador Maestro (Tú):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nombre Administrador
                        OutlinedTextField(
                            value = adminNameInput,
                            onValueChange = { adminNameInput = it },
                            label = { Text("Tu Nombre Completo") },
                            placeholder = { Text("ej: Carlos López") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_company_admin_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Email Administrador
                        OutlinedTextField(
                            value = adminEmailInput,
                            onValueChange = { adminEmailInput = it },
                            label = { Text("Tu Correo Electrónico (Login)") },
                            placeholder = { Text("carlos@empresa.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_company_admin_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password Administrador
                        OutlinedTextField(
                            value = adminPasswordInput,
                            onValueChange = { adminPasswordInput = it },
                            label = { Text("Tu Contraseña de Administrador") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showAdminPassword = !showAdminPassword }) {
                                    Icon(
                                        imageVector = if (showAdminPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (showAdminPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_company_admin_password_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Teléfono
                        OutlinedTextField(
                            value = adminPhoneInput,
                            onValueChange = { adminPhoneInput = it },
                            label = { Text("Teléfono de Contacto (Opcional)") },
                            placeholder = { Text("+34 600 000 000") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBlue)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (companyNameInput.isBlank()) {
                                    errorMessage = "Por favor indica el nombre de la empresa"
                                    return@Button
                                }
                                if (companyCodeInput.trim().length < 3) {
                                    errorMessage = "El código de empresa debe tener al menos 3 caracteres"
                                    return@Button
                                }
                                if (adminNameInput.isBlank()) {
                                    errorMessage = "Introduce tu nombre completo"
                                    return@Button
                                }
                                if (adminEmailInput.isBlank() || !adminEmailInput.contains("@")) {
                                    errorMessage = "Introduce un correo electrónico válido"
                                    return@Button
                                }
                                if (adminPasswordInput.trim().length < 4) {
                                    errorMessage = "La contraseña debe tener al menos 4 caracteres"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                infoMessage = null
                                onCreateCompany(
                                    companyNameInput.trim(),
                                    companyCodeInput.trim().uppercase(),
                                    adminNameInput.trim(),
                                    adminEmailInput.trim(),
                                    adminPasswordInput.trim(),
                                    adminPhoneInput.trim()
                                ) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        infoMessage = msg
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("create_company_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Crear Entorno y Entrar como Admin 👑",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // SECCIÓN: ACCESO RÁPIDO EN ESTE DISPOSITIVO (USUARIOS DE LA EMPRESA ACTIVA)
            // =========================================================================
            if (employees.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Acceso Rápido • ${loginCompanyCode.ifBlank { "Empresa Activa" }}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mostrar empleados de esta empresa
                    employees.take(4).forEach { emp ->
                        val isMaster = emp.isMasterAdmin
                        val roleTag = if (isMaster) "👑 Administrador Maestro" else emp.systemRole.label
                        val cardBg = when (emp.systemRole) {
                            SystemRole.ADMIN -> PrimaryBlue.copy(alpha = 0.08f)
                            SystemRole.MANAGER -> TealAccent.copy(alpha = 0.08f)
                            SystemRole.EMPLOYEE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onQuickLogin(emp) }
                                .testTag("quick_login_${emp.id}"),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(emp.avatarColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emp.name.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = emp.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = roleTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${emp.email} • Clave: ${emp.passwordHash}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = PrimaryBlue)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Google Sign-In Picker Dialog
    if (showGoogleDialog) {
        var customGoogleEmail by remember { mutableStateOf("") }
        var customGoogleName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showGoogleDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Acceso con Cuenta de Google")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selecciona o escribe el correo corporativo de Google con el que tu Administrador te registró en la empresa $loginCompanyCode:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Empleados de la empresa que tienen email
                    employees.take(2).forEach { emp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showGoogleDialog = false
                                    onGoogleSignIn(emp.email, emp.name, loginCompanyCode) { success, msg ->
                                        if (success) infoMessage = msg else errorMessage = msg
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(emp.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(emp.email, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    OutlinedTextField(
                        value = customGoogleName,
                        onValueChange = { customGoogleName = it },
                        label = { Text("Nombre") },
                        placeholder = { Text("Tu nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customGoogleEmail,
                        onValueChange = { customGoogleEmail = it },
                        label = { Text("Correo Google / Gmail") },
                        placeholder = { Text("tu.usuario@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customGoogleEmail.isNotBlank()) {
                            showGoogleDialog = false
                            onGoogleSignIn(
                                customGoogleEmail.trim(),
                                customGoogleName.trim().ifBlank { "Usuario Google" },
                                loginCompanyCode
                            ) { success, msg ->
                                if (success) infoMessage = msg else errorMessage = msg
                            }
                        }
                    },
                    enabled = customGoogleEmail.isNotBlank()
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
