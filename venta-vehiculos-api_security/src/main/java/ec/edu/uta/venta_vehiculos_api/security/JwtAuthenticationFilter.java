package ec.edu.uta.venta_vehiculos_api.security;



// Importa Claims.
//
// Claims representa los datos que están
// almacenados dentro del payload del JWT.
//
// Ejemplo:
// sub
// scope
// idapli
// tipo
// iat
// exp
import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


// Registra esta clase como un componente de Spring.
@Component


// Declara nuestro filtro personalizado.
public class JwtAuthenticationFilter


        // Hereda de OncePerRequestFilter.
        //
        // Esto significa que Spring ejecutará
        // este filtro una vez por cada petición HTTP.
        extends OncePerRequestFilter {


    // Dependencia que utilizaremos
    // para validar el JWT recibido.
    private final JwtService jwtService;


    // Constructor de la clase.
    //
    // Spring utiliza este constructor
    // para inyectar JwtService automáticamente.
    public JwtAuthenticationFilter(
            JwtService jwtService) {


        // Guarda el servicio recibido
        // dentro del atributo de la clase.
        this.jwtService = jwtService;
    }


    // Indica que estamos sobrescribiendo
    // un método definido en OncePerRequestFilter.
    @Override


    // Método principal del filtro.
    //
    // Este método se ejecutará
    // cuando llegue una petición HTTP.
    protected void doFilterInternal(


            // Representa la petición HTTP.
            HttpServletRequest request,


            // Representa la respuesta HTTP.
            HttpServletResponse response,


            // Permite continuar
            // con los demás filtros.
            FilterChain filterChain)


            // Indica las excepciones
            // que este método puede lanzar.
            throws ServletException, IOException {


        // Busca el encabezado HTTP:
        //
        // Authorization
        //
        // Ejemplo:
        //
        // Authorization: Bearer eyJhbGciOi...
        String authorization =
                request.getHeader("Authorization");


        // Comprueba dos situaciones:
        //
        // 1. Authorization no existe.
        //
        // O
        //
        // 2. Authorization existe,
        //    pero no comienza con "Bearer ".
        if (authorization == null ||

                !authorization.startsWith("Bearer ")) {


            // Si no encontramos Bearer Token,
            // este filtro NO autentica al usuario.
            //
            // Simplemente continúa
            // hacia el siguiente filtro.
            filterChain.doFilter(
                    request,
                    response
            );


            // Termina la ejecución
            // de este método.
            //
            // Esto evita continuar intentando
            // extraer un token que no existe.
            return;
        }


        // Si llegamos aquí significa que tenemos:
        //
        // Authorization: Bearer TOKEN
        //
        // substring(7) elimina exactamente:
        //
        // "Bearer "
        //
        // porque:
        //
        // Bearer = 6 caracteres
        // espacio = 1
        //
        // total = 7.
        String token =
                authorization.substring(7);


        // Inicia un bloque try.
        //
        // Intentaremos validar el JWT.
        //
        // Si el token:
        //
        // - está expirado
        // - tiene firma incorrecta
        // - tiene formato incorrecto
        //
        // JwtService puede lanzar una excepción.
        try {


            // Envía el token a JwtService.
            //
            // validarToken() verifica el JWT.
            //
            // Si es válido,
            // devuelve sus Claims.
            Claims claims =
                    jwtService.validarToken(token);


            // Obtiene el subject del JWT.
            //
            // En JwtService anteriormente hicimos:
            //
            // .subject(username)
            //
            // Por eso aquí podemos recuperar:
            //
            // username
            String username =
                    claims.getSubject();


            // Obtiene el claim personalizado:
            //
            // "scope"
            //
            // y solicita que sea convertido a String.
            //
            // En nuestro ejemplo:
            //
            // scope = "secret"
            
            /*
            String scope =
                    claims.get(
                            "scope",
                            String.class
                    );
                    */
                String rol =
                        claims.get(
                                "rol",
                                String.class
                        );


            // Declaramos un objeto Authentication.
            //
            // Este objeto será el que Spring Security
            // considere como la identidad autenticada.
            UsernamePasswordAuthenticationToken authentication =


                    // Creamos la autenticación.
                    new UsernamePasswordAuthenticationToken(


                            // PRIMER PARÁMETRO:
                            //
                            // principal.
                            //
                            // Representa quién está autenticado.
                            //
                            // En este ejemplo:
                            //
                            // username = 9999999999
                            username,


                            // SEGUNDO PARÁMETRO:
                            //
                            // credentials.
                            //
                            // Aquí ponemos null porque
                            // la autenticación ya fue comprobada
                            // utilizando el JWT.
                            //
                            // No necesitamos conservar
                            // la contraseña.
                            null,


                            // TERCER PARÁMETRO:
                            //
                            // authorities.
                            //
                            // Representa los permisos
                            // asociados al usuario/token.
                            List.of(


                                    // Creamos una autoridad
                                    // reconocida por Spring Security.
                                    new SimpleGrantedAuthority(


                                            // Convertimos:
                                            //
                                            // scope = "secret"
                                            //
                                            // en:
                                            //
                                            // SCOPE_secret
                                            "ROLE_" + rol
                                    )
                            )
                    );


            // Obtiene el contexto de seguridad
            // de la petición actual.
            SecurityContextHolder
                    .getContext()


                    // Guarda dentro del contexto
                    // la Authentication que acabamos de crear.
                    //
                    // Desde este momento,
                    // Spring Security considera
                    // que la petición está autenticada.
                    .setAuthentication(authentication);


        // Si ocurrió cualquier error
        // durante la validación del JWT,
        // entraremos aquí.
        } catch (Exception e) {


            // Limpia el contexto de seguridad.
            //
            // Esto garantiza que la petición
            // quede SIN autenticación válida.
            SecurityContextHolder.clearContext();
        }


        // Después de intentar procesar el token,
        // la petición continúa por la cadena
        // de filtros de Spring Security.
        //
        // Spring Security decidirá después
        // si la ruta puede continuar o debe bloquearse.
        filterChain.doFilter(
                request,
                response
        );
    }
}