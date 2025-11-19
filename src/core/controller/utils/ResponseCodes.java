
package core.controller.utils;

/**
 *
 * @author david
 */
public class ResponseCodes {
    // Éxito
    public static final int SUCCESS = 200;
    
    // Errores de Cliente (Entrada de Datos o Lógica de Negocio)
    public static final int INVALID_ARGUMENT = 400; // Generalmente para campos vacíos o formatos incorrectos
    public static final int ALREADY_EXISTS = 409;   // Si ya existe una entidad con ese ID/ISBN/etc.
    public static final int NOT_FOUND = 404;        // Si la entidad que se busca no existe (ej. Autor no existe)
    public static final int INVALID_ACTION = 403;   // Acción no permitida por la lógica de negocio (ej. Intentar comprar un Stand ya vendido)

    // Errores de Servidor (si aplicara, pero es bueno tener un rango)
    public static final int INTERNAL_SERVER_ERROR = 500;
}
