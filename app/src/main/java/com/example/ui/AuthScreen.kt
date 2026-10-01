package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetSignInWithGoogleOption
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(com.example.R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId = clientId).build())
        .build()
    scope.launch {
        try {
            val activity = context as? Activity
            if (activity == null) {
                onAuthError("Activity context required for Google Sign-In")
                return@launch
            }
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else onAuthError("Unexpected credential type")
        } catch (_: GetCredentialCancellationException) {
            onAuthCancelled()
        } catch (e: Exception) {
            onAuthError(e.localizedMessage ?: "Google sign-in failed")
        }
    }
}

fun signOutUser(
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var createAccount by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun runAuth(block: suspend () -> Unit) {
        if (!acceptedTerms) {
            error = "Please acknowledge the Terms and Conditions before continuing."
            return
        }
        loading = true
        error = null
        scope.launch {
            try {
                block()
                onAuthSuccess()
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Sign-in failed"
            } finally {
                loading = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Welcome to Edam", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Sign in to sync your courses and learning progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (createAccount) "Create account" else "Sign in", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { createAccount = !createAccount; error = null }) {
                            Text(if (createAccount) "Have an account? Sign in" else "Create account")
                        }
                    }
                    OutlinedTextField(
                        value = email, onValueChange = { email = it }, label = { Text("Email") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !loading, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password, onValueChange = { password = it }, label = { Text("Password") },
                        singleLine = true, visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !loading, modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (email.isBlank() || password.length < 6) {
                                error = "Enter a valid email and a password with at least 6 characters."
                            } else runAuth {
                                if (createAccount) Firebase.auth.createUserWithEmailAndPassword(email.trim(), password).await()
                                else Firebase.auth.signInWithEmailAndPassword(email.trim(), password).await()
                            }
                        },
                        enabled = !loading && acceptedTerms,
                        modifier = Modifier.fillMaxWidth().testTag("email_sign_in_button")
                    ) {
                        Text(if (createAccount) "Create account with email" else "Sign in with email")
                    }

                    HorizontalDivider()
                    Text("Phone number", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it }, label = { Text("Phone (+country code)") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = !loading && verificationId == null, modifier = Modifier.fillMaxWidth()
                    )
                    if (verificationId == null) {
                        OutlinedButton(
                            onClick = {
                                if (!acceptedTerms) {
                                    error = "Please acknowledge the Terms and Conditions before continuing."
                                } else {
                                    val activity = context as? Activity
                                    if (activity == null || phone.isBlank()) {
                                        error = "Enter a phone number including its country code."
                                    } else {
                                        loading = true
                                        error = null
                                        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                                                scope.launch {
                                                    try {
                                                        Firebase.auth.signInWithCredential(credential).await()
                                                        onAuthSuccess()
                                                    } catch (e: Exception) {
                                                        error = e.localizedMessage ?: "Phone sign-in failed"
                                                    } finally { loading = false }
                                                }
                                            }
                                            override fun onVerificationFailed(e: Exception) {
                                                error = e.localizedMessage ?: "Phone verification failed"
                                                loading = false
                                            }
                                            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                                verificationId = id
                                                loading = false
                                            }
                                        }
                                        PhoneAuthProvider.verifyPhoneNumber(
                                            PhoneAuthOptions.newBuilder(Firebase.auth)
                                                .setPhoneNumber(phone.trim())
                                                .setTimeout(60L, TimeUnit.SECONDS)
                                                .setActivity(activity)
                                                .setCallbacks(callbacks)
                                                .build()
                                        )
                                    }
                                }
                            },
                            enabled = !loading && acceptedTerms,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Send SMS code") }
                    } else {
                        OutlinedTextField(
                            value = otp, onValueChange = { otp = it }, label = { Text("SMS verification code") },
                            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !loading, modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val id = verificationId
                                if (id.isNullOrBlank() || otp.isBlank()) error = "Enter the SMS code."
                                else runAuth { Firebase.auth.signInWithCredential(PhoneAuthProvider.getCredential(id, otp.trim())).await() }
                            },
                            enabled = !loading && acceptedTerms,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Verify code and sign in") }
                    }

                    HorizontalDivider()
                    Text("Continue with a provider", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        onClick = {
                            if (!acceptedTerms) error = "Please acknowledge the Terms and Conditions before continuing."
                            else {
                                loading = true
                                error = null
                                onGoogleSignInClicked(context, credentialManager, onAuthSuccess,
                                    { message -> error = message; loading = false }, scope,
                                    { loading = false })
                            }
                        },
                        enabled = !loading && acceptedTerms,
                        modifier = Modifier.fillMaxWidth().testTag("google_sign_in_button")
                    ) { Text("Google") }

                    listOf("apple.com" to "Apple", "facebook.com" to "Facebook", "github.com" to "GitHub").forEach { (providerId, label) ->
                        OutlinedButton(
                            onClick = {
                                if (!acceptedTerms) error = "Please acknowledge the Terms and Conditions before continuing."
                                else {
                                    val activity = context as? Activity
                                    if (activity == null) error = "Activity context required"
                                    else {
                                        loading = true
                                        error = null
                                        val provider = OAuthProvider.newBuilder(providerId).build()
                                        scope.launch {
                                            try {
                                                Firebase.auth.startActivityForSignInWithProvider(activity, provider).await()
                                                onAuthSuccess()
                                            } catch (e: Exception) {
                                                error = e.localizedMessage ?: "$label sign-in failed"
                                            } finally { loading = false }
                                        }
                                    }
                                }
                            },
                            enabled = !loading && acceptedTerms,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Continue with $label") }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it }, enabled = !loading)
                        Text("I acknowledge Edam's Terms and Conditions", style = MaterialTheme.typography.bodySmall)
                    }
                    if (loading) CircularProgressIndicator()
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.testTag("auth_error_text")) }
                }
            }
        }
    }
}
