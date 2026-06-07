package com.app.apuntes.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.apuntes.domain.model.Horario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioScreen(navController: NavController) {
    val diasOrdenados = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    val horariosPorDia = SampleData.horarios.groupBy { it.dia }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi Horario") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(diasOrdenados.filter { horariosPorDia.containsKey(it) }) { dia ->
                HorarioDiaSection(
                    dia = dia,
                    horarios = horariosPorDia[dia] ?: emptyList()
                )
            }
        }
    }
}

@Composable
private fun HorarioDiaSection(dia: String, horarios: List<Horario>) {
    Column {
        Text(
            text = dia,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        horarios.forEach { horario ->
            val materia = SampleData.materias.find { it.id == horario.materiaId }
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = materia?.nombre ?: "Materia desconocida",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        horario.aula?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Text(
                        text = "${horario.horaInicio} - ${horario.horaFin}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
