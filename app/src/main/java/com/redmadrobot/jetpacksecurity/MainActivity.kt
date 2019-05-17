package com.redmadrobot.jetpacksecurity

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream

class MainActivity : AppCompatActivity() {
    private val file by lazy { File(filesDir, "super_secure_file") }

    private val encryptedFile by lazy {
        EncryptedFile.Builder(file, this, "my_secret_key", EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB)
            .setKeysetAlias("my_test_keyset_alias")
            .setKeysetPrefName("keyset_pref_file")
            .build()
    }

    private val encryptedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "super_secret_preferences",
            "prefrences_master_key",
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        encrypt_file_button.setOnClickListener {
            if (file.delete()) {
                val plainText = "${edit_file_content.text}".toByteArray()

                encryptedFile.openFileOutput().use {
                    it.write(plainText)
                }

                val cipherText = FileInputStream(file).use {
                    Base64.encodeToString(it.readBytes(), Base64.NO_WRAP)
                }

                edit_file_content.setText(cipherText)
            }
        }

        decrypt_file_button.setOnClickListener {
            val fileContent = encryptedFile.openFileInput().use {
                it.readBytes()
            }

            edit_file_content.setText(String(fileContent))
        }

        save_prefs_button.setOnClickListener {
            encryptedPreferences.edit()
                .putString("login", "${edit_login.text}")
                .putString("password", "${edit_password.text}")
                .apply()
        }

        load_prefs_button.setOnClickListener {
            edit_login.setText(encryptedPreferences.getString("login", "no data"))
            edit_password.setText(encryptedPreferences.getString("password", "no data"))
        }
    }
}
