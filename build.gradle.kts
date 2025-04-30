// Archivo de configuración de nivel superior (para todos los módulos)
// Archivo de configuración de nivel superior (para todos los módulos)
plugins {
    id("com.android.application") version "8.9.2" apply false
    id("com.android.library") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false // **Añade esta línea aquí**
}
