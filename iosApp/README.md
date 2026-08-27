# iosApp

Este diretório contém o **código-fonte Swift** do app iOS (`iOSApp.swift`, `ContentView.swift`,
`Info.plist`), mas propositalmente **não inclui um `iosApp.xcodeproj`**.

Um arquivo `.xcodeproj` (`project.pbxproj`) é um formato binário/serializado que o Xcode gera e
mantém — escrevê-lo à mão fora do Xcode tem alto risco de gerar um projeto corrompido, o que seria
pior do que não ter nenhum. Como a geração deste código aconteceu numa máquina Windows, sem Xcode
disponível para validar, o projeto `.xcodeproj` precisa ser criado uma vez, no Mac, apontando para
estes arquivos já prontos:

## Como criar o projeto (uma vez, no Mac)

1. Rode `./gradlew :shared:embedAndSignAppleFrameworkForXcode` (ou abra o projeto normalmente — o
   Kotlin Multiplatform Gradle plugin expõe o `shared` como framework para o Xcode consumir).
2. No Xcode: **File → New → Project → iOS → App**.
   - Product Name: `iosApp`
   - Interface: SwiftUI
   - Salve dentro desta pasta (`iosApp/`), substituindo o `ContentView.swift` e `iOSApp.swift`
     gerados pelo wizard pelos deste diretório (ou copie o conteúdo deles).
3. Adicione o framework `shared` ao projeto (**General → Frameworks, Libraries, and Embedded
   Content**), ou configure um *Run Script Build Phase* chamando o Gradle
   (`embedAndSignAppleFrameworkForXcode`) — é o padrão recomendado pelo template oficial do
   Kotlin Multiplatform.
4. Substitua o `Info.plist` gerado pelo deste diretório.
5. Rode em um simulador ou dispositivo físico.

Depois de criado uma vez, o `.xcodeproj` deve ser commitado no repositório normalmente.
