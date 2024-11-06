package com.aditya.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var personDao: PersonDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var spinnerPerson: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbarAddTransaction: Toolbar = findViewById(R.id.toolbar_add_transaction)
        setSupportActionBar(toolbarAddTransaction)


        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true) // Enable the back button
            setHomeAsUpIndicator(R.drawable.ic_back_icon) // Replace with the appropriate drawable
        }

        personDao = DatabaseProvider.getDatabase(this).personDao()
        transactionDao = DatabaseProvider.getDatabase(this).transactionDao()
        spinnerPerson = findViewById(R.id.spinnerPerson)

        val buttonAddNewPerson: Button = findViewById(R.id.buttonAddNewPerson)
        val editTextName: EditText = findViewById(R.id.editTextName)
        val editTextAmount: EditText = findViewById(R.id.editTextAmount)
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        val buttonSave: Button = findViewById(R.id.buttonSave)

        buttonAddNewPerson.setOnClickListener {
            val name = editTextName.text.toString().trim()
            if (name.isNotEmpty()) {
                lifecycleScope.launch {
                    val count = personDao.getPersonCountByName(name)
                    if (count > 0) {
                        runOnUiThread {
                            Toast.makeText(this@AddTransactionActivity, "Person already exists", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        personDao.insert(Person(name = name))
                        updateSpinner()
                        runOnUiThread {
                            Toast.makeText(this@AddTransactionActivity, "Person added successfully", Toast.LENGTH_SHORT).show()
                            editTextName.text.clear()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        buttonSave.setOnClickListener {
            val selectedPerson = spinnerPerson.selectedItem?.toString()
            val transactionType = when (radioGroup.checkedRadioButtonId) {
                R.id.radioBorrowed -> "borrowed"
                R.id.radioLent -> "lent"
                else -> null
            }
            val amount = editTextAmount.text.toString().toDoubleOrNull()

            if (selectedPerson != null && !transactionType.isNullOrEmpty() && amount != null) {
                val currentDate = Date()
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(currentDate)

                lifecycleScope.launch {
                    transactionDao.insert(
                        Transaction(
                            name = selectedPerson,
                            amount = amount,
                            transactionType = transactionType,
                            date = formattedDate
                        )
                    )
                    runOnUiThread {
                        Toast.makeText(this@AddTransactionActivity, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after saving.
                    }
                }
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        updateSpinner()
    }

    private fun updateSpinner() {
        lifecycleScope.launch {
            val persons = personDao.getAllPersons()
            val adapter = ArrayAdapter(this@AddTransactionActivity, android.R.layout.simple_spinner_item, persons.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            runOnUiThread {
                spinnerPerson.adapter = adapter
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish() // Close this activity and return to MainActivity
        return true
    }
}