package com.example.mobileassignment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var db: ContactDatabase
    private lateinit var contactDao: ContactDao
    private lateinit var adapter: ArrayAdapter<Contact>
    private var contacts = mutableListOf<Contact>()
    private var categories = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val name = findViewById<EditText>(R.id.name)
        val phone = findViewById<EditText>(R.id.phone)
        val category = findViewById<EditText>(R.id.category)
        val saveBtn = findViewById<Button>(R.id.savebtn)
        val showAllBtn = findViewById<Button>(R.id.showAllBtn)
        val filterBtn = findViewById<Button>(R.id.filterBtn)
        val listView = findViewById<ListView>(R.id.list)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)

        db = ContactDatabase.getInstance(this)
        contactDao = db.contactDao()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter


        lifecycleScope.launch(Dispatchers.IO) {
            val allContacts = contactDao.getAllContacts()
            categories = allContacts.map { it.category }.distinct()
            withContext(Dispatchers.Main) {
                val spinnerAdapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_item,
                    categories
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = spinnerAdapter
            }
        }

        saveBtn.setOnClickListener {
            val nameValue = name.text.toString().trim()
            val phoneValue = phone.text.toString().trim()
            val categoryValue = category.text.toString().trim()

            if (nameValue.isEmpty() || phoneValue.isEmpty() || categoryValue.isEmpty()) {
                Toast.makeText(this, "Please enter all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val contact = Contact(name = nameValue, phone = phoneValue, category = categoryValue)

            lifecycleScope.launch(Dispatchers.IO) {
                contactDao.insertContact(contact)
                contacts = contactDao.getAllContacts().toMutableList()
                categories = contacts.map { it.category }.distinct()

                withContext(Dispatchers.Main) {
                    val spinnerAdapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        categories
                    )
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = spinnerAdapter

                    Toast.makeText(this@MainActivity, "Saved!", Toast.LENGTH_SHORT).show()

                    name.text.clear()
                    phone.text.clear()
                    category.text.clear()
                }
            }
        }

        showAllBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                contacts = contactDao.getAllContacts().toMutableList()
                categories = contacts.map { it.category }.distinct()

                withContext(Dispatchers.Main) {
                    adapter.clear()
                    adapter.addAll(contacts)

                    val spinnerAdapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        categories
                    )
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = spinnerAdapter
                }
            }
        }

        filterBtn.setOnClickListener {
            val selectedCategory =
                spinnerCategory.selectedItem as? String ?: return@setOnClickListener
            lifecycleScope.launch(Dispatchers.IO) {
                val filtered = contactDao.getContactsByCategory(selectedCategory)
                withContext(Dispatchers.Main) {
                    adapter.clear()
                    adapter.addAll(filtered)
                }
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedContact = adapter.getItem(position)
            selectedContact?.let {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.phone}"))
                startActivity(intent)
            }
        }
    }
}
