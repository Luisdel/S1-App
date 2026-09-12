package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.Employee
import com.example.data.model.PerformanceReview
import com.example.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    reviews: List<PerformanceReview>,
    employees: List<Employee>,
    canReview: Boolean,
    onAddReview: (PerformanceReview) -> Unit,
    onDeleteReview: (PerformanceReview) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("performance_screen"),
        floatingActionButton = {
            if (canReview) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    text = { Text("Nueva Evaluación") },
                    containerColor = AmberWarning,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_review_fab")
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Gestión del Rendimiento Laboral",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Supervisión de KPIs, cumplimiento de objetivos y evaluaciones periódicas de la plantilla.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }

            if (reviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Grade,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Aún no se han registrado evaluaciones de desempeño",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(reviews, key = { it.id }) { review ->
                    PerformanceCard(
                        review = review,
                        canDelete = canReview,
                        onDelete = { onDeleteReview(review) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddReviewDialog(
            employees = employees,
            onDismiss = { showAddDialog = false },
            onSave = { rev ->
                onAddReview(rev)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PerformanceCard(
    review: PerformanceReview,
    canDelete: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("review_card_${review.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = review.employeeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Evaluado el ${review.date} por ${review.reviewerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AmberWarning.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", review.overallRating),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RatingItem("Puntualidad", review.punctualityRating)
                RatingItem("Productividad", review.productivityRating)
                RatingItem("Equipo", review.teamworkRating)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TealAccent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Objetivos: ${review.goalsAchieved} de ${review.totalGoals} cumplidos (${if (review.totalGoals > 0) ((review.goalsAchieved.toFloat() / review.totalGoals) * 100).toInt() else 100}%)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (review.feedback.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"${review.feedback}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canDelete) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingItem(label: String, score: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "${score}/5.0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewDialog(
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onSave: (PerformanceReview) -> Unit
) {
    var selectedEmployee by remember { mutableStateOf(employees.firstOrNull()) }
    var overallRating by remember { mutableFloatStateOf(4.5f) }
    var punctuality by remember { mutableFloatStateOf(4.5f) }
    var productivity by remember { mutableFloatStateOf(4.5f) }
    var teamwork by remember { mutableFloatStateOf(4.5f) }
    var goalsAchieved by remember { mutableStateOf("5") }
    var totalGoals by remember { mutableStateOf("5") }
    var feedback by remember { mutableStateOf("") }
    var reviewerName by remember { mutableStateOf("Supervisor de RRHH") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Evaluación de Desempeño") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Empleado:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    employees.forEach { emp ->
                        FilterChip(
                            selected = selectedEmployee?.id == emp.id,
                            onClick = { selectedEmployee = emp },
                            label = { Text(emp.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Puntuación General: ${String.format("%.1f", overallRating)} ★", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = overallRating,
                    onValueChange = { overallRating = it },
                    valueRange = 1f..5f,
                    steps = 7
                )

                Text("Puntualidad: ${String.format("%.1f", punctuality)}", style = MaterialTheme.typography.labelSmall)
                Slider(value = punctuality, onValueChange = { punctuality = it }, valueRange = 1f..5f, steps = 7)

                Text("Productividad: ${String.format("%.1f", productivity)}", style = MaterialTheme.typography.labelSmall)
                Slider(value = productivity, onValueChange = { productivity = it }, valueRange = 1f..5f, steps = 7)

                Text("Trabajo en Equipo: ${String.format("%.1f", teamwork)}", style = MaterialTheme.typography.labelSmall)
                Slider(value = teamwork, onValueChange = { teamwork = it }, valueRange = 1f..5f, steps = 7)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = goalsAchieved,
                        onValueChange = { goalsAchieved = it },
                        label = { Text("Objetivos Logrados") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalGoals,
                        onValueChange = { totalGoals = it },
                        label = { Text("Total Objetivos") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Comentarios y Feedback *") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = { reviewerName = it },
                    label = { Text("Nombre del Evaluador") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedEmployee != null) {
                        onSave(
                            PerformanceReview(
                                employeeId = selectedEmployee!!.id,
                                employeeName = selectedEmployee!!.name,
                                date = LocalDate.now().toString(),
                                overallRating = overallRating,
                                punctualityRating = punctuality,
                                productivityRating = productivity,
                                teamworkRating = teamwork,
                                goalsAchieved = goalsAchieved.toIntOrNull() ?: 5,
                                totalGoals = totalGoals.toIntOrNull() ?: 5,
                                feedback = feedback.trim(),
                                reviewerName = reviewerName.trim()
                            )
                        )
                    }
                },
                enabled = selectedEmployee != null
            ) {
                Text("Guardar Evaluación")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
