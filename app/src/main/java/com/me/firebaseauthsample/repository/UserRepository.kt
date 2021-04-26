package com.me.firebaseauthsample.repository

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.gson.Gson
import com.me.firebaseauthsample.model.Authentication
import com.me.firebaseauthsample.model.User
import com.me.firebaseauthsample.utils.stringFlow
import com.me.firebaseauthsample.utils.await
import com.me.firebaseauthsample.utils.safeSendBlocking
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*

class UserRepository {

    companion object {
        private const val EXPIRED_GAP = 300000L
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            if (instance == null) {
                synchronized(UserRepository::class.java) { instance = UserRepository() }
            }
            return instance!!
        }
    }

    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val gson = Gson()

    enum class ExternalSignInProvider(val providerId: String) {
        APPLE("apple.com")
    }

    fun getUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    suspend fun getActiveToken(): String? {
        return try {
            var task = firebaseAuth.currentUser?.getIdToken(false)?.await()
            var expireTime: Long = task?.expirationTimestamp ?: 0L

            if (expireTime < 100_0000_000_000L) {
                expireTime *= 1000L
            }

            val isAlive = expireTime - System.currentTimeMillis() > EXPIRED_GAP

            if (!isAlive) {
                task = firebaseAuth.currentUser?.getIdToken(true)?.await()
            }
            task?.token
        } catch (e: Throwable) {
            null
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun signIn(authentication: Authentication): User? {
        return try {
            when (authentication) {
                is Authentication.Email -> signInWithEmail(
                    authentication.email,
                    authentication.password
                )
                is Authentication.Apple -> {
                    val obj = authentication.activity
                    if (obj !is Activity) {
                        throw Exception("You must pass activity to do authentication")
                    }
                    signInWithApple(obj)
                }
            }
        } catch (e: Throwable) {
            null
        }
    }

    fun saveUser(application: Application, user: User) {
        val json = gson.toJson(user)
        application.getSharedPreferences("user_profile", Context.MODE_PRIVATE).edit().putString(
            "Profile_${user.id}",
            json
        ).apply()
    }

    private suspend fun signInWithEmail(email: String, password: String): User? {
        val task = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = task.user ?: return null
        return User(user.uid, user.email ?: "", user.displayName, user.photoUrl?.toString())
    }

    private suspend fun signInWithApple(activity: Activity): User? {
        val task = firebaseAuth.startActivityForSignInWithProvider(
            activity,
            OAuthProvider.newBuilder(ExternalSignInProvider.APPLE.providerId, firebaseAuth).build()
        ).await()
        val user = task.user ?: return null
        return User(user.uid, user.email ?: "", user.displayName, user.photoUrl?.toString())
    }

    suspend fun isExistUserForEmail(email: String): Boolean {
        val task = firebaseAuth.fetchSignInMethodsForEmail(email).await()
        return !task.signInMethods.isNullOrEmpty()
    }

    private fun authStateChanges(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser ?: return@AuthStateListener this@callbackFlow.safeSendBlocking(
                null
            )
            this@callbackFlow.safeSendBlocking(
                User(
                    user.uid,
                    user.email ?: "",
                    user.displayName,
                    user.photoUrl?.toString()
                )
            )
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): User? {
        val task = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = task.user ?: return null
        return User(user.uid, user.email ?: "", user.displayName, user.photoUrl?.toString())
    }

    suspend fun reloadUser(): User? {
        return try {
            firebaseAuth.currentUser?.reload()?.await()
            val user = firebaseAuth.currentUser ?: return null
            User(user.uid, user.email ?: "", user.displayName, user.photoUrl?.toString())
        } catch (e: Exception) {
            null
        }
    }

    fun getUser(application: Application): Flow<User?> {
        return authStateChanges().flatMapLatest { getUser(application, it?.id) }
    }

    fun isUserLoggedIn(application: Application): Flow<Boolean> {
        return authStateChanges().flatMapLatest { getUser(application, it?.id) }.mapLatest { it != null }.distinctUntilChanged()
    }

    private fun getUser(application: Application, userId: String?): Flow<User?> {
        return application.getSharedPreferences("user_profile", Context.MODE_PRIVATE).stringFlow("Profile_${userId}").map {
            try {
                if (it.isEmpty()) {
                    return@map null
                }

                gson.fromJson(it, User::class.java)
            } catch (e: Throwable) {
                null
            }
        }
    }
}