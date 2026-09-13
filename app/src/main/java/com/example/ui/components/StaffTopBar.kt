package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Employee
import com.example.data.model.SystemRole
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffTopBar(
    currentRole: SystemRole,
    currentUserName: String,
    unreadNotificationsCount: Int,
    loggedInEmployee: Employee? = null,
    onRoleChange: (SystemRole) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAdmin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier.testTag("staff_top_bar"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S1",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "S1",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = loggedInEmployee?.let { "${it.name} • ${it.email}" } ?: currentUserName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            val isAdminUser = loggedInEmployee?.systemRole == SystemRole.ADMIN || loggedInEmployee?.isMasterAdmin == true

            if (isAdminUser) {
                // Admin role view mode switcher
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (currentRole) {
                            SystemRole.ADMIN -> MaterialTheme.colorScheme.primaryContainer
                            SystemRole.EMPLOYEE -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .clickable { showRoleMenu = true }
                            .testTag("role_switcher_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (currentRole) {
                                    SystemRole.ADMIN -> Icons.Default.AdminPanelSettings
                                    SystemRole.EMPLOYEE -> Icons.Default.Badge
                                    else -> Icons.Default.AdminPanelSettings
                                },
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = when (currentRole) {
                                    SystemRole.ADMIN -> MaterialTheme.colorScheme.onPrimaryContainer
                                    SystemRole.EMPLOYEE -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentRole == SystemRole.ADMIN) {
                                    if (loggedInEmployee?.isMasterAdmin == true) "👑 Admin Maestro" else "Admin"
                                } else {
                                    "Modo Empleado"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (currentRole) {
                                    SystemRole.ADMIN -> MaterialTheme.colorScheme.onPrimaryContainer
                                    SystemRole.EMPLOYEE -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = when (currentRole) {
                                    SystemRole.ADMIN -> MaterialTheme.colorScheme.onPrimaryContainer
                                    SystemRole.EMPLOYEE -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Modo Administrador", fontWeight = FontWeight.Bold)
                                    Text("Control total de gestión y configuración", style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                if (currentRole == SystemRole.ADMIN) {
                                    Icon(Icons.Default.Check, contentDescription = "Activo", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                showRoleMenu = false
                                onRoleChange(SystemRole.ADMIN)
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Modo Empleado (Personal)", fontWeight = FontWeight.Bold)
                                    Text("Solicitar permisos o ver tus turnos propios", style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                            trailingIcon = {
                                if (currentRole == SystemRole.EMPLOYEE) {
                                    Icon(Icons.Default.Check, contentDescription = "Activo", tint = MaterialTheme.colorScheme.tertiary)
                                }
                            },
                            onClick = {
                                showRoleMenu = false
                                onRoleChange(SystemRole.EMPLOYEE)
                            }
                        )
                    }
                }
            } else {
                // Fixed role pill for non-admin users (employees/managers cannot switch roles)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (currentRole) {
                        SystemRole.MANAGER -> MaterialTheme.colorScheme.secondaryContainer
                        SystemRole.EMPLOYEE -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .testTag("fixed_role_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (currentRole) {
                                SystemRole.MANAGER -> Icons.Default.SupervisorAccount
                                SystemRole.EMPLOYEE -> Icons.Default.Badge
                                else -> Icons.Default.Badge
                            },
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = when (currentRole) {
                                SystemRole.MANAGER -> MaterialTheme.colorScheme.onSecondaryContainer
                                SystemRole.EMPLOYEE -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentRole) {
                                SystemRole.MANAGER -> "Manager"
                                SystemRole.EMPLOYEE -> "Empleado"
                                else -> currentRole.name
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (currentRole) {
                                SystemRole.MANAGER -> MaterialTheme.colorScheme.onSecondaryContainer
                                SystemRole.EMPLOYEE -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Notifications with badge
            IconButton(
                onClick = onOpenNotifications,
                modifier = Modifier.testTag("notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge {
                                Text(unreadNotificationsCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificaciones"
                    )
                }
            }

            // Quick access to Admin settings
            IconButton(
                onClick = onOpenAdmin,
                modifier = Modifier.testTag("admin_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración y Administración"
                )
            }

            // Logout / Switch User
            IconButton(
                onClick = onLogout,
                modifier = Modifier.testTag("logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Cerrar sesión o cambiar de cuenta"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
