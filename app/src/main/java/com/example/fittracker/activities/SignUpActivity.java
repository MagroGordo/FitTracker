package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittracker.R;
import com.example.fittracker.core.Prefs;
import com.example.fittracker.database.entities.User;
import com.example.fittracker.database.repositories.UserRepository;
import com.example.fittracker.database.repositories.GoalRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {

    private EditText inputFullName, inputEmail, inputPassword, inputBirthday, inputPeso, inputAltura;
    private Spinner spinnerSexo;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private UserRepository userRepository;
    private GoalRepository goalRepository;
    private ExecutorService executor;

    private Calendar selectedDob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userRepository = new UserRepository(getApplicationContext());
        goalRepository = new GoalRepository(getApplicationContext());
        executor = Executors.newSingleThreadExecutor();

        inputFullName = findViewById(R.id.inputFullName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputBirthday = findViewById(R.id.inputBirthday);
        inputPeso = findViewById(R.id.inputPeso);
        inputAltura = findViewById(R.id.inputAltura);
        spinnerSexo = findViewById(R.id.spinnerSexo);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Male", "Female"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSexo.setAdapter(adapter);

        inputBirthday.setOnClickListener(v -> showBirthdayPicker());

        findViewById(R.id.txtEnter).setOnClickListener(v -> {
            Intent i = new Intent(this, LogInActivity.class);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btnSignUp).setOnClickListener(v -> trySignUp());
    }

    private void showBirthdayPicker() {
        final Calendar cal = selectedDob != null ? (Calendar) selectedDob.clone() : Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    selectedDob = c;
                    inputBirthday.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year));
                },
                y, m, d
        ).show();
    }

    private void trySignUp() {
        String name = inputFullName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String gender = spinnerSexo.getSelectedItem().toString();
        String pesoStr = inputPeso.getText().toString().trim();
        String alturaStr = inputAltura.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            inputFullName.setError("Nome obrigatório");
            inputFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Email obrigatório");
            inputEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Email inválido");
            inputEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Password obrigatória");
            inputPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            inputPassword.setError("A password deve ter pelo menos 6 caracteres");
            inputPassword.requestFocus();
            return;
        }
        if (selectedDob == null) {
            inputBirthday.setError("Data de nascimento obrigatória");
            inputBirthday.requestFocus();
            return;
        }

        Calendar hoje = Calendar.getInstance();
        Calendar data14AnosAtras = Calendar.getInstance();
        data14AnosAtras.add(Calendar.YEAR, -14);

        if (selectedDob.after(data14AnosAtras)) {
            inputBirthday.setError("Tem de ter pelo menos 14 anos para criar conta");
            inputBirthday.requestFocus();
            Toast.makeText(this, "Tem de ter pelo menos 14 anos para criar conta", Toast.LENGTH_LONG).show();
            return;
        }

        double altura, peso;
        try {
            altura = Double.parseDouble(alturaStr);
            if (altura <= 0 || altura > 300) {
                inputAltura.setError("Altura inválida");
                inputAltura.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            inputAltura.setError("Altura inválida");
            inputAltura.requestFocus();
            return;
        }

        try {
            peso = Double.parseDouble(pesoStr);
            if (peso <= 0 || peso > 500) {
                inputPeso.setError("Peso inválido");
                inputPeso.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            inputPeso.setError("Peso inválido");
            inputPeso.requestFocus();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(task -> {
                    FirebaseUser fbUser = auth.getCurrentUser();
                    if (fbUser == null) {
                        Toast.makeText(this, "Erro interno ao criar conta.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = fbUser.getUid();
                    Date birthday = selectedDob.getTime();

                    User user = new User();
                    user.setFirebaseUid(uid);
                    user.setName(name);
                    user.setEmail(email);
                    user.setGender(gender);
                    user.setBirthday(birthday);
                    user.setHeight(altura);
                    user.setWeight(peso);
                    user.setCreatedAt(new Date());
                    user.setSynced(true);

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("email", email);
                    data.put("gender", gender);
                    data.put("birthday", birthday);
                    data.put("height", altura);
                    data.put("weight", peso);
                    data.put("createdAt", new Date());

                    firestore.collection("users")
                            .document(uid)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                // Guardar utilizador localmente e criar objetivo
                                executor.execute(() -> {
                                    try {
                                        // Insere utilizador no Room
                                        long userId = userRepository.getUserByEmail(email) != null
                                                ? userRepository.getUserByEmail(email).getId()
                                                : 0;

                                        if (userId == 0) {
                                            userRepository.insertLocal(user);
                                            // Aguarda um pouco para garantir que foi inserido
                                            Thread.sleep(300);
                                            User inserted = userRepository.getUserByEmail(email);
                                            if (inserted != null) {
                                                userId = inserted.getId();
                                            }
                                        }

                                        // Cria objetivo aleatório
                                        if (userId > 0) {
                                            android.util.Log.d("SignUp", "🎯 A criar objetivo para utilizador ID: " + userId);
                                            goalRepository.createRandomGoalForUser(userId, uid);
                                            android.util.Log.d("SignUp", "✅ Objetivo criado com sucesso!");
                                        } else {
                                            android.util.Log.e("SignUp", "❌ Erro: userId inválido");
                                        }

                                    } catch (Exception e) {
                                        android.util.Log.e("SignUp", "❌ Erro ao criar objetivo", e);
                                    }
                                });

                                Prefs.setRememberMe(getApplicationContext(), false);
                                Toast.makeText(this, "Conta criada com sucesso! 🎯", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(this, DashboardActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(err -> {
                                Toast.makeText(this, "Erro ao guardar perfil: " + err.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(this, "Erro ao criar conta: " + err.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}