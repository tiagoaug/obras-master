package br.com.tiago.obramaster.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import br.com.tiago.obramaster.domain.ContatoImportado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class ContactsProvider(private val context: Context) {

    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun pickContacts(): List<ContatoImportado> {
        val jaConcedida = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        val concedida = jaConcedida || ContactsPermissionBridge.solicitar()
        if (!concedida) return emptyList()

        return withContext(Dispatchers.IO) {
            val contatos = mutableListOf<ContatoImportado>()
            val resolver = context.contentResolver
            resolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME,
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nomeIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIndex) ?: continue
                    val nome = cursor.getString(nomeIndex) ?: continue
                    contatos.add(
                        ContatoImportado(
                            nome = nome,
                            telefone = buscarTelefone(resolver, id),
                            email = buscarEmail(resolver, id),
                        ),
                    )
                }
            }
            contatos
        }
    }

    private fun buscarTelefone(resolver: android.content.ContentResolver, contatoId: String): String? {
        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contatoId),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }
        return null
    }

    private fun buscarEmail(resolver: android.content.ContentResolver, contatoId: String): String? {
        resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contatoId),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
            }
        }
        return null
    }
}
