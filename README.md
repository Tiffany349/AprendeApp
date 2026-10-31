#  AprendeApp

**Sistema de Gestión de Recursos de Aprendizaje**

AprendeApp es una aplicación móvil Android desarrollada con Kotlin que permite consultar y administrar recursos educativos relacionados con Ingeniería en Sistemas. Su propósito es facilitar el acceso a libros, videos y artículos educativos mediante una interfaz intuitiva y una API dinámica.

La aplicación cuenta con autenticación de usuarios, control de acceso según roles, búsqueda avanzada, filtros, favoritos y un sistema de calificaciones para compartir la valoración de los recursos.

---

##  Autores

**Institución:** Universidad Don Bosco.

**Carrera:** Técnico en Ingeniería en Computación.

**Asignatura:** Desarrollo de Software para Móviles DSM441 G04L

**Integrantes:**

- Tiffany Nahomy Benítez Reyes BR250073.

---

##  Objetivo del proyecto

Desarrollar una aplicación Android que facilite la gestión y consulta de recursos de aprendizaje, aplicando la arquitectura MVC, el consumo de servicios web mediante una API REST y la persistencia de información de los usuarios.

##  Funcionalidades principales

### 1. Registro e inicio de sesión
- Registro de nuevos usuarios.
- Inicio de sesión mediante correo electrónico y contraseña.
- Validación de campos obligatorios.
- Verificación de correos electrónicos registrados.
- Validación de contraseñas seguras.
- Persistencia de la sesión del usuario.
- Cierre de sesión.

**Requisitos de contraseña:**
- Mínimo 12 caracteres.
- Al menos una letra mayúscula.
- Al menos una letra minúscula.
- Al menos un número.
- Al menos un carácter especial: `! @ # $ % ^ & *`.

### 2. Sistema de autenticación con roles

La aplicación maneja dos roles:

| Rol | Permisos |
|---|---|
| Estudiante (`usuario`) | Consultar, buscar, filtrar, marcar como favorito y calificar recursos. |
| Docente (`docente`) | Acceder a la administración para crear, editar y eliminar recursos. |

El acceso a las funciones de administración está restringido a los usuarios con rol docente.

### 3. Visualización y gestión de recursos

Los recursos se presentan mediante tarjetas que contienen:
- Título.
- Descripción.
- Tipo de recurso.
- Imagen ilustrativa.
- Enlace de acceso al contenido.
- Calificación individual.
- Promedio general de calificaciones.

Los tipos de recursos disponibles son:
- Libro.
- Video.
- Artículo.

### 4. Búsqueda y filtros

La aplicación permite:
- Buscar recursos por ID.
- Buscar por título.
- Buscar por tipo.
- Filtrar por categoría.
- Ordenar por título de manera ascendente o descendente.
- Ordenar por tipo de recurso.

### 5. Administración de recursos (Docente)

El docente dispone de una pantalla de administración con operaciones CRUD conectadas a MockAPI.

| Operación | Descripción | Método HTTP |
|---|---|---|
| Crear | Registrar un nuevo recurso. | POST |
| Consultar | Obtener los recursos disponibles. | GET |
| Actualizar | Modificar un recurso existente. | PUT |
| Eliminar | Eliminar un recurso de la lista. | DELETE |

Los formularios incluyen validaciones para los campos obligatorios y las direcciones URL.

### 6. Lista de favoritos

- Permite marcar y quitar recursos de favoritos.
- Permite visualizar únicamente los recursos seleccionados.
- Conserva los favoritos mediante almacenamiento local.
- Las preferencias se almacenan de manera independiente para cada usuario.

### 7. Sistema de calificaciones

- Permite calificar recursos de 1 a 5 estrellas.
- Guarda las calificaciones en el backend.
- Permite modificar una calificación anterior.
- Muestra la calificación individual del usuario.
- Calcula y muestra el promedio general de cada recurso.

---

##  Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje de programación. |
| Android Studio | Entorno de desarrollo. |
| XML | Diseño de las interfaces. |
| MockAPI | API REST para la gestión de datos. |
| Retrofit | Consumo de servicios web. |
| Gson | Conversión de datos JSON. |
| Picasso | Carga de imágenes. |
| RecyclerView | Presentación de listas de recursos. |
| Material Components | Componentes de interfaz. |
| SharedPreferences | Persistencia local de sesión y favoritos. |
| Git y GitHub | Control de versiones y alojamiento del código. |

---

##  Arquitectura del proyecto

La aplicación utiliza el patrón arquitectónico **MVC (Modelo-Vista-Controlador)** para organizar sus responsabilidades.

### Modelo (Model)

Contiene las estructuras de datos, solicitudes y componentes encargados de la comunicación con la API.

Clases principales:
- `Recurso`
- `RecursoRequest`
- `Usuario`
- `UsuarioRequest`
- `Calificacion`
- `UsuarioCalificacionesRequest`
- `RecursoApi`
- `UsuarioApi`
- `RetrofitClient`

### Vista (View)

Se encarga de mostrar la información y los componentes visuales de la aplicación.

Componentes principales:
- `RecursoAdapter`
- `AdminRecursoAdapter`
- Diseños XML de las diferentes pantallas y tarjetas.

### Controlador

Las actividades coordinan las acciones del usuario y la comunicación entre las vistas y los modelos.

Actividades principales:
- `AccesoActivity`
- `MainActivity`
- `DocenteActivity`

### Persistencia local

La aplicación utiliza:
- `SesionManager`: administra la información de la sesión.
- `RecursoPreferencias`: almacena los favoritos y las preferencias de calificación locales.

### Estructura de paquetes

```text
sv.edu.aprendeapp
│
├── model
│   ├── api
│   │   ├── RecursoApi
│   │   ├── RetrofitClient
│   │   └── UsuarioApi
│   │
│   ├── Calificacion
│   ├── Recurso
│   ├── RecursoRequest
│   ├── Usuario
│   ├── UsuarioRequest
│   └── UsuarioCalificacionesRequest
│
├── view
│   ├── AdminRecursoAdapter
│   └── RecursoAdapter
│
├── AccesoActivity
├── DocenteActivity
├── MainActivity
├── RecursoPreferencias
└── SesionManager
```

---

##  API y almacenamiento de datos

AprendeApp utiliza MockAPI como servicio externo para almacenar y consultar información en formato JSON.

**URL base de la API:**

```text
https://6ae182c1be3bc8fead9899dc.mockapi.io/apiv1/
```

### Recurso: recursos

Endpoint:

```text
GET    /recursos
POST   /recursos
GET    /recursos/{id}
PUT    /recursos/{id}
DELETE /recursos/{id}
```

Estructura de un recurso:

```json
{
  "id": "1",
  "titulo": "Think Python",
  "descripcion": "Libro para aprender programación utilizando Python.",
  "tipo": "Libro",
  "enlace": "https://allendowney.github.io/ThinkPython/",
  "imagen": ""
}
```

### Recurso: usuarios

Endpoint:

```text
GET   /usuarios
POST  /usuarios
PATCH /usuarios/{id}
```

Estructura de un usuario:

```json
{
  "id": "1",
  "nombre": "Nombre del usuario",
  "correo": "usuario@ejemplo.com",
  "password": "Contraseña",
  "rol": "usuario",
  "calificaciones": "[]"
}
```

Las calificaciones se almacenan en el campo `calificaciones` como una cadena JSON asociada al usuario.

Ejemplo de su contenido:

```json
[
  {
    "recursoId": "1",
    "usuarioId": "1",
    "calificacion": 5
  },
  {
    "recursoId": "2",
    "usuarioId": "1",
    "calificacion": 4
  }
]
```

---

##  Interfaz de usuario

La aplicación utiliza una interfaz basada en Material Design, con componentes visuales para facilitar la navegación y la interacción.

### Paleta de colores

| Color | Código | Aplicación |
|---|---|---|
| Vino | `#930031` | Color principal. |
| Morado | `#6C5B7B` | Color secundario. |
| Turquesa | `#0D7377` | Elementos destacados. |
| Azul | `#3282B8` | Información y enlaces. |
| Verde | `#4A943E` | Acciones exitosas. |

También cuenta con adaptación al tema claro y oscuro del dispositivo.

### Experiencia de usuario (UI/UX)

- Tarjetas para presentar los recursos educativos.
- Buscador y filtros de fácil acceso.
- Formularios para la administración de recursos.
- Botones con estilos diferenciados.
- Mensajes de confirmación y error.
- Indicadores de carga.
- Navegación entre las pantallas de acceso, consulta y administración.

---

##  Instalación y ejecución

### Requisitos previos

- Android Studio.
- JDK compatible con la configuración de Gradle del proyecto.
- Conexión a Internet.
- Dispositivo Android o emulador.

### Pasos para ejecutar el proyecto

1. Clonar el repositorio:

```bash
git clone URL_DEL_REPOSITORIO
```

2. Abrir Android Studio.

3. Seleccionar **Open** y elegir la carpeta del proyecto `AprendeApp`.

4. Esperar a que finalice la sincronización de Gradle.

5. Verificar la configuración de la API en `RetrofitClient.kt`.

6. Comprobar que la URL base corresponda a la instancia de MockAPI utilizada:

```kotlin
private const val BASE_URL =
    "https://6ae182c1be3bc8fead9899dc.mockapi.io/apiv1/"
```

7. Seleccionar un dispositivo o emulador Android.

8. Ejecutar el proyecto mediante **Run**.

---

##  Consideraciones importantes

- La aplicación necesita conexión a Internet para consultar y modificar los datos alojados en MockAPI.
- La cuenta docente debe existir previamente en el recurso `usuarios`, con el rol `docente`.
- Los nuevos registros se crean con el rol `usuario`.
- Los favoritos se conservan localmente en el dispositivo.
- Las calificaciones se almacenan en el backend para consultar los promedios compartidos.
- MockAPI se utiliza como backend de demostración académica. Para un entorno de producción se recomienda implementar autenticación segura en servidor y almacenamiento protegido de contraseñas.

---


##  Video demostrativo

**Defensa del proyecto:**

https://udbedu-my.sharepoint.com/:f:/g/personal/br250073_alumno_udb_edu_sv/IgCvtKHJKtRRS7aC-VcirY6zAd_dA2jskYCyVe7j7tC51jM?e=EvPz6H

En el video se presenta el funcionamiento de AprendeApp, incluyendo el registro, inicio de sesión, roles, consulta de recursos, búsqueda, filtros, favoritos, calificaciones y operaciones CRUD del docente.

---



*AprendeApp — Aprende, consulta y comparte conocimiento.* 
