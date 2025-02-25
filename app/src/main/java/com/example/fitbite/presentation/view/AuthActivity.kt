package com.example.fitbite.presentation.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbite.R
import com.example.fitbite.data.model.UserData
import com.example.fitbite.data.repository.UserRepository
import com.example.fitbite.domain.usecase.GetUserDataUseCase
import com.example.fitbite.domain.usecase.SaveUserDataUseCase
import com.example.fitbite.presentation.viewmodel.AuthViewModel
import com.example.fitbite.presentation.viewmodel.AuthViewModelFactory
import com.example.fitbite.presentation.viewmodel.InfoUserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest


class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            SaveUserDataUseCase(UserRepository()),
            GetUserDataUseCase(UserRepository()),
            FirebaseAuth.getInstance() // Передаем FirebaseAuth
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        // Проверка, авторизован ли пользователь
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Если пользователь уже авторизован, сразу переходим на InfoUserActivity
            navigateToInfoUserActivity()
            return  // Прерываем выполнение метода, чтобы не показывать экран авторизации
        }

        //Инициазация полей
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val actionButton: Button = findViewById(R.id.actionButton)
        val switchTextView: TextView = findViewById(R.id.switchTextView)

        var isLoginMode = true

        // Обработчик для входа и регистрации
        actionButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val username = usernameEditText.text.toString()

            if (isLoginMode) {
                authViewModel.loginUser(email, password, {
                    updateUI(auth.currentUser) // Успешный вход
                    navigateToInfoUserActivity() // Переход на InfoUserActivity
                }, { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                })
            } else {
                authViewModel.registerUser(email, password, username, {
                    Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
                    updateUI(auth.currentUser)
                    navigateToInfoUserActivity() // Переход на InfoUserActivity
                }, { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                })
            }
        }

        // Переключение между режимами входа и регистрации
        switchTextView.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                actionButton.text = "Вход"
                switchTextView.text = "У Вас еще нет аккаунта? Зарегистрируйтесь"
                usernameEditText.visibility = EditText.GONE

            } else {
                actionButton.text = "Регистрация"
                switchTextView.text = "У Вас уже есть аккаунт? Войдите"
                usernameEditText.visibility = EditText.VISIBLE
            }
        }

    }

    //Обновляет видимость элементов в зависимости от того, авторизован ли пользователь
    private fun updateUI(user: FirebaseUser?) {
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
    }

    // Переход на InfoUserActivity
    private fun navigateToInfoUserActivity() {
        val intent = Intent(this, InfoUserActivity::class.java)
        startActivity(intent)
        finish() // Завершаем текущую активность, чтобы она не осталась в стеке
    }
}



