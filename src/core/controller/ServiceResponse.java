
package core.controller;
import core.controller.utils.ResponseCodes;
/**
 *
 * @author david
 */
public class ServiceResponse<T> {
    private final int code;
    private final String message;
    private final T data; // El objeto de datos (ej. un Autor creado, o una lista de Libros)

    // Constructor para respuestas exitosas (ej. 200 SUCCESS)
    public ServiceResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Constructor para respuestas de error (ej. 400 INVALID_ARGUMENT)
    public ServiceResponse(int code, String message) {
        this(code, message, null); // Llama al constructor completo con data = null
    }

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
