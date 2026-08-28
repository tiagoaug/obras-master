package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ArquivoSelecionado

// Mesma decisão do ImagePicker.ios.kt/ContactsProvider.ios.kt: UIDocumentPickerViewController é
// cinterop com framework Apple que não tenho como compilar nem validar sem Mac/Xcode.
// isAvailable() = false por enquanto — o botão de importar arquivo some da UI no iOS; desenho
// manual (Fase 3.5) e importação de foto continuam funcionando normalmente.
actual class FilePicker {
    actual suspend fun isAvailable(): Boolean = false
    actual suspend fun escolherArquivo(extensoesAceitas: List<String>): ArquivoSelecionado? = null
}
