package com.guc.gucfood

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.guc.gucfood.databinding.ActivityHomeBinding


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        val currentUser = auth.currentUser
        if (currentUser == null) {
            goToLogin()
            return
        }


        userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(currentUser.uid)

        loadProfile()

        binding.buttonSave.setOnClickListener { saveProfile() }

        binding.buttonLogout.setOnClickListener {
            auth.signOut()
            goToLogin()
        }
    }


    private fun loadProfile() {
        userRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Toast.makeText(this, "Profile not found in database", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""

                val role = snapshot.child("role").getValue(String::class.java) ?: "not set"

                binding.textWelcome.text = "Welcome, $name!"
                binding.textRole.text = "Role: ${role.replaceFirstChar { it.uppercase() }}"
                binding.textEmail.text = "Email: $email"
                binding.editTextName.setText(name)
                binding.editTextPhone.setText(phone)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun saveProfile() {
        val newName = binding.editTextName.text.toString().trim()
        val newPhone = binding.editTextPhone.text.toString().trim()

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Name and phone cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf<String, Any>(
            "name" to newName,
            "phone" to newPhone
        )

        binding.buttonSave.isEnabled = false
        userRef.updateChildren(updates)
            .addOnSuccessListener {
                binding.buttonSave.isEnabled = true
                binding.textWelcome.text = "Welcome, $newName!"
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                binding.buttonSave.isEnabled = true
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}