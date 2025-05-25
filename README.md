# ListasReproduccionQuipux
API para gestionar listas de reproducción

Este proyecto implementa una API RESTful para la gestión de listas de reproducción y canciones utilizando Spring Boot, Spring Security con JWT y una base de datos en memoria H2.

## Tecnologías Principales

*   Java 17
*   Spring Boot 3.X.X
*   Maven
*   Spring Data JPA
*   Spring Security (con JWT)
*   H2 Database
*   Lombok
*   Bean Validation
*   io.jsonwebtoken

## Estructura del Proyecto

El proyecto sigue una estructura de paquetes modular:

```
src/main/java/com/example/playlist_api/
├───config/
├───controller/
├───dto/
├───entity/
├───exception/
├───repository/
├───security/
├───service/
└───PlaylistApiApplication.java
```

*   `config/`: Clases de configuración (ej. `SecurityConfig`).
*   `controller/`: Controladores REST para manejar las peticiones HTTP.
*   `dto/`: Objetos de Transferencia de Datos (DTOs) para la comunicación entre capas.
*   `entity/`: Entidades JPA que representan las tablas de la base de datos.
*   `exception/`: Clases para el manejo de excepciones personalizadas.
*   `repository/`: Interfaces de Spring Data JPA para el acceso a datos.
*   `security/`: Componentes relacionados con la seguridad y JWT.
*   `service/`: Lógica de negocio de la aplicación.
*   `PlaylistApiApplication.java`: Clase principal para iniciar la aplicación Spring Boot.

## Configuración de la Base de Datos (H2)

La base de datos H2 en memoria está configurada en `src/main/resources/application.properties`. Puedes acceder a la consola H2 en `http://localhost:8080/h2-console` después de iniciar la aplicación.

## Pruebas con Postman

Este proyecto incluye una colección de Postman (`ListasReproduccionQuipux.postman_collection.json`) con las definiciones de los endpoints y pruebas para los controladores. Sigue estos pasos para ejecutar las pruebas:

### Prerrequisitos

*   Asegúrate de que tu aplicación Spring Boot (`playlist-api`) esté en ejecución.
*   Ten instalado Postman.
*   La URL base de tu aplicación será `http://localhost:8080` (o el puerto que hayas configurado).

### Paso 1: Importar la Colección de Postman

1.  Abre Postman.
2.  Haz clic en "Import".
3.  Selecciona el archivo `ListasReproduccionQuipux.postman_collection.json` desde la ubicación de tu proyecto.
4.  La colección "ListasReproduccionQuipux" aparecerá en tu espacio de trabajo.

### Paso 2: Obtener el Token de Autenticación (JWT)

Necesitarás un token para acceder a la mayoría de los endpoints protegidos. La colección de Postman ya incluye una solicitud para esto.

1.  En la colección importada, expande la carpeta y selecciona la solicitud llamada "AuthRequest".
2.  Ve a la pestaña "Body". Verás el JSON para autenticar como `admin`.
3.  Haz clic en "Send".
4.  En la respuesta (pestaña "Body"), copia el valor del campo `token`. Este es tu token JWT de administrador.
5.  Si deseas obtener un token para el usuario `user`, modifica el "Body" de la petición con las credenciales de `user` (`username: "user"`, `password: "userpass"`) y envía la petición nuevamente.

### Paso 3: Ejecutar las Peticiones de la API

Para cada petición a los endpoints protegidos, deberás incluir el token JWT obtenido en el Paso 2 en el encabezado de la solicitud.

1.  Selecciona la solicitud que deseas probar (por ejemplo, "ObtenerCanciones").
2.  Ve a la pestaña "Headers".
3.  En el encabezado `Authorization`, reemplaza `<TU_TOKEN_JWT>` con el token que copiaste en el Paso 2. Asegúrate de mantener el prefijo `Bearer ` antes del token.
4.  Si la solicitud requiere un cuerpo (POST, PUT), ve a la pestaña "Body" y ajusta los datos JSON según sea necesario.
5.  Haz clic en "Send".
6.  Verifica la respuesta (código de estado, cuerpo de la respuesta) y la pestaña "Test Results" para ver si las pruebas asociadas a la solicitud pasaron.

A continuación, se detallan los endpoints disponibles y los roles requeridos:

#### Endpoints de CancionController (`/api/canciones`)

*   **Crear una Nueva Canción**
    *   Método: `POST`
    *   URL: `/api/canciones`
    *   Rol Requerido: `ADMIN`
    *   Body: JSON con los datos de la canción (`titulo`, `artista`, `album`, `anno`, `genero`).
    *   Ejemplo de Body:
        ```json
        {
            "titulo": "Stairway to Heaven",
            "artista": "Led Zeppelin",
            "album": "Led Zeppelin IV",
            "anno": "1971",
            "genero": "Rock"
        }
        ```
    *   Prueba en Postman: "CrearCancion"

*   **Obtener Todas las Canciones**
    *   Método: `GET`
    *   URL: `/api/canciones`
    *   Rol Requerido: `ADMIN` o `USER`
    *   Prueba en Postman: "ObtenerCanciones"

*   **Obtener una Canción por ID**
    *   Método: `GET`
    *   URL: `/api/canciones/{id}`
    *   Rol Requerido: `ADMIN` o `USER`
    *   Prueba en Postman: "ObtenerCancionPorId" (modifica el ID en la URL)

*   **Actualizar una Canción**
    *   Método: `PUT`
    *   URL: `/api/canciones/{id}`
    *   Rol Requerido: `ADMIN`
    *   Body: JSON con los datos actualizados de la canción.
    *   Ejemplo de Body:
        ```json
        {
            "titulo": "Stairway to Heaven (Remastered)",
            "artista": "Led Zeppelin",
            "album": "Led Zeppelin IV (Deluxe Edition)",
            "anno": "1971",
            "genero": "Hard Rock"
        }
        ```
    *   Prueba en Postman: "ActualizarCancion" (modifica el ID en la URL y el Body)

*   **Borrar una Canción**
    *   Método: `DELETE`
    *   URL: `/api/canciones/{id}`
    *   Rol Requerido: `ADMIN`
    *   Prueba en Postman: "EliminarCancion" (modifica el ID en la URL)

#### Endpoints de PlaylistController (`/api/listas`)

*   **Crear una Nueva Lista de Reproducción (con IDs de canciones)**
    *   Método: `POST`
    *   URL: `/api/listas`
    *   Rol Requerido: `ADMIN`
    *   Body: JSON con `nombre`, `descripcion` y un array opcional `cancionIds`.
    *   Ejemplo de Body:
        ```json
        {
            "nombre": "Rock Clásico",
            "descripcion": "Lo mejor del rock de los 70s y 80s",
            "cancionIds": [1, 2]
        }
        ```
    *   Prueba en Postman: "CrearListaReproduccion" (ajusta el Body con IDs de canciones existentes)

*   **Crear Lista con Canción Inicial**
    *   Método: `POST`
    *   URL: `/api/listas/con-cancion-inicial/{idCancion}`
    *   Rol Requerido: `ADMIN`
    *   Body: JSON con `nombre` y `descripcion`.
    *   Ejemplo de Body:
        ```json
        {
            "nombre": "Playlist para Viajar",
            "descripcion": "Canciones para la carretera"
        }
        ```
    *   Prueba en Postman: "CrearListaReproduccionConCancion" (modifica el `idCancion` en la URL y ajusta el Body)

*   **Obtener Todas las Listas de Reproducción**
    *   Método: `GET`
    *   URL: `/api/listas`
    *   Rol Requerido: `ADMIN` o `USER`
    *   Prueba en Postman: "ObtenerListasReproduccion"

*   **Obtener una Lista por Nombre**
    *   Método: `GET`
    *   URL: `/api/listas/porNombre/{nombreLista}`
    *   Rol Requerido: `ADMIN` o `USER`
    *   *Nota: Esta petición no está directamente en la colección de Postman proporcionada, pero puedes crearla fácilmente basándote en las otras peticiones GET.*

*   **Obtener una Lista por ID**
    *   Método: `GET`
    *   URL: `/api/listas/{id}`
    *   Rol Requerido: `ADMIN` o `USER`
    *   Prueba en Postman: "ObtenerListasReproduccionPorId" (modifica el ID en la URL)

*   **Actualizar una Lista de Reproducción**
    *   Método: `PUT`
    *   URL: `/api/listas/{id}`
    *   Rol Requerido: `ADMIN`
    *   Body: JSON con los datos actualizados de la lista (`nombre`, `descripcion`, `cancionIds`).
    *   Ejemplo de Body:
        ```json
        {
            "nombre": "Rock Clásico V2",
            "descripcion": "Actualizado con más energía",
            "cancionIds": [1, 3]
        }
        ```
    *   Prueba en Postman: "ActualizarListasReproduccion" (modifica el ID en la URL y el Body)

*   **Borrar una Lista por Nombre**
    *   Método: `DELETE`
    *   URL: `/api/listas/porNombre/{nombreLista}`
    *   Rol Requerido: `ADMIN`
    *   *Nota: Esta petición no está directamente en la colección de Postman proporcionada, pero puedes crearla fácilmente basándote en las otras peticiones DELETE.*

*   **Borrar una Lista por ID**
    *   Método: `DELETE`
    *   URL: `/api/listas/{id}`
    *   Rol Requerido: `ADMIN`
    *   Prueba en Postman: "EliminarListaReproduccion" (modifica el ID en la URL)

Asegúrate de tener la aplicación en ejecución antes de intentar ejecutar las peticiones de Postman.
