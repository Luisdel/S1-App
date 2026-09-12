package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RolePermissions
import com.example.data.model.SystemRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.IntegrationsConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    currentRole: SystemRole,
    currentUserName: String,
    adminPermissions: RolePermissions,
    managerPermissions: RolePermissions,
    employeePermissions: RolePermissions,
    integrationsConfig: IntegrationsConfig,
    onRoleChange: (SystemRole) -> Unit,
    onUpdateIntegrations: (IntegrationsConfig) -> Unit,
    onTestSlack: () -> Unit,
    onTestEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    var slackUrl by remember { mutableStateOf(integrationsConfig.slackWebhookUrl) }
    var slackChannel by remember { mutableStateOf(integrationsConfig.slackChannel) }
    var emailRecipient by remember { mutableStateOf(integrationsConfig.emailRecipient) }
    var autoNotifyTimeOff by remember { mutableStateOf(integrationsConfig.autoNotifyOnTimeOff) }
    var autoNotifyApproval by remember { mutableStateOf(integrationsConfig.autoNotifyOnApproval) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("admin_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active session / Role switch card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = PrimaryBlueLight,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Panel de Administración y Seguridad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Control de Acceso Basado en Roles (RBAC) y Conexiones",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sesión Activa Actual: $currentUserName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cambia de rol para verificar cómo se aplican los permisos en la aplicación:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SystemRole.values().forEach { role ->
                        FilterChip(
                            selected = currentRole == role,
                            onClick = { onRoleChange(role) },
                            label = { Text(role.name) },
                            leadingIcon = if (currentRole == role) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Permissions Matrix Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Matriz de Permisos Personalizados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nivel de acceso actual para ${currentRole.label}:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                val perms = when (currentRole) {
                    SystemRole.ADMIN -> adminPermissions
                    SystemRole.MANAGER -> managerPermissions
                    SystemRole.EMPLOYEE -> employeePermissions
                }

                PermissionRow("Gestionar Empleados (Altas, Bajas y Edición)", perms.canManageEmployees)
                PermissionRow("Asignar y Modificar Turnos de Trabajo", perms.canAssignShifts)
                PermissionRow("Aprobar y Denegar Vacaciones / Ausencias", perms.canApproveTimeOff)
                PermissionRow("Crear y Asignar Tareas Diarias", perms.canAssignTasks)
                PermissionRow("Registrar Evaluaciones de Desempeño", perms.canReviewPerformance)
                PermissionRow("Generar y Exportar Informes PDF y Excel", perms.canExportReports)
                PermissionRow("Configurar Integraciones y Parámetros del Sistema", perms.canConfigSystem)
            }
        }

        // Integrations Configuration (Slack & Email)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = TealAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Integraciones de Comunicación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Configura canales para automatizar alertas cuando se soliciten o aprueben vacaciones y cambios de turno.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Slack Webhook
                OutlinedTextField(
                    value = slackUrl,
                    onValueChange = { slackUrl = it },
                    label = { Text("Webhook URL de Slack") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://hooks.slack.com/services/...") },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = slackChannel,
                    onValueChange = { slackChannel = it },
                    label = { Text("Canal de Slack") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    singleLine = true
                )

                // Email Recipient
                OutlinedTextField(
                    value = emailRecipient,
                    onValueChange = { emailRecipient = it },
                    label = { Text("Correo Electrónico de Recursos Humanos") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notificar solicitudes de tiempo libre", style = MaterialTheme.typography.bodyMedium)
                        Text("Envía aviso automático al canal Slack y correo de RRHH", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoNotifyTimeOff,
                        onCheckedChange = { autoNotifyTimeOff = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notificar al aprobar / denegar", style = MaterialTheme.typography.bodyMedium)
                        Text("Informa al empleado y supervisores de la resolución", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoNotifyApproval,
                        onCheckedChange = { autoNotifyApproval = it }
                    )
                }

                Button(
                    onClick = {
                        onUpdateIntegrations(
                            IntegrationsConfig(
                                slackWebhookUrl = slackUrl.trim(),
                                slackChannel = slackChannel.trim(),
                                emailRecipient = emailRecipient.trim(),
                                autoNotifyOnTimeOff = autoNotifyTimeOff,
                                autoNotifyOnApproval = autoNotifyApproval
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Configuración de Integraciones")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Pruebas de Conectividad:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTestSlack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar Slack")
                    }
                    OutlinedButton(
                        onClick = onTestEmail,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar Correo")
                    }
                }
            }
        }

        // Security & Compliance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Estándares de Seguridad y Conformidad",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                SecurityBadge("Cifrado Local y Almacenamiento Seguro (Room SQLite)")
                SecurityBadge("Acceso Restringido por Roles (RBAC - Admin, Manager, Operador)")
                SecurityBadge("Auditoría de Alertas y Trazabilidad de Aprobaciones")
                SecurityBadge("Generación de Archivos en Sandboxing (FileProvider)")
                SecurityBadge("Compatibilidad Multiplataforma (Móvil y Escritorio / Tablet)")
            }
        }
    }
}

@Composable
private fun PermissionRow(name: String, granted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (granted) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SecurityBadge(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = EmeraldSuccess,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
