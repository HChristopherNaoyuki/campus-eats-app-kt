package com.example.campus_eats_app_kt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campus_eats_app_kt.data.MenuRepository
import com.example.campus_eats_app_kt.data.entity.MenuItemEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AddEditMenuViewModel(
    private val repository: MenuRepository,
    val vendorId: String,
    val itemId: Long?
) : ViewModel() {
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var price by mutableStateOf("")
    var stock by mutableStateOf("")
    var category by mutableStateOf("")
    var imageUrl by mutableStateOf("")

    init {
        if (itemId != null) {
            viewModelScope.launch {
                val item = repository.getMenuItemsByVendor(vendorId).firstOrNull()?.find { it.itemId == itemId }
                item?.let {
                    name = it.name
                    description = it.description
                    price = it.price.toString()
                    stock = it.stock.toString()
                    category = it.category
                    imageUrl = it.imageUrl ?: ""
                }
            }
        }
    }

    fun saveItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val item = MenuItemEntity(
                itemId = itemId ?: 0,
                vendorId = vendorId,
                name = name,
                description = description,
                price = price.toDoubleOrNull() ?: 0.0,
                stock = stock.toIntOrNull() ?: 0,
                category = category,
                imageUrl = imageUrl.ifBlank { null }
            )
            if (itemId == null) {
                repository.addMenuItem(item)
            } else {
                repository.updateMenuItem(item)
            }
            onSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMenuItemScreen(
    onBackClick: () -> Unit,
    viewModel: AddEditMenuViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.itemId == null) "Add Item" else "Edit Item") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveItem(onBackClick) }) {
                        Icon(Icons.Rounded.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = viewModel.name, onValueChange = { viewModel.name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.description, onValueChange = { viewModel.description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = viewModel.price, onValueChange = { viewModel.price = it }, label = { Text("Price (R)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = viewModel.stock, onValueChange = { viewModel.stock = it }, label = { Text("Stock") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            OutlinedTextField(value = viewModel.category, onValueChange = { viewModel.category = it }, label = { Text("Category (e.g., Burgers, Drinks)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.imageUrl, onValueChange = { viewModel.imageUrl = it }, label = { Text("Image URL (Optional)") }, modifier = Modifier.fillMaxWidth())
            
            Button(
                onClick = { viewModel.saveItem(onBackClick) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Save Item")
            }
        }
    }
}
