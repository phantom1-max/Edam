package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.LocalEdamThemeSpec
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

    scope.launch {
        try {
            val activity = context as? Activity
            if (activity == null) {
                onAuthError("Activity context required for interactive Google Sign-In")
                return@launch
            }
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
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


private fun authenticateWithEmail(
    email: String,
    password: String,
    createAccount: Boolean,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    scope: CoroutineScope
) {
    scope.launch {
        try {
            if (createAccount) {
                Firebase.auth.createUserWithEmailAndPassword(email.trim(), password).await()
            } else {
                Firebase.auth.signInWithEmailAndPassword(email.trim(), password).await()
            }
            onSuccess()
        } catch (error: Exception) {
            onError(error.localizedMessage ?: "Email authentication failed")
        }
    }
}

private fun requestPhoneOtp(
    activity: Activity,
    phoneNumber: String,
    onCodeSent: (String) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    scope: CoroutineScope
) {
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            scope.launch {
                try {
                    Firebase.auth.signInWithCredential(credential).await()
                    onSuccess()
                } catch (error: Exception) {
                    onError(error.localizedMessage ?: "Phone authentication failed")
                }
            }
        }

        override fun onVerificationFailed(error: com.google.firebase.FirebaseException) {
            onError(error.localizedMessage ?: "Could not send the verification code")
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            onCodeSent(verificationId)
        }
    }

    PhoneAuthProvider.verifyPhoneNumber(
        PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(phoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
    )
}

private fun authenticateWithPhoneOtp(
    verificationId: String,
    otp: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    scope: CoroutineScope
) {
    scope.launch {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp.trim())
            Firebase.auth.signInWithCredential(credential).await()
            onSuccess()
        } catch (error: Exception) {
            onError(error.localizedMessage ?: "Invalid or expired verification code")
        }
    }
}

private fun authenticateWithProvider(
    activity: Activity,
    providerId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    scope: CoroutineScope
) {
    scope.launch {
        try {
            val provider = OAuthProvider.newBuilder(providerId).build()
            Firebase.auth.startActivityForSignInWithProvider(activity, provider).await()
            onSuccess()
        } catch (error: Exception) {
            onError(error.localizedMessage ?: "Provider sign-in failed")
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val themeSpec = LocalEdamThemeSpec.current

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var phoneVerificationId by remember { mutableStateOf<String?>(null) }
    var termsAccepted by remember { mutableStateOf(false) }


    val glowAlpha = themeSpec.radialGlowAlpha
    val primaryGlow = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
    val secondaryGlow = MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha)
    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = bgColor)
                if (glowAlpha > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryGlow, bgColor.copy(alpha = 0f)),
                            center = Offset(size.width * 0.18f, size.height * 0.14f),
                            radius = size.maxDimension * 0.42f
                        ),
                        center = Offset(size.width * 0.18f, size.height * 0.14f),
                        radius = size.maxDimension * 0.42f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(secondaryGlow, bgColor.copy(alpha = 0f)),
                            center = Offset(size.width * 0.82f, size.height * 0.20f),
                            radius = size.maxDimension * 0.42f
                        ),
                        center = Offset(size.width * 0.82f, size.height * 0.20f),
                        radius = size.maxDimension * 0.42f
                    )
                }
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = if (themeSpec.isHighContrast) 0.dp else 4.dp,
            shadowElevation = if (themeSpec.isHighContrast) 0.dp else 12.dp,
            border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand badge with animated Edam Mascot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EdamMascot(
                        expression = EdamExpression.HAPPY,
                        character = EdamCompanionCharacter.EDAM,
                        size = 48.dp,
                        showTablet = false
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Official Edam Mascot & Companion Squad Hero Stage (replacing old AI book illustration)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_mascot_hero_stage"),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            EdamMascot(
                                expression = EdamExpression.CURIOUS,
                                character = EdamCompanionCharacter.KORA,
                                size = 56.dp,
                                showTablet = false
                            )
                            EdamMascot(
                                expression = EdamExpression.IDLE,
                                character = EdamCompanionCharacter.EDAM,
                                size = 96.dp,
                                showTablet = true
                            )
                            EdamMascot(
                                expression = EdamExpression.THINKING,
                                character = EdamCompanionCharacter.VEX,
                                size = 56.dp,
                                showTablet = false
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Meet Edam, Kora, Vex & Nova — Your Learning Companions",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = stringResource(R.string.auth_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.auth_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Feature highlights pill list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuthFeatureRow(
                        icon = Icons.Filled.OfflinePin,
                        text = "Daily Streak Flashcards, XP Rewards & 100% Offline Course Packs"
                    )
                    AuthFeatureRow(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        text = "Stock Market Lab (Equities, Options, Bonds, RSI & Candlestick Graphs)"
                    )
                    AuthFeatureRow(
                        icon = Icons.Filled.School,
                        text = "Novice to Grandmaster (GM) Chess Training & Interactive Board"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sign in or create an account",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().testTag("email_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (at least 6 characters)") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().testTag("password_input")
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("terms_checkbox")
                    )
                    Text(
                        text = "I agree to the Terms and Conditions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (!termsAccepted) {
                    Text(
                        text = "Accept the Terms and Conditions to continue.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (email.isBlank() || password.length < 6) {
                                errorMessage = "Enter an email and a password of at least 6 characters."
                            } else {
                                isLoading = true
                                errorMessage = null
                                authenticateWithEmail(email, password, false, {
                                    isLoading = false
                                    onAuthSuccess()
                                }, {
                                    isLoading = false
                                    errorMessage = it
                                }, scope)
                            }
                        },
                        enabled = !isLoading && termsAccepted,
                        modifier = Modifier.weight(1f).testTag("email_sign_in_button")
                    ) { Text("Sign in") }
                    Button(
                        onClick = {
                            if (email.isBlank() || password.length < 6) {
                                errorMessage = "Enter an email and a password of at least 6 characters."
                            } else {
                                isLoading = true
                                errorMessage = null
                                authenticateWithEmail(email, password, true, {
                                    isLoading = false
                                    onAuthSuccess()
                                }, {
                                    isLoading = false
                                    errorMessage = it
                                }, scope)
                            }
                        },
                        enabled = !isLoading && termsAccepted,
                        modifier = Modifier.weight(1f).testTag("email_create_account_button")
                    ) { Text("Create account") }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Use a phone number",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone number with country code") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().testTag("phone_input")
                )
                if (phoneVerificationId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text("SMS verification code") },
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().testTag("otp_input")
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val activity = context as? Activity
                        if (activity == null) {
                            errorMessage = "An activity is required to verify a phone number."
                        } else if (phoneVerificationId == null) {
                            isLoading = true
                            errorMessage = null
                            requestPhoneOtp(activity, phoneNumber, {
                                phoneVerificationId = it
                                isLoading = false
                            }, {
                                isLoading = false
                                onAuthSuccess()
                            }, {
                                isLoading = false
                                errorMessage = it
                            }, scope)
                        } else {
                            isLoading = true
                            errorMessage = null
                            authenticateWithPhoneOtp(phoneVerificationId!!, otp, {
                                isLoading = false
                                onAuthSuccess()
                            }, {
                                isLoading = false
                                errorMessage = it
                            }, scope)
                        }
                    },
                    enabled = !isLoading && termsAccepted && phoneNumber.isNotBlank() &&
                        (phoneVerificationId == null || otp.isNotBlank()),
                    modifier = Modifier.fillMaxWidth().testTag("phone_otp_button")
                ) {
                    Text(if (phoneVerificationId == null) "Send SMS code" else "Verify SMS code")
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Text(
                        text = stringResource(R.string.auth_or_divider),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        onGoogleSignInClicked(context, credentialManager, {
                            isLoading = false
                            onAuthSuccess()
                        }, {
                            isLoading = false
                            errorMessage = it
                        }, scope, { isLoading = false })
                    },
                    enabled = !isLoading && termsAccepted,
                    modifier = Modifier.fillMaxWidth().testTag("google_sign_in_button")
                ) { Text(stringResource(R.string.btn_sign_in_google)) }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        onGoogleSignInClicked(context, credentialManager, {
                            isLoading = false
                            onAuthSuccess()
                        }, {
                            isLoading = false
                            errorMessage = it
                        }, scope, { isLoading = false })
                    },
                    enabled = !isLoading && termsAccepted,
                    modifier = Modifier.fillMaxWidth().testTag("google_create_account_button")
                ) { Text(stringResource(R.string.btn_sign_up_google)) }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity == null) errorMessage = "An activity is required for Apple sign-in."
                            else {
                                isLoading = true
                                authenticateWithProvider(activity, "apple.com", {
                                    isLoading = false
                                    onAuthSuccess()
                                }, {
                                    isLoading = false
                                    errorMessage = it
                                }, scope)
                            }
                        },
                        enabled = !isLoading && termsAccepted,
                        modifier = Modifier.weight(1f).testTag("apple_sign_in_button")
                    ) { Text("Apple") }
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity == null) errorMessage = "An activity is required for GitHub sign-in."
                            else {
                                isLoading = true
                                authenticateWithProvider(activity, "github.com", {
                                    isLoading = false
                                    onAuthSuccess()
                                }, {
                                    isLoading = false
                                    errorMessage = it
                                }, scope)
                            }
                        },
                        enabled = !isLoading && termsAccepted,
                        modifier = Modifier.weight(1f).testTag("github_sign_in_button")
                    ) { Text("GitHub") }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val activity = context as? Activity
                        if (activity == null) errorMessage = "An activity is required for Facebook sign-in."
                        else {
                            isLoading = true
                            authenticateWithProvider(activity, "facebook.com", {
                                isLoading = false
                                onAuthSuccess()
                            }, {
                                isLoading = false
                                errorMessage = it
                            }, scope)
                        }
                    },
                    enabled = !isLoading && termsAccepted,
                    modifier = Modifier.fillMaxWidth().testTag("facebook_sign_in_button")
                ) { Text(stringResource(R.string.btn_sign_in_facebook)) }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onAuthSuccess() },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("guest_explore_button")
                ) {
                    Icon(imageVector = Icons.Filled.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.btn_explore_guest), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("auth_error_text")
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
