package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ImageRef

// Mesma decisão do ContactsProvider.ios.kt: PHPickerViewController/UIImagePickerController é
// cinterop com frameworks Apple que não tenho como compilar nem validar sem Mac/Xcode.
// isAvailable() = false por enquanto — o botão de importar foto some da UI no iOS; desenho manual
// (Fase 3.5) continua funcionando normalmente.
actual class ImagePicker {
    actual suspend fun isAvailable(): Boolean = false
    actual suspend fun takePhoto(): ImageRef? = null
    actual suspend fun pickFromGallery(multiple: Boolean): List<ImageRef> = emptyList()
}
