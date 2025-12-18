package com.ondra.contenidos.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * Datos predefinidos de artistas con sus álbumes y canciones para inicialización de la base de datos.
 *
 * <p>Contiene información estructurada de artistas, incluyendo álbumes, singles y canciones asociadas.
 * Los identificadores de artista deben coincidir con los registrados en el microservicio de usuarios.</p>
 */
public class ArtistasContenidoData {

    /**
     * Lista de artistas con su contenido musical predefinido.
     *
     * <p>Incluye artistas de diversos géneros: pop, trap latino, rock alternativo,
     * EDM, flamenco urbano y reggaetón.</p>
     */
    public static final List<ArtistaContenido> ARTISTAS = Arrays.asList(
            new ArtistaContenido(
                    2L,
                    "aitana",
                    Arrays.asList(
                            new AlbumInfo("Alpha", 2L, 2022, 19.99,
                                    "Álbum debut de Aitana con sonidos pop frescos y letras personales.",
                                    Arrays.asList(
                                            new CancionInfo("Los Ángeles", 1, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203010/01_losangeles_s2bjre.mp3"),
                                            new CancionInfo("LAS BABYS", 2, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203017/02_lasbabys_edvpit.mp3"),
                                            new CancionInfo("Darari", 3, 29L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203019/03_darari_wpzcyl.mp3"),
                                            new CancionInfo("AQYNE", 4, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203021/04_aqyne_rjazqj.mp3"),
                                            new CancionInfo("miamor", 5, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203023/05_miamor_wwyctx.mp3"),
                                            new CancionInfo("Formentera", 6, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203027/06_formentera_tzouxa.mp3"),
                                            new CancionInfo("En El Coche", 7, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203025/07_enelcoche_hkuwt0.mp3")
                                    ),
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996438/alpha_g3u9em.jpg"
                            ),
                            new AlbumInfo("Cuarto Azul", 2L, 2020, 17.99,
                                    "Reedición de su primer disco con nuevos temas que marcaron su carrera.",
                                    Arrays.asList(
                                            new CancionInfo("6 DE FEBRERO", 1, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203421/01_6defebrero_ibw712.mp3"),
                                            new CancionInfo("SEGUNDO INTENTO", 2, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203423/02_segundointento_ejd5yr.mp3"),
                                            new CancionInfo("CUANDO HABLES CON ÉL", 3, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203425/03_cuandohablesconel_wpxmh6.mp3"),
                                            new CancionInfo("SUPERESTRELLA", 4, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203428/04_superestrella_ftpprh.mp3"),
                                            new CancionInfo("CONEXIÓN PSÍQUICA", 5, 2L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203429/05_conexionpsiquica_sfrxya.mp3")
                                    ),
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996437/cuartoazul_pulgxz.jpg"
                            )
                    )
            ),

            new ArtistaContenido(
                    1L,
                    "duki",
                    Arrays.asList(
                            new AlbumInfo("Ameri", 28L, 2023, 22.99,
                                    "Álbum que consolida a Duki como uno de los referentes del trap latino.",
                                    Arrays.asList(
                                            new CancionInfo("Nueva Era", 1, 28L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203641/01_nuevaera_s0v6px.mp3"),
                                            new CancionInfo("Brindis", 2, 28L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203642/02_brindis_kfa0tc.mp3"),
                                            new CancionInfo("Hardaway", 3, 28L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203645/03_hardaway_r6bfrp.mp3"),
                                            new CancionInfo("Ameri", 4, 28L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203647/04_ameri_kmxz9b.mp3")
                                    ),
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996291/ameri_kgqmuo.jpg"
                            ),
                            new AlbumInfo("Desde el Fin del Mundo", 28L, 2021, 21.99,
                                    "Álbum conceptual de Duki que explora trap melódico y letras introspectivas.",
                                    Arrays.asList(
                                            new CancionInfo("Sudor y Trabajo", 1, 28L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203814/01_sudorytrabajo_uaoihn.mp3"),
                                            new CancionInfo("Malbec", 2, 28L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203817/02_malbec_cy9hcf.mp3"),
                                            new CancionInfo("Rápido", 3, 28L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203822/03_rapido_wixsjz.mp3"),
                                            new CancionInfo("Cascada", 4, 28L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203819/04_cascada_nlotlu.mp3"),
                                            new CancionInfo("Pintao", 5, 28L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203827/05_pintao_yd37a2.mp3")
                                    ),
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996291/desdeelfindelmundo_yyaz3y.jpg"
                            )
                    ),
                    Arrays.asList(
                            new SingleInfo("Antes de Perderte", 28L, 1.49,
                                    "Single emotivo de Duki sobre relaciones y despedidas.",
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996291/antesdeperderte_vxrxz1.jpg",
                                    "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203919/antesdeperderte_ukwtus.mp3")
                    )
            ),

            new ArtistaContenido(
                    6L,
                    "sanguijuelasdelguadiana",
                    Arrays.asList(
                            new AlbumInfo("Revolá", 1L, 2023, 16.99,
                                    "Álbum debut de la banda extremeña con rock alternativo y letras crudas sobre la España profunda.",
                                    Arrays.asList(
                                            new CancionInfo("Intro", 1, 1L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204158/01_intro_k9maj8.mp3"),
                                            new CancionInfo("1000 Amapolas", 2, 1L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204162/02_1000amapolas_ccwn0l.mp3"),
                                            new CancionInfo("Jaribe", 3, 1L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204172/03_jaribe_rpeu0y.mp3"),
                                            new CancionInfo("Septiembre", 4, 1L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204175/04_septiembre_k5dei4.mp3"),
                                            new CancionInfo("Llevadme a Mi Extremadura", 5, 1L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204165/05_llevadmeamiextremadura_ic88ii.mp3"),
                                            new CancionInfo("Revolá", 6, 1L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204169/06_revola_kvvjmb.mp3"),
                                            new CancionInfo("Intacto", 7, 1L, 1.29, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204179/07_intacto_lrtd1z.mp3")
                                    ),
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204128/cover_rzbuen.jpg"
                            )
                    )
            ),

            new ArtistaContenido(
                    3L,
                    "avicii",
                    Arrays.asList(
                            new AlbumInfo("True", 8L, 2013, 24.99,
                                    "Álbum revolucionario que mezcló EDM con folk y country, con 'Wake Me Up' como hit mundial.",
                                    Arrays.asList(
                                            new CancionInfo("Hey Brother", 1, 8L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204337/01_heybrother_il6jdd.mp3"),
                                            new CancionInfo("You Make Me", 2, 8L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204335/02_youmakeme_stdl7n.mp3"),
                                            new CancionInfo("Addicted to You", 3, 8L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204331/03_addictedtoyou_tejm7a.mp3")
                                    ),
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204341/cover_oya6ma.jpg"
                            )
                    ),
                    Arrays.asList(
                            new SingleInfo("Levels", 8L, 1.49,
                                    "El himno que definió la era dorada del EDM.",
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204344/cover_pglkku.jpg",
                                    "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204428/levels_ds2jmy.mp3"),
                            new SingleInfo("The Nights", 8L, 1.49,
                                    "Canción inspiracional que se convirtió en un himno generacional.",
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204349/cover_fo5cqa.jpg",
                                    "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204604/thenights_tglv4l.mp3"),
                            new SingleInfo("Wake Me Up", 8L, 1.49,
                                    "Fusión innovadora de EDM y folk que rompió barreras musicales.",
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204354/cover_yvezjj.jpg",
                                    "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204609/wakemeup_cd467f.mp3")
                    )
            ),

            new ArtistaContenido(
                    5L,
                    "rosalia",
                    Arrays.asList(
                            new AlbumInfo("El Mal Querer", 2L, 2018, 21.99,
                                    "Álbum conceptual basado en una novela medieval, fusionando flamenco con pop urbano.",
                                    Arrays.asList(
                                            new CancionInfo("Malamente", 1, 2L, 1.99, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205154/01_malamente_xe118k.mp3"),
                                            new CancionInfo("Pienso en Tu Mirá", 2, 2L, 1.99, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205144/02_piensoentumira_k6nb3r.mp3"),
                                            new CancionInfo("Bagdad", 3, 2L, 1.99, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205149/03_bagdad_dmoqcm.mp3"),
                                            new CancionInfo("Nana", 4, 2L, 1.99, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205157/04_nana_i3usbq.mp3")
                                    ),
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764205173/cover_wo3lhq.jpg"
                            )
                    ),
                    Arrays.asList(
                            new SingleInfo("Despechá", 2L, 1.99,
                                    "Single veraniego con ritmos de merengue dominicano.",
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764205168/cover_wpzzdh.jpg",
                                    "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205163/despecha_sdc2mw.mp3")
                    )
            ),

            new ArtistaContenido(
                    4L,
                    "daddyyankee",
                    Arrays.asList(
                            new AlbumInfo("Prestige", 19L, 2012, 19.99,
                                    "Álbum que consolidó a Daddy Yankee como el Rey del Reggaetón mundial.",
                                    Arrays.asList(
                                            new CancionInfo("Perros Salvajes", 1, 19L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204656/01_perrossalvajes_oegwli.mp3"),
                                            new CancionInfo("Limbo", 2, 19L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204659/02_limbo_slntng.mp3"),
                                            new CancionInfo("Lovumba", 3, 19L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204662/03_lovumba_cemwnc.mp3"),
                                            new CancionInfo("Pasarela", 4, 19L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204652/04_pasarela_tedvft.mp3"),
                                            new CancionInfo("El Amante", 5, 19L, 1.49, "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204666/05_elamante_kpz3wq.mp3")
                                    ),
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204670/cover_vgefhq.jpg"
                            )
                    ),
                    Arrays.asList(
                            new SingleInfo("Gasolina", 19L, 0.00,
                                    "El himno que llevó el reggaetón al mundo entero. Legendario.",
                                    "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204707/cover_ty52br.jpg",
                                    "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204837/gasolina_queplv.mp3")
                    )
            )
    );

    /**
     * Representa un artista con su contenido musical asociado.
     */
    @Data
    @AllArgsConstructor
    public static class ArtistaContenido {
        private Long idArtista;
        private String carpeta;
        private List<AlbumInfo> albumes;
        private List<SingleInfo> singles;

        /**
         * Constructor para artistas sin singles.
         *
         * @param idArtista identificador del artista
         * @param carpeta nombre de carpeta para almacenamiento de archivos
         * @param albumes lista de álbumes del artista
         */
        public ArtistaContenido(Long idArtista, String carpeta, List<AlbumInfo> albumes) {
            this.idArtista = idArtista;
            this.carpeta = carpeta;
            this.albumes = albumes;
            this.singles = Arrays.asList();
        }
    }

    /**
     * Representa la información de un álbum musical.
     */
    @Data
    @AllArgsConstructor
    public static class AlbumInfo {
        private String titulo;
        private Long idGenero;
        private Integer anio;
        private Double precio;
        private String descripcion;
        private List<CancionInfo> canciones;
        private String urlPortadaCompartida;
    }

    /**
     * Representa la información de una canción dentro de un álbum.
     */
    @Data
    @AllArgsConstructor
    public static class CancionInfo {
        private String titulo;
        private Integer numeroPista;
        private Long idGenero;
        private Double precio;
        private String urlAudioCompartida;
    }

    /**
     * Representa la información de un single independiente
     */
    @Data
    @AllArgsConstructor
    public static class SingleInfo {
        private String titulo;
        private Long idGenero;
        private Double precio;
        private String descripcion;
        private String urlPortadaCompartida;
        private String urlAudioCompartida;
    }
}