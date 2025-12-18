package com.ondra.contenidos.services;

import com.ondra.contenidos.data.ArtistasContenidoData;
import com.ondra.contenidos.models.dao.*;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.models.enums.TipoContenido;
import com.ondra.contenidos.models.enums.TipoUsuario;
import com.ondra.contenidos.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de población inicial de la base de datos con contenido musical y datos de usuarios.
 *
 * <p>Crea canciones, álbumes, favoritos, compras, valoraciones y comentarios para usuarios
 * y artistas. Mantiene coherencia con los IDs del microservicio de usuarios.</p>
 *
 * <p>Mapeo de usuarios:</p>
 * <ul>
 *   <li>Usuarios normales: IDs de usuario 1-5</li>
 *   <li>Artistas: IDs de artista 1-6, IDs de usuario 6-11</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Profile({"dev", "docker"})
public class ContenidosDataSeederService implements CommandLineRunner {

    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final AlbumCancionRepository albumCancionRepository;
    private final FavoritoRepository favoritoRepository;
    private final CompraRepository compraRepository;
    private final ComentarioRepository comentarioRepository;
    private final ValoracionRepository valoracionRepository;
    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;

    @Value("${seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${seed.user-data.enabled:true}")
    private boolean seedUserDataEnabled;

    @Value("${seed.artist-interactions.enabled:false}")
    private boolean seedArtistInteractionsEnabled;

    private final Random random = new Random();

    private static final List<Long> USUARIOS_NORMALES = Arrays.asList(1L, 2L, 3L, 4L, 5L);
    private static final List<Long> USUARIOS_ARTISTAS = Arrays.asList(6L, 7L, 8L, 9L, 10L, 11L);

    private static final Map<Long, String> NOMBRES_USUARIOS = Map.of(
            1L, "Ana García",
            2L, "Carlos Martínez",
            3L, "Laura Rodríguez",
            4L, "Miguel Fernández",
            5L, "Sara González"
    );

    private static final Map<Long, String> SLUGS_USUARIOS = Map.of(
            1L, "anagarcia",
            2L, "carlosmartinez",
            3L, "laurarodriguez",
            4L, "miguelfernandez",
            5L, "saragonzalez"
    );

    private static final Map<String, Long> CARPETA_A_ID_ARTISTA = Map.of(
            "duki", 1L,
            "aitana", 2L,
            "avicii", 3L,
            "daddyyankee", 4L,
            "rosalia", 5L,
            "sanguijuelasdelguadiana", 6L
    );

    private static final Map<Long, Long> ID_ARTISTA_A_ID_USUARIO = Map.of(
            1L, 6L,   // Duki
            2L, 7L,   // Aitana
            3L, 8L,   // Avicii
            4L, 9L,   // Daddy Yankee
            5L, 10L,  // Rosalía
            6L, 11L   // Sanguijuelas del Guadiana
    );

    private static final Map<Long, String> NOMBRES_ARTISTAS = Map.of(
            6L, "Duki",
            7L, "Aitana",
            8L, "Avicii",
            9L, "Daddy Yankee",
            10L, "Rosalía",
            11L, "Sanguijuelas del Guadiana"
    );

    private static final Map<Long, String> SLUGS_ARTISTAS = Map.of(
            6L, "duki",
            7L, "aitana",
            8L, "avicii",
            9L, "daddyyankee",
            10L, "rosalia",
            11L, "sanguijuelasdelguadiana"
    );

    private final List<Long> cancionesCreadas = new ArrayList<>();
    private final List<Long> albumesCreados = new ArrayList<>();
    private final Map<Long, List<Long>> cancionesPorArtista = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("⏭️  Data seeding deshabilitado");
            return;
        }

        // Verificar si ya hay datos en la base de datos
        long cancionesExistentes = cancionRepository.count();
        if (cancionesExistentes > 0) {
            log.info("✅ Base de datos ya contiene {} canciones. Seeding omitido (datos ya existen)", cancionesExistentes);
            return;
        }

        log.info("🚀 Iniciando población de contenido musical...");
        log.info("📋 Usuarios normales: IDs 1-5 | Artistas: IDs artista 1-6, IDs usuario 6-11");
        log.info("📸 Usando URLs compartidas (sin subir archivos)");

        try {
            int totalCanciones = 0;
            int totalAlbumes = 0;
            int totalSingles = 0;

            for (ArtistasContenidoData.ArtistaContenido artista : ArtistasContenidoData.ARTISTAS) {
                Long idArtista = CARPETA_A_ID_ARTISTA.get(artista.getCarpeta());

                if (idArtista == null) {
                    log.warn("⚠️  Artista sin mapeo: {}", artista.getCarpeta());
                    continue;
                }

                Long idUsuarioArtista = ID_ARTISTA_A_ID_USUARIO.get(idArtista);
                cancionesPorArtista.put(idArtista, new ArrayList<>());

                log.info("🎤 Procesando '{}' - ID Artista: {}, ID Usuario: {}",
                        artista.getCarpeta(), idArtista, idUsuarioArtista);

                for (ArtistasContenidoData.AlbumInfo albumInfo : artista.getAlbumes()) {
                    try {
                        Album album = crearAlbum(idArtista, albumInfo);
                        albumesCreados.add(album.getIdAlbum());
                        totalAlbumes++;
                        totalCanciones += albumInfo.getCanciones().size();
                        log.info("✅ Álbum '{}' con {} canciones", album.getTituloAlbum(),
                                albumInfo.getCanciones().size());
                    } catch (Exception e) {
                        log.error("❌ Error en álbum '{}': {}", albumInfo.getTitulo(), e.getMessage());
                    }
                }

                for (ArtistasContenidoData.SingleInfo singleInfo : artista.getSingles()) {
                    try {
                        Cancion cancion = crearSingle(idArtista, singleInfo);
                        cancionesCreadas.add(cancion.getIdCancion());
                        cancionesPorArtista.get(idArtista).add(cancion.getIdCancion());
                        totalSingles++;
                        totalCanciones++;
                        log.info("✅ Single '{}'", singleInfo.getTitulo());
                    } catch (Exception e) {
                        log.error("❌ Error en single '{}': {}", singleInfo.getTitulo(), e.getMessage());
                    }
                }
            }

            if (seedUserDataEnabled) {
                log.info("👥 Poblando datos de usuarios normales...");
                poblarDatosUsuarios();
            }

            if (seedArtistInteractionsEnabled) {
                log.info("🎤 Poblando interacciones de artistas...");
                poblarInteraccionesArtistas();
            }

            log.info("✅ Población completada");
            imprimirResumen(totalAlbumes, totalSingles, totalCanciones);

        } catch (Exception e) {
            log.error("❌ Error durante población: {}", e.getMessage(), e);
        }
    }

    /**
     * Crea datos de interacción para usuarios normales.
     */
    private void poblarDatosUsuarios() {
        int totalFavoritos = 0;
        int totalCompras = 0;
        int totalComentarios = 0;
        int totalValoraciones = 0;
        int totalCarrito = 0;

        for (Long idUsuario : USUARIOS_NORMALES) {
            try {
                Set<Long> cancionesFavoritas = seleccionarAleatorio(cancionesCreadas, 3, 8);
                Set<Long> albumesFavoritos = seleccionarAleatorio(albumesCreados, 2, 5);
                Set<Long> cancionesCompradas = seleccionarAleatorio(cancionesCreadas, 5, 15);
                Set<Long> albumesComprados = seleccionarAleatorio(albumesCreados, 1, 4);
                Set<Long> cancionesComentadas = seleccionarAleatorio(cancionesCreadas, 2, 6);
                Set<Long> albumesComentados = seleccionarAleatorio(albumesCreados, 1, 3);
                Set<Long> cancionesValoradas = seleccionarAleatorio(cancionesCreadas, 4, 10);
                Set<Long> albumesValorados = seleccionarAleatorio(albumesCreados, 2, 4);

                Set<Long> cancionesCarrito = seleccionarAleatorio(
                        cancionesCreadas.stream()
                                .filter(id -> !cancionesCompradas.contains(id))
                                .collect(Collectors.toList()),
                        0, 3
                );
                Set<Long> albumesCarrito = seleccionarAleatorio(
                        albumesCreados.stream()
                                .filter(id -> !albumesComprados.contains(id))
                                .collect(Collectors.toList()),
                        0, 2
                );

                totalFavoritos += crearFavoritos(idUsuario, cancionesFavoritas, albumesFavoritos);
                totalCompras += crearCompras(idUsuario, cancionesCompradas, albumesComprados);
                totalComentarios += crearComentarios(idUsuario, TipoUsuario.NORMAL,
                        NOMBRES_USUARIOS.get(idUsuario),
                        SLUGS_USUARIOS.get(idUsuario),
                        null,
                        cancionesComentadas, albumesComentados);
                totalValoraciones += crearValoraciones(idUsuario, TipoUsuario.NORMAL,
                        NOMBRES_USUARIOS.get(idUsuario), cancionesValoradas, albumesValorados);
                totalCarrito += crearCarrito(idUsuario, cancionesCarrito, albumesCarrito);

                log.info("✅ Usuario {} completado", NOMBRES_USUARIOS.get(idUsuario));

            } catch (Exception e) {
                log.error("❌ Error en usuario {}: {}", idUsuario, e.getMessage());
            }
        }

        log.info("📊 Usuarios: {} favoritos, {} compras, {} comentarios, {} valoraciones, {} items carrito",
                totalFavoritos, totalCompras, totalComentarios, totalValoraciones, totalCarrito);
    }

    /**
     * Crea interacciones de artistas sobre contenido de otros artistas.
     */
    private void poblarInteraccionesArtistas() {
        int totalComentarios = 0;
        int totalValoraciones = 0;

        for (Long idUsuarioArtista : USUARIOS_ARTISTAS) {
            try {
                Long idArtista = ID_ARTISTA_A_ID_USUARIO.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(idUsuarioArtista))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse(null);

                if (idArtista == null) continue;

                List<Long> cancionesOtrosArtistas = cancionesCreadas.stream()
                        .filter(id -> !cancionesPorArtista.getOrDefault(idArtista, Collections.emptyList()).contains(id))
                        .collect(Collectors.toList());

                Set<Long> cancionesComentadas = seleccionarAleatorio(cancionesOtrosArtistas, 1, 3);
                Set<Long> albumesComentados = seleccionarAleatorio(albumesCreados, 0, 2);
                Set<Long> cancionesValoradas = seleccionarAleatorio(cancionesOtrosArtistas, 2, 5);
                Set<Long> albumesValorados = seleccionarAleatorio(albumesCreados, 1, 3);

                String nombreArtista = NOMBRES_ARTISTAS.get(idUsuarioArtista);

                totalComentarios += crearComentarios(idUsuarioArtista, TipoUsuario.ARTISTA,
                        nombreArtista,
                        SLUGS_ARTISTAS.get(idUsuarioArtista),
                        null,
                        cancionesComentadas, albumesComentados);
                totalValoraciones += crearValoraciones(idUsuarioArtista, TipoUsuario.ARTISTA,
                        nombreArtista, cancionesValoradas, albumesValorados);

                log.info("✅ Artista {} completado", nombreArtista);

            } catch (Exception e) {
                log.error("❌ Error en artista {}: {}", idUsuarioArtista, e.getMessage());
            }
        }

        log.info("📊 Artistas: {} comentarios, {} valoraciones", totalComentarios, totalValoraciones);
    }

    /**
     * Crea favoritos para un usuario.
     */
    private int crearFavoritos(Long idUsuario, Set<Long> cancionesIds, Set<Long> albumesIds) {
        int count = 0;

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                Favorito favorito = Favorito.builder()
                        .idUsuario(idUsuario)
                        .tipoContenido(TipoContenido.CANCIÓN)
                        .cancion(cancionOpt.get())
                        .fechaAgregado(generarFechaAleatoria())
                        .build();
                favoritoRepository.save(favorito);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                Favorito favorito = Favorito.builder()
                        .idUsuario(idUsuario)
                        .tipoContenido(TipoContenido.ÁLBUM)
                        .album(albumOpt.get())
                        .fechaAgregado(generarFechaAleatoria())
                        .build();
                favoritoRepository.save(favorito);
                count++;
            }
        }

        return count;
    }

    /**
     * Crea compras para un usuario.
     */
    private int crearCompras(Long idUsuario, Set<Long> cancionesIds, Set<Long> albumesIds) {
        int count = 0;

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                Cancion cancion = cancionOpt.get();
                Compra compra = Compra.builder()
                        .idUsuario(idUsuario)
                        .tipoContenido(TipoContenido.CANCIÓN)
                        .cancion(cancion)
                        .precioPagado(BigDecimal.valueOf(cancion.getPrecioCancion()))
                        .fechaCompra(generarFechaAleatoria())
                        .build();
                compraRepository.save(compra);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                Album album = albumOpt.get();
                Compra compra = Compra.builder()
                        .idUsuario(idUsuario)
                        .tipoContenido(TipoContenido.ÁLBUM)
                        .album(album)
                        .precioPagado(BigDecimal.valueOf(album.getPrecioAlbum()))
                        .fechaCompra(generarFechaAleatoria())
                        .build();
                compraRepository.save(compra);
                count++;
            }
        }

        return count;
    }

    /**
     * Crea comentarios de usuarios o artistas sobre contenido musical.
     */
    private int crearComentarios(Long idUsuario, TipoUsuario tipoUsuario, String nombreUsuario,
                                 String slugUsuario, String urlFotoPerfil,
                                 Set<Long> cancionesIds, Set<Long> albumesIds) {
        int count = 0;

        String[] comentariosUsuarios = {
                "¡Me encanta esta canción! La escucho todos los días.",
                "Increíble trabajo, definitivamente uno de mis favoritos.",
                "La mejor del álbum sin duda.",
                "No me canso de escucharla, brutal.",
                "Tiene un ritmo increíble, perfecta para el gym.",
                "Las letras son muy profundas, me identifico mucho.",
                "Obra maestra, nada más que decir.",
                "La mejor parte es cuando se acaba",
                "Una victoria más para los sordos.",
                "No es mi estilo pero respeto el trabajo.",
                "Muy buena producción, se nota la calidad."
        };

        String[] comentariosArtistas = {
                "Gran trabajo, se nota el esfuerzo en la producción.",
                "Respeto mucho tu arte, sigue así.",
                "Me inspira tu forma de crear música.",
                "Colaboraríamos bien juntos, excelente track.",
                "La mezcla está increíble, ¿qué setup usaste?",
                "Brutal hermano, esto es de otro nivel.",
                "Se nota la evolución, felicidades.",
                "Esto es lo que necesita la escena, fuego."
        };

        String[] comentarios = tipoUsuario == TipoUsuario.ARTISTA ? comentariosArtistas : comentariosUsuarios;

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                Cancion cancion = cancionOpt.get();
                String texto = comentarios[random.nextInt(comentarios.length)];

                Comentario comentario = Comentario.builder()
                        .idUsuario(idUsuario)
                        .tipoUsuario(tipoUsuario)
                        .nombreUsuario(nombreUsuario)
                        .slugUsuario(slugUsuario)
                        .urlFotoPerfil(urlFotoPerfil)
                        .tipoContenido(TipoContenido.CANCIÓN)
                        .cancion(cancion)
                        .idArtista(cancion.getIdArtista())
                        .contenido(texto)
                        .fechaPublicacion(generarFechaAleatoria())
                        .build();

                if (random.nextDouble() < 0.2) {
                    comentario.setFechaUltimaEdicion(comentario.getFechaPublicacion().plusHours(random.nextInt(48)));
                }

                comentarioRepository.save(comentario);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                Album album = albumOpt.get();
                String texto = comentarios[random.nextInt(comentarios.length)];

                Comentario comentario = Comentario.builder()
                        .idUsuario(idUsuario)
                        .tipoUsuario(tipoUsuario)
                        .nombreUsuario(nombreUsuario)
                        .slugUsuario(slugUsuario)
                        .urlFotoPerfil(urlFotoPerfil)
                        .tipoContenido(TipoContenido.ÁLBUM)
                        .album(album)
                        .idArtista(album.getIdArtista())
                        .contenido(texto)
                        .fechaPublicacion(generarFechaAleatoria())
                        .build();

                if (random.nextDouble() < 0.2) {
                    comentario.setFechaUltimaEdicion(comentario.getFechaPublicacion().plusHours(random.nextInt(48)));
                }

                comentarioRepository.save(comentario);
                count++;
            }
        }

        return count;
    }

    /**
     * Crea valoraciones de usuarios o artistas sobre contenido musical.
     */
    private int crearValoraciones(Long idUsuario, TipoUsuario tipoUsuario, String nombreUsuario,
                                  Set<Long> cancionesIds, Set<Long> albumesIds) {
        int count = 0;

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                int puntuacion = generarPuntuacion(tipoUsuario);

                Valoracion valoracion = Valoracion.builder()
                        .idUsuario(idUsuario)
                        .tipoUsuario(tipoUsuario)
                        .nombreUsuario(nombreUsuario)
                        .tipoContenido(TipoContenido.CANCIÓN)
                        .cancion(cancionOpt.get())
                        .valor(puntuacion)
                        .fechaValoracion(generarFechaAleatoria())
                        .build();
                valoracionRepository.save(valoracion);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                int puntuacion = generarPuntuacion(tipoUsuario);

                Valoracion valoracion = Valoracion.builder()
                        .idUsuario(idUsuario)
                        .tipoUsuario(tipoUsuario)
                        .nombreUsuario(nombreUsuario)
                        .tipoContenido(TipoContenido.ÁLBUM)
                        .album(albumOpt.get())
                        .valor(puntuacion)
                        .fechaValoracion(generarFechaAleatoria())
                        .build();
                valoracionRepository.save(valoracion);
                count++;
            }
        }

        return count;
    }

    /**
     * Genera una puntuación según el tipo de usuario.
     */
    private int generarPuntuacion(TipoUsuario tipoUsuario) {
        double rand = random.nextDouble();

        if (tipoUsuario == TipoUsuario.ARTISTA) {
            if (rand < 0.60) return 5;
            if (rand < 0.90) return 4;
            return 3;
        } else {
            if (rand < 0.50) return 5;
            if (rand < 0.80) return 4;
            if (rand < 0.95) return 3;
            if (rand < 0.98) return 2;
            return 1;
        }
    }

    /**
     * Crea items en el carrito para un usuario.
     */
    private int crearCarrito(Long idUsuario, Set<Long> cancionesIds, Set<Long> albumesIds) {
        if (cancionesIds.isEmpty() && albumesIds.isEmpty()) {
            return 0;
        }

        int count = 0;

        Optional<Carrito> carritoOpt = carritoRepository.findByIdUsuario(idUsuario);
        Carrito carrito = carritoOpt.orElseGet(() -> carritoRepository.save(
                Carrito.builder().idUsuario(idUsuario).build()
        ));

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                Cancion cancion = cancionOpt.get();
                Long idArtista = cancion.getIdArtista();
                Long idUsuarioArtista = ID_ARTISTA_A_ID_USUARIO.get(idArtista);
                String nombreArtista = NOMBRES_ARTISTAS.get(idUsuarioArtista);
                String slugArtista = SLUGS_ARTISTAS.get(idUsuarioArtista);

                CarritoItem item = CarritoItem.builder()
                        .carrito(carrito)
                        .cancion(cancion)
                        .tipoProducto(CarritoItem.TipoProducto.CANCIÓN)
                        .precio(BigDecimal.valueOf(cancion.getPrecioCancion()))
                        .titulo(cancion.getTituloCancion())
                        .urlPortada(cancion.getUrlPortada())
                        .nombreArtistico(nombreArtista != null ? nombreArtista : "Artista Desconocido")
                        .slugArtista(slugArtista)
                        .fechaAgregado(generarFechaAleatoria())
                        .build();

                carritoItemRepository.save(item);
                carrito.agregarItem(item);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                Album album = albumOpt.get();
                Long idArtista = album.getIdArtista();
                Long idUsuarioArtista = ID_ARTISTA_A_ID_USUARIO.get(idArtista);
                String nombreArtista = NOMBRES_ARTISTAS.get(idUsuarioArtista);
                String slugArtista = SLUGS_ARTISTAS.get(idUsuarioArtista);

                CarritoItem item = CarritoItem.builder()
                        .carrito(carrito)
                        .album(album)
                        .tipoProducto(CarritoItem.TipoProducto.ÁLBUM)
                        .precio(BigDecimal.valueOf(album.getPrecioAlbum()))
                        .titulo(album.getTituloAlbum())
                        .urlPortada(album.getUrlPortada())
                        .nombreArtistico(nombreArtista != null ? nombreArtista : "Artista Desconocido")
                        .slugArtista(slugArtista)
                        .fechaAgregado(generarFechaAleatoria())
                        .build();

                carritoItemRepository.save(item);
                carrito.agregarItem(item);
                count++;
            }
        }

        if (count > 0) {
            carritoRepository.save(carrito);
        }

        return count;
    }

    /**
     * Selecciona aleatoriamente elementos de una lista.
     */
    private Set<Long> seleccionarAleatorio(List<Long> lista, int min, int max) {
        if (lista.isEmpty()) {
            return new HashSet<>();
        }

        int cantidad = min + random.nextInt(Math.min(max - min + 1, lista.size()));
        Set<Long> seleccionados = new HashSet<>();
        List<Long> copia = new ArrayList<>(lista);
        Collections.shuffle(copia);

        for (int i = 0; i < Math.min(cantidad, copia.size()); i++) {
            seleccionados.add(copia.get(i));
        }

        return seleccionados;
    }

    /**
     * Genera una fecha aleatoria dentro del último año.
     */
    private LocalDateTime generarFechaAleatoria() {
        return LocalDateTime.now().minusDays(random.nextInt(365));
    }

    /**
     * Imprime resumen de contenido creado.
     */
    private void imprimirResumen(int totalAlbumes, int totalSingles, int totalCanciones) {
        log.info("📊 RESUMEN:");
        log.info("   • Artistas: {}", ArtistasContenidoData.ARTISTAS.size());
        log.info("   • Álbumes: {}", totalAlbumes);
        log.info("   • Singles: {}", totalSingles);
        log.info("   • Canciones totales: {}", totalCanciones);
    }

    /**
     * Crea un álbum con sus canciones asociadas usando URLs compartidas.
     */
    private Album crearAlbum(Long idArtista, ArtistasContenidoData.AlbumInfo albumInfo) {
        String urlPortada = albumInfo.getUrlPortadaCompartida();

        Album album = Album.builder()
                .tituloAlbum(albumInfo.getTitulo())
                .idArtista(idArtista)
                .genero(GeneroMusical.fromId(albumInfo.getIdGenero()))
                .precioAlbum(albumInfo.getPrecio())
                .urlPortada(urlPortada)
                .descripcion(albumInfo.getDescripcion())
                .fechaPublicacion(LocalDateTime.of(albumInfo.getAnio(), 1, 1, 0, 0))
                .build();

        album = albumRepository.save(album);
        log.debug("  ✓ Álbum '{}' guardado con URL compartida", album.getTituloAlbum());

        for (ArtistasContenidoData.CancionInfo cancionInfo : albumInfo.getCanciones()) {
            try {
                Cancion cancion = crearCancion(idArtista, cancionInfo, urlPortada);
                cancionesCreadas.add(cancion.getIdCancion());
                cancionesPorArtista.get(idArtista).add(cancion.getIdCancion());

                AlbumCancion albumCancion = AlbumCancion.builder()
                        .album(album)
                        .cancion(cancion)
                        .numeroPista(cancionInfo.getNumeroPista())
                        .build();

                albumCancionRepository.save(albumCancion);
                log.debug("  ✓ Pista {}: {}", cancionInfo.getNumeroPista(), cancion.getTituloCancion());

            } catch (Exception e) {
                log.warn("  ⚠️  Error en canción '{}': {}", cancionInfo.getTitulo(), e.getMessage());
            }
        }

        return album;
    }

    /**
     * Crea un single independiente usando URLs compartidas.
     */
    private Cancion crearSingle(Long idArtista, ArtistasContenidoData.SingleInfo singleInfo) {
        String urlPortada = singleInfo.getUrlPortadaCompartida();
        String urlAudio = singleInfo.getUrlAudioCompartida();

        Cancion cancion = Cancion.builder()
                .tituloCancion(singleInfo.getTitulo())
                .idArtista(idArtista)
                .genero(GeneroMusical.fromId(singleInfo.getIdGenero()))
                .precioCancion(singleInfo.getPrecio())
                .duracionSegundos(180)
                .urlPortada(urlPortada)
                .urlAudio(urlAudio)
                .descripcion(singleInfo.getDescripcion())
                .reproducciones(generarReproduccionesAleatorias())
                .fechaPublicacion(LocalDateTime.now().minusDays(random.nextInt(365)))
                .build();

        log.debug("  ✓ Single '{}' creado con URLs compartidas", singleInfo.getTitulo());
        return cancionRepository.save(cancion);
    }

    /**
     * Crea una canción perteneciente a un álbum usando URLs compartidas.
     */
    private Cancion crearCancion(Long idArtista,
                                 ArtistasContenidoData.CancionInfo cancionInfo,
                                 String urlPortadaAlbum) {
        String urlAudio = cancionInfo.getUrlAudioCompartida();

        Cancion cancion = Cancion.builder()
                .tituloCancion(cancionInfo.getTitulo())
                .idArtista(idArtista)
                .genero(GeneroMusical.fromId(cancionInfo.getIdGenero()))
                .precioCancion(cancionInfo.getPrecio())
                .duracionSegundos(180)
                .urlPortada(urlPortadaAlbum)
                .urlAudio(urlAudio)
                .descripcion("Pista " + cancionInfo.getNumeroPista())
                .reproducciones(generarReproduccionesAleatorias())
                .fechaPublicacion(LocalDateTime.now().minusDays(random.nextInt(365)))
                .build();

        return cancionRepository.save(cancion);
    }

    /**
     * Genera un número aleatorio de reproducciones siguiendo una distribución realista.
     */
    private Long generarReproduccionesAleatorias() {
        double probabilidad = random.nextDouble();

        if (probabilidad < 0.40) {
            return 1_000L + random.nextInt(9_000);
        } else if (probabilidad < 0.75) {
            return 10_000L + random.nextInt(90_000);
        } else if (probabilidad < 0.95) {
            return 100_000L + random.nextInt(900_000);
        } else {
            return 1_000_000L + random.nextInt(9_000_000);
        }
    }
}