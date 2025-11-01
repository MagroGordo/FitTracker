package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittracker.R;
import com.example.fittracker.core.Prefs;
import com.example.fittracker.database.entities.User;
import com.example.fittracker.database.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "LogInActivity";

    private TextView txtRegistar, txtEsqueceuPassword;
    private Button btnEntrar;
    private EditText inputEmail, inputPassword;
    private CheckBox rememberMe;

    private FirebaseAuth auth;
    private UserRepository userRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        auth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(getApplicationContext());

        // Bind UI
        txtRegistar = findViewById(R.id.txtRegister);
        txtEsqueceuPassword = findViewById(R.id.txtForgot);
        btnEntrar = findViewById(R.id.btnLogin);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        rememberMe = findViewById(R.id.rememberMe);

        // Registo
        txtRegistar.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Recuperação de password
        txtEsqueceuPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, RecoverPasswordActivity.class);
            startActivity(intent);
        });

        // Login
        btnEntrar.setOnClickListener(v -> doLogin());

        // ✅ VERIFICAR SESSÃO APENAS DEPOIS DE CONFIGURAR OS LISTENERS
        checkExistingSession();
    }

    private void checkExistingSession() {
        boolean remember = Prefs.isRememberMe(getApplicationContext());
        FirebaseUser current = auth.getCurrentUser();

        if (current != null && !remember) {
            // Existe sessão, mas o utilizador não marcou Remember Me: terminar sessão
            auth.signOut();
        } else if (current != null && remember) {
            // Sessão existe e Remember Me ativo: validar no servidor
            current.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && auth.getCurrentUser() != null) {
                    // Token válido: ir para Dashboard
                    goToDashboardSafe();
                } else {
                    // Token inválido: fazer logout
                    auth.signOut();
                    Prefs.setRememberMe(getApplicationContext(), false);
                    Toast.makeText(this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void doLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString();

        Log.d(TAG, "doLogin chamado: email=" + email);

        if (!isValidEmail(email)) {
            inputEmail.setError("Email inválido");
            inputEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            inputPassword.setError("Password deve ter pelo menos 6 caracteres");
            inputPassword.requestFocus();
            return;
        }

        setLoading(true);

        // Timeout defensivo
        mainHandler.postDelayed(() -> {
            if (!btnEntrar.isEnabled()) {
                setLoading(false);
                Toast.makeText(this, "Timeout - tenta novamente", Toast.LENGTH_SHORT).show();
            }
        }, 15000);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser fbUser = auth.getCurrentUser();
                    if (fbUser == null) {
                        Toast.makeText(this, "Falha na autenticação.", Toast.LENGTH_SHORT).show();
                        setLoading(false);
                        return;
                    }

                    Log.d(TAG, "Login bem-sucedido: " + fbUser.getUid());

                    // Guardar escolha de Remember Me
                    boolean remember = rememberMe != null && rememberMe.isChecked();
                    Prefs.setRememberMe(getApplicationContext(), remember);

                    // Sincronizar com Room
                    new Thread(() -> {
                        try {
                            User existing = userRepository.getUserByEmail(email);
                            if (existing != null) {
                                boolean changed = false;
                                if (existing.getFirebaseUid() == null || !existing.getFirebaseUid().equals(fbUser.getUid())) {
                                    existing.setFirebaseUid(fbUser.getUid());
                                    changed = true;
                                }
                                if (existing.getEmail() == null) {
                                    existing.setEmail(email);
                                    changed = true;
                                }
                                if (changed) userRepository.updateLocal(existing);
                            } else {
                                User shell = new User();
                                shell.setFirebaseUid(fbUser.getUid());
                                shell.setEmail(email);
                                shell.setName(fbUser.getDisplayName() != null ? fbUser.getDisplayName() : email);
                                shell.setCreatedAt(new Date());
                                shell.setSynced(true);
                                userRepository.insertLocal(shell);
                            }
                        } catch (Throwable t) {
                            Log.e(TAG, "Erro a sincronizar User em Room", t);
                        } finally {
                            runOnUiThread(() -> {
                                setLoading(false);
                                goToDashboardSafe();
                            });
                        }
                    }).start();
                })
                .addOnFailureListener(err -> {
                    Log.e(TAG, "Erro login Firebase", err);
                    Toast.makeText(this, "Erro: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }

    private void goToDashboardSafe() {
        Log.d(TAG, "goToDashboardSafe chamado");
        Intent intent = new Intent(LogInActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        btnEntrar.setEnabled(!loading);
        btnEntrar.setAlpha(loading ? 0.6f : 1f);
        inputEmail.setEnabled(!loading);
        inputPassword.setEnabled(!loading);
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}