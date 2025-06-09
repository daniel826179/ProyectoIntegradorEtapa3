Gestor de Tareas con MySQL

Este es un proyecto en Java que implementa un gestor de tareas con una interfaz gráfica (GUI) utilizando Swing, conectado a una base de datos MySQL. Permite agregar, editar, eliminar y marcar tareas como completadas, con notificaciones automáticas para tareas próximas a vencer.

Características





Gestión de tareas: Crea, edita, elimina y marca tareas como completadas.



Interfaz gráfica: Interfaz intuitiva construida con Java Swing.



Base de datos MySQL: Almacena tareas de forma persistente.



Notificaciones: Alertas automáticas para tareas que están por vencer (en un plazo de 1 día).



Multihilo: Operaciones de base de datos ejecutadas en hilos separados para no bloquear la interfaz.



Personalización visual: Colores dinámicos para indicar el estado de las tareas (completada, pendiente, vencida).

Requisitos





Java: JDK 8 o superior.



MySQL: Servidor MySQL 5.7 o superior.



Conector JDBC: mysql-connector-java (puedes descargarlo desde Maven Repository).



Sistema operativo: Compatible con Windows, macOS o Linux.

Instalación





Clona el repositorio:

git clone https://github.com/tu-usuario/gestor-tareas-mysql.git
cd gestor-tareas-mysql



Configura la base de datos MySQL:





Crea una base de datos llamada gestor_tareas:

CREATE DATABASE gestor_tareas;



Asegúrate de que el usuario y contraseña en ManejadorBaseDeDatos.java coincidan con tu configuración de MySQL:

private static final String URL = "jdbc:mysql://localhost:3306/gestor_tareas";
private static final String USUARIO = "root";
private static final String CONTRASENA = "Whitelamp1";



La tabla tareas se creará automáticamente al iniciar la aplicación.



Añade el conector JDBC:





Descarga el conector MySQL (mysql-connector-java-x.x.xx.jar) desde Maven Repository.



Agrega el .jar al classpath de tu proyecto:





En IntelliJ IDEA: File > Project Structure > Libraries > + > Java > Selecciona el .jar.



En NetBeans: Properties > Libraries > Add JAR/Folder.



En Eclipse: Project > Properties > Java Build Path > Libraries > Add External JARs.



Compila y ejecuta:





Compila el proyecto:

javac -cp .:mysql-connector-java-x.x.xx.jar proyectointegradore3/*.java



Ejecuta la aplicación:

java -cp .:mysql-connector-java-x.x.xx.jar proyectointegradore3.Main

(En Windows, reemplaza : por ; en los comandos anteriores).

Uso





Inicia la aplicación ejecutando Main.java.



Interfaz principal:





Agregar tarea: Ingresa título, descripción y fecha límite (formato AAAA-MM-DD) y haz clic en "Agregar Tarea".



Editar tarea: Selecciona una tarea en la tabla, haz clic en "Editar Tarea", modifica los campos y confirma con "ACEPTAR".



Eliminar tarea: Selecciona una tarea y haz clic en "Eliminar Tarea". Confirma con "SÍ".



Marcar como completada: Selecciona una o más tareas, haz clic en "Marcar Completadas" o usa el checkbox en la columna "Completada".



Notificaciones: Las tareas próximas a vencer (en 1 día) mostrarán alertas automáticas cada 30 segundos.



Cerrar la aplicación: Confirma la salida con "SÍ" en el diálogo de cierre.

Estructura del Proyecto

gestor-tareas-mysql/
├── proyectointegradore3/
│   ├── ManejadorBaseDeDatos.java  # Maneja la conexión y operaciones con MySQL
│   ├── Tarea.java                # Clase modelo para representar una tarea
│   ├── InterfazGestorTareas.java # Interfaz gráfica principal
│   ├── NotificadorTareas.java    # Hilo para notificaciones de tareas próximas a vencer
│   ├── TrabajadorTarea.java      # Hilo para operaciones de base de datos
│   ├── Main.java                 # Punto de entrada de la aplicación
├── README.md                     # Este archivo

Licencia

Este proyecto está licenciado bajo la Licencia Creative Commons Attribution 4.0 Internacional (CC-BY 4.0). Puedes usar, compartir y adaptar este trabajo siempre que des el crédito apropiado al autor original.

Detalles de la licencia:





Autor: [Tu Nombre o Alias]



Licencia completa: https://creativecommons.org/licenses/by/4.0/



Resumen: Eres libre de:





Compartir: Copiar y redistribuir el material en cualquier medio o formato.



Adaptar: Remezclar, transformar y construir sobre el material para cualquier propósito, incluso comercialmente.



Bajo las siguientes condiciones:





Atribución: Debes dar crédito al autor original, proporcionar un enlace a la licencia e indicar si se han realizado cambios.



Sin restricciones adicionales: No puedes aplicar términos legales o medidas tecnológicas que restrinjan legalmente a otros de hacer lo que la licencia permite.

Contribuciones

¡Las contribuciones son bienvenidas! Si deseas mejorar el proyecto:





Haz un fork del repositorio.



Crea una rama para tu característica (git checkout -b feature/nueva-caracteristica).



Realiza tus cambios y haz commit (git commit -m 'Añade nueva característica').



Sube tu rama (git push origin feature/nueva-caracteristica).



Abre un Pull Request en GitHub.

Contacto

Si tienes preguntas o sugerencias, contáctame en danielcastroherrera2004@gmail.com o abre un issue en el repositorio.



Desarrollado con ❤️ por Daniel826179 (Castro Herrera Daniel Esteban)
