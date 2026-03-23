package com.contactgraph.data.repository

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RawContact(
    val id: String,
    val name: String,
    val phoneNumbers: List<String>,
)

class ContactsRepository(private val context: Context) {

    suspend fun loadContacts(): List<RawContact> = withContext(Dispatchers.IO) {
        val phoneMap = mutableMapOf<String, MutableList<String>>() // contactId -> phones

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
            ),
            null, null, null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val cid = cursor.getString(idCol) ?: continue
                val num = normalizeNumber(cursor.getString(numCol) ?: continue)
                phoneMap.getOrPut(cid) { mutableListOf() }.add(num)
            }
        }

        val contacts = mutableListOf<RawContact>()
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ),
            "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1",
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            while (cursor.moveToNext()) {
                val id = cursor.getString(idCol) ?: continue
                val name = cursor.getString(nameCol)?.ifBlank { null } ?: "Unknown"
                val phones = phoneMap[id] ?: continue
                contacts.add(RawContact(id, name, phones))
            }
        }

        contacts
    }

    companion object {
        /** Strip spaces, dashes, parentheses from phone numbers for matching. */
        fun normalizeNumber(raw: String): String =
            raw.replace(Regex("[\\s\\-().+]"), "").trimStart('0')
    }
}
