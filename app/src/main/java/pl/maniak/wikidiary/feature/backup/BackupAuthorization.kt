package pl.maniak.wikidiary.feature.backup

import android.accounts.Account
import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

const val BACKUP_SCOPE = "https://www.googleapis.com/auth/drive.appdata"

fun backupAuthorizationRequest(account: String): AuthorizationRequest = AuthorizationRequest.builder()
    .setAccount(Account(account, "com.google"))
    .setRequestedScopes(listOf(Scope(BACKUP_SCOPE)))
    .setOptOutIncludingGrantedScopes(true)
    .build()

class BackupAuthorization(private val context: Context) {
    suspend fun token(account: String): String = suspendCancellableCoroutine { continuation ->
        Identity.getAuthorizationClient(context).authorize(backupAuthorizationRequest(account))
            .addOnSuccessListener { result ->
                if (!continuation.isActive) return@addOnSuccessListener
                val token = result.accessToken
                if (result.hasResolution() || token.isNullOrBlank() || BACKUP_SCOPE !in result.grantedScopes) {
                    continuation.resumeWithException(BackupCloudException(
                        "Kopia wymaga zgody Google. W ustawieniach wybierz Zatwierdź dostęp.", false))
                } else continuation.resume(token)
            }
            .addOnFailureListener { exception ->
                val retryable = exception is ApiException && exception.statusCode in setOf(
                    CommonStatusCodes.NETWORK_ERROR, CommonStatusCodes.TIMEOUT, CommonStatusCodes.INTERNAL_ERROR)
                if (continuation.isActive) continuation.resumeWithException(
                    BackupCloudException(
                        if (retryable) "Nie udało się połączyć z Google. Kopia zostanie ponowiona."
                        else "Nie udało się uzyskać dostępu Google. Zatwierdź dostęp w ustawieniach.", retryable))
            }
    }
}
