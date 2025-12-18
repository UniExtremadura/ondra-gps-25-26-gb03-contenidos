package com.ondra.contenidos.services;

import com.ondra.contenidos.clients.UsuariosClient;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.dao.Comentario;
import com.ondra.contenidos.models.enums.TipoContenido;
import com.ondra.contenidos.models.enums.TipoUsuario;
import com.ondra.contenidos.repositories.AlbumRepository;
import com.ondra.contenidos.repositories.CancionRepository;
import com.ondra.contenidos.repositories.ComentarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestión de comentarios en canciones y álbumes.
 *
 * <p>
 * Proporciona operaciones de creación, edición, eliminación y consulta de
 * comentarios.
 * Sincroniza automáticamente los datos de usuario con el microservicio de
 * usuarios.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final UsuariosClient usuariosClient;

    /**
     * Clase interna para almacenar datos del usuario obtenidos del microservicio de
     * usuarios.
     */
    private static class DatosUsuario {
        String nombre;
        String slug;
        String urlFotoPerfil;
    }

    /**
     * Crea un nuevo comentario en una canción o álbum.
     *
     * <p>
     * Valida el tipo de usuario y obtiene los datos actualizados desde el
     * microservicio
     * de usuarios. Si el usuario comenta como artista, utiliza el perfil de
     * artista.
     * </p>
     *
     * @param idUsuario   identificador del usuario autenticado (del token JWT)
     * @param idArtista   identificador del artista autenticado (del token JWT,
     *                    puede ser null)
     * @param tipoUsuario tipo de usuario (USUARIO o ARTISTA)
     * @param dto         datos del comentario a crear
     * @return comentario creado
     * @throws IllegalStateException    si el usuario es artista pero no tiene
     *                                  artistId
     * @throws CancionNotFoundException si la canción especificada no existe
     * @throws AlbumNotFoundException   si el álbum especificado no existe
     */
    @Transactional
    public ComentarioDTO crearComentario(Long idUsuario, Long idArtista, String tipoUsuario, CrearComentarioDTO dto) {
        log.debug("➕ Creando comentario - Usuario: {}, Artista: {}, Tipo: {}",
                idUsuario, idArtista, dto.getTipoContenido());

        TipoContenido tipo = TipoContenido.valueOf(dto.getTipoContenido().toUpperCase());
        TipoUsuario tipoUsuarioEnum = TipoUsuario.valueOf(tipoUsuario.toUpperCase());

        if (tipoUsuarioEnum == TipoUsuario.ARTISTA && idArtista == null) {
            throw new IllegalStateException("Usuario autenticado como artista pero sin artistId en el token");
        }

        Long idEntidad = (tipoUsuarioEnum == TipoUsuario.ARTISTA) ? idArtista : idUsuario;

        DatosUsuario datos = obtenerDatosUsuario(idEntidad, tipoUsuarioEnum);

        Comentario comentario = Comentario.builder()
                .idUsuario(idUsuario)
                .idArtista(idArtista)
                .tipoUsuario(tipoUsuarioEnum)
                .nombreUsuario(datos.nombre)
                .slugUsuario(datos.slug)
                .urlFotoPerfil(datos.urlFotoPerfil)
                .tipoContenido(tipo)
                .contenido(dto.getContenido())
                .build();

        if (tipo == TipoContenido.CANCIÓN) {
            Cancion cancion = cancionRepository.findById(dto.getIdCancion())
                    .orElseThrow(() -> new CancionNotFoundException(dto.getIdCancion()));
            comentario.setCancion(cancion);
        } else {
            Album album = albumRepository.findById(dto.getIdAlbum())
                    .orElseThrow(() -> new AlbumNotFoundException(dto.getIdAlbum()));
            comentario.setAlbum(album);
        }

        Comentario comentarioGuardado = comentarioRepository.save(comentario);
        return convertirADTO(comentarioGuardado);
    }

    /**
     * Edita el contenido de un comentario existente.
     *
     * <p>
     * Solo el autor del comentario puede editarlo. Actualiza también los datos
     * del usuario por si han cambiado desde la creación del comentario.
     * </p>
     *
     * @param idComentario identificador del comentario a editar
     * @param idUsuario    identificador del usuario autenticado
     * @param dto          datos actualizados del comentario
     * @return comentario actualizado
     * @throws ComentarioNotFoundException si el comentario no existe
     * @throws AccesoDenegadoException     si el usuario no es el autor
     */
    @Transactional
    public ComentarioDTO editarComentario(Long idComentario, Long idUsuario, EditarComentarioDTO dto) {
        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new ComentarioNotFoundException(idComentario));

        if (!comentario.getIdUsuario().equals(idUsuario)) {
            throw new AccesoDenegadoException("No tienes permiso para editar este comentario");
        }

        Long idEntidad = (comentario.getTipoUsuario() == TipoUsuario.ARTISTA)
                ? comentario.getIdArtista()
                : comentario.getIdUsuario();

        DatosUsuario datosActualizados = obtenerDatosUsuario(idEntidad, comentario.getTipoUsuario());

        comentario.setNombreUsuario(datosActualizados.nombre);
        comentario.setSlugUsuario(datosActualizados.slug);
        comentario.setUrlFotoPerfil(datosActualizados.urlFotoPerfil);
        comentario.setContenido(dto.getContenido());

        comentarioRepository.save(comentario);

        return convertirADTO(comentario);
    }

    /**
     * Lista los comentarios de una canción con paginación.
     *
     * <p>
     * Actualiza los datos de usuario de todos los comentarios antes de devolverlos
     * para sincronizar cambios de perfil.
     * </p>
     *
     * @param idCancion identificador de la canción
     * @param pagina    número de página (base 1)
     * @param limite    cantidad de elementos por página (máximo 100)
     * @return comentarios paginados ordenados por fecha descendente
     * @throws CancionNotFoundException si la canción no existe
     */
    @Transactional
    public ComentariosPaginadosDTO listarComentariosCancion(Long idCancion, Integer pagina, Integer limite) {
        if (!cancionRepository.existsById(idCancion)) {
            throw new CancionNotFoundException(idCancion);
        }

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        Page<Comentario> paginaComentarios = comentarioRepository.findByCancion(idCancion, pageable);

        List<Comentario> comentariosActualizados = actualizarYPersistirDatosUsuarios(paginaComentarios.getContent());

        return ComentariosPaginadosDTO.builder()
                .comentarios(comentariosActualizados.stream().map(this::convertirADTO).toList())
                .paginaActual(paginaComentarios.getNumber() + 1)
                .totalPaginas(paginaComentarios.getTotalPages())
                .totalElementos(paginaComentarios.getTotalElements())
                .elementosPorPagina(paginaComentarios.getSize())
                .build();
    }

    /**
     * Lista los comentarios de un álbum con paginación.
     *
     * <p>
     * Actualiza los datos de usuario de todos los comentarios antes de devolverlos
     * para sincronizar cambios de perfil.
     * </p>
     *
     * @param idAlbum identificador del álbum
     * @param pagina  número de página (base 1)
     * @param limite  cantidad de elementos por página (máximo 100)
     * @return comentarios paginados ordenados por fecha descendente
     * @throws AlbumNotFoundException si el álbum no existe
     */
    @Transactional
    public ComentariosPaginadosDTO listarComentariosAlbum(Long idAlbum, Integer pagina, Integer limite) {
        if (!albumRepository.existsById(idAlbum)) {
            throw new AlbumNotFoundException(idAlbum);
        }

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        Page<Comentario> paginaComentarios = comentarioRepository.findByAlbum(idAlbum, pageable);

        List<Comentario> comentariosActualizados = actualizarYPersistirDatosUsuarios(paginaComentarios.getContent());

        return ComentariosPaginadosDTO.builder()
                .comentarios(comentariosActualizados.stream().map(this::convertirADTO).toList())
                .paginaActual(paginaComentarios.getNumber() + 1)
                .totalPaginas(paginaComentarios.getTotalPages())
                .totalElementos(paginaComentarios.getTotalElements())
                .elementosPorPagina(paginaComentarios.getSize())
                .build();
    }

    /**
     * Lista todos los comentarios realizados por un usuario con paginación.
     *
     * <p>
     * Actualiza los datos de usuario de todos los comentarios antes de devolverlos
     * para sincronizar cambios de perfil.
     * </p>
     *
     * @param idUsuario identificador del usuario
     * @param pagina    número de página (base 1)
     * @param limite    cantidad de elementos por página (máximo 100)
     * @return comentarios paginados ordenados por fecha descendente
     */
    @Transactional
    public ComentariosPaginadosDTO listarComentariosUsuario(Long idUsuario, Integer pagina, Integer limite) {
        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        Page<Comentario> paginaComentarios = comentarioRepository.findByIdUsuario(idUsuario, pageable);

        List<Comentario> comentariosActualizados = actualizarYPersistirDatosUsuarios(paginaComentarios.getContent());

        return ComentariosPaginadosDTO.builder()
                .comentarios(comentariosActualizados.stream().map(this::convertirADTO).toList())
                .paginaActual(paginaComentarios.getNumber() + 1)
                .totalPaginas(paginaComentarios.getTotalPages())
                .totalElementos(paginaComentarios.getTotalElements())
                .elementosPorPagina(paginaComentarios.getSize())
                .build();
    }

    /**
     * Elimina un comentario.
     *
     * <p>
     * Puede eliminar el comentario el autor o el artista propietario del contenido
     * comentado.
     * </p>
     *
     * @param idComentario identificador del comentario a eliminar
     * @param idUsuario    identificador del usuario autenticado
     * @param tipoUsuario  tipo de usuario autenticado
     * @throws ComentarioNotFoundException si el comentario no existe
     * @throws AccesoDenegadoException     si el usuario no tiene permisos para
     *                                     eliminar
     */
    @Transactional
    public void eliminarComentario(Long idComentario, Long idUsuario, String tipoUsuario) {
        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new ComentarioNotFoundException(idComentario));

        boolean esAutor = comentario.getIdUsuario().equals(idUsuario);
        boolean esDuenoContenido = false;

        if ("ARTISTA".equalsIgnoreCase(tipoUsuario)) {
            if (comentario.getCancion() != null) {
                esDuenoContenido = comentario.getCancion().getIdArtista().equals(idUsuario);
            } else if (comentario.getAlbum() != null) {
                esDuenoContenido = comentario.getAlbum().getIdArtista().equals(idUsuario);
            }
        }

        if (!esAutor && !esDuenoContenido) {
            throw new AccesoDenegadoException("No tienes permiso para eliminar este comentario");
        }

        comentarioRepository.delete(comentario);
    }

    /**
     * Elimina todos los comentarios de un usuario.
     *
     * <p>
     * Utilizado cuando se elimina un usuario del sistema.
     * </p>
     *
     * @param idUsuario identificador del usuario
     */
    @Transactional
    public void eliminarTodosLosComentarios(Long idUsuario) {
        comentarioRepository.deleteByIdUsuario(idUsuario);
    }

    /**
     * Actualiza los datos de los usuarios en los comentarios listados.
     *
     * <p>
     * Sincroniza nombre, slug y foto de perfil con el microservicio de usuarios.
     * Solo persiste los comentarios que han cambiado para optimizar escrituras en
     * base de datos.
     * </p>
     *
     * @param comentarios lista de comentarios a actualizar
     * @return lista de comentarios con datos actualizados
     */
    private List<Comentario> actualizarYPersistirDatosUsuarios(List<Comentario> comentarios) {
        List<Comentario> comentariosActualizados = new ArrayList<>();

        for (Comentario comentario : comentarios) {
            try {
                Long idEntidad = (comentario.getTipoUsuario() == TipoUsuario.ARTISTA)
                        ? comentario.getIdArtista()
                        : comentario.getIdUsuario();

                DatosUsuario datosActualizados = obtenerDatosUsuario(idEntidad, comentario.getTipoUsuario());

                boolean cambios = false;

                if (!datosActualizados.nombre.equals(comentario.getNombreUsuario())) {
                    comentario.setNombreUsuario(datosActualizados.nombre);
                    cambios = true;
                }

                if (datosActualizados.slug != null &&
                        !datosActualizados.slug.equals(comentario.getSlugUsuario())) {
                    comentario.setSlugUsuario(datosActualizados.slug);
                    cambios = true;
                }

                if (datosActualizados.urlFotoPerfil != null &&
                        !datosActualizados.urlFotoPerfil.equals(comentario.getUrlFotoPerfil())) {
                    comentario.setUrlFotoPerfil(datosActualizados.urlFotoPerfil);
                    cambios = true;
                }

                if (cambios) {
                    comentarioRepository.save(comentario);
                }

            } catch (Exception e) {
                log.warn("⚠️ No se pudo actualizar datos del usuario/artista {} en comentario {}: {}",
                        comentario.getIdUsuario(), comentario.getIdComentario(), e.getMessage());
            }

            comentariosActualizados.add(comentario);
        }

        return comentariosActualizados;
    }

    /**
     * Obtiene los datos de un usuario o artista desde el microservicio de usuarios.
     *
     * <p>
     * En caso de error en la comunicación, retorna valores por defecto.
     * </p>
     *
     * @param idEntidad   identificador del usuario o artista
     * @param tipoUsuario tipo de usuario (USUARIO o ARTISTA)
     * @return datos del usuario con nombre, slug y foto de perfil
     */
    private DatosUsuario obtenerDatosUsuario(Long idEntidad, TipoUsuario tipoUsuario) {
        try {
            Map<String, Object> datosUsuario = usuariosClient.obtenerDatosUsuario(idEntidad, tipoUsuario.name());

            if (datosUsuario != null) {
                DatosUsuario datos = new DatosUsuario();
                datos.nombre = (String) datosUsuario.get("nombreCompleto");
                datos.slug = (String) datosUsuario.get("slug");
                datos.urlFotoPerfil = (String) datosUsuario.get("urlFoto");
                return datos;
            }

        } catch (Exception e) {
            log.warn("⚠️ Error al obtener datos del {} {}: {}",
                    tipoUsuario.name(), idEntidad, e.getMessage());
        }

        DatosUsuario fallback = new DatosUsuario();
        fallback.nombre = "Usuario Desconocido";
        fallback.slug = null;
        fallback.urlFotoPerfil = null;

        return fallback;
    }

    /**
     * Convierte una entidad Comentario a su representación DTO.
     *
     * <p>
     * Incluye información del contenido comentado y metadatos de edición.
     * </p>
     *
     * @param comentario entidad a convertir
     * @return DTO del comentario
     */
    private ComentarioDTO convertirADTO(Comentario comentario) {
        ComentarioDTO dto = ComentarioDTO.builder()
                .idComentario(comentario.getIdComentario())
                .idUsuario(comentario.getIdUsuario())
                .idArtista(comentario.getIdArtista())
                .tipoUsuario(comentario.getTipoUsuario().name())
                .nombreUsuario(comentario.getNombreUsuario())
                .slug(comentario.getSlugUsuario())
                .urlFotoPerfil(comentario.getUrlFotoPerfil())
                .tipoContenido(comentario.getTipoContenido().name())
                .contenido(comentario.getContenido())
                .fechaPublicacion(comentario.getFechaPublicacion())
                .fechaUltimaEdicion(comentario.getFechaUltimaEdicion())
                .editado(comentario.fueEditado())
                .build();

        if (comentario.getCancion() != null) {
            dto.setIdContenido(comentario.getCancion().getIdCancion());
            dto.setTituloContenido(comentario.getCancion().getTituloCancion());
            dto.setUrlPortada(comentario.getCancion().getUrlPortada());
        } else if (comentario.getAlbum() != null) {
            dto.setIdContenido(comentario.getAlbum().getIdAlbum());
            dto.setTituloContenido(comentario.getAlbum().getTituloAlbum());
            dto.setUrlPortada(comentario.getAlbum().getUrlPortada());
        }

        return dto;
    }
}