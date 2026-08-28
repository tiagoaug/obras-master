package br.com.tiago.obramaster.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.tiago.obramaster.App
import br.com.tiago.obramaster.platform.ContactsPermissionBridge
import br.com.tiago.obramaster.platform.FilePickerBridge
import br.com.tiago.obramaster.platform.ImagePickerBridge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContactsPermissionBridge.registrar(this)
        ImagePickerBridge.registrar(this)
        FilePickerBridge.registrar(this)

        setContent {
            App()
        }
    }
}
