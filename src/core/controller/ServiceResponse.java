
package core.controller;
import core.controller.utils.ResponseCodes;
/**
 *
 * @author david
 */
/**
 * Representa una respuesta estándar retornada por los servicios del controlador.
 * Incluye un código de estado, un mensaje descriptivo y opcionalmente un objeto
 * de datos asociado al resultado de la operación.
 */
public class ServiceResponse<T> {
    private final int code;
    private final String message;
    private final T data; // El objeto de datos (ej. un Autor creado, o una lista de Libros)
    //Construye una respuesta completa con código, mensaje y datos./
    // Constructor para respuestas exitosas (ej. 200 SUCCESS)
    public ServiceResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

        /**
     * Construye una respuesta sin datos, utilizada para errores
     * o eventos donde no se retorna un objeto asociado.
     */
    public ServiceResponse(int code, String message) {
        this(code, message, null); // Llama al constructor completo con data = null
    }
    //Verifica si el código corresponde a una operación exitosa.
    public boolean isSuccess() {
        return this.code == ResponseCodes.SUCCESS;
    }

    // Getters para acceder a los campos (omito setters para inmutabilidad)
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
