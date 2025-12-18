package com.ondra.contenidos.models.dao;

import com.ondra.contenidos.models.enums.GeneroMusical;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una canción en el catálogo de la plataforma.
 *
 * <p>
 * Una canción pertenece a un artista y puede estar asociada a múltiples álbumes
 * mediante la tabla intermedia AlbumCancion. Incluye información sobre el
 * archivo
 * de audio, portada, duración y estadísticas de reproducción.
 * </p>
 */
@Entity
@Table(name = "canciones", indexes = {
        @Index(name = "idx_cancion_artista", columnList = "id_artista"),
        @Index(name = "idx_cancion_genero", columnList = "genero"),
        @Index(name = "idx_cancion_precio", columnList = "precio_cancion"),
        @Index(name = "idx_cancion_fecha", columnList = "fecha_publicacion"),
        @Index(name = "idx_cancion_reproducciones", columnList = "reproducciones")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cancion {

    /**
     * Identificador único de la canción.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancion")
    private Long idCancion;

    /**
     * Título de la canción.
     */
    @Column(name = "titulo_cancion", nullable = false, length = 200)
    private String tituloCancion;

    /**
     * Identificador del artista propietario de la canción.
     */
    @Column(name = "id_artista", nullable = false)
    private Long idArtista;

    /**
     * Género musical de la canción.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 50)
    private GeneroMusical genero;

    /**
     * Precio de la canción en euros.
     * Un valor de 0.00 indica contenido gratuito.
     */
    @Column(name = "precio_cancion", nullable = false, columnDefinition = "NUMERIC(10,2)")
    private Double precioCancion;

    /**
     * Duración de la canción en segundos.
     */
    @Column(name = "duracion_segundos", nullable = false)
    private Integer duracionSegundos;

    /**
     * URL de la portada almacenada en Cloudinary.
     */
    @Column(name = "url_portada", columnDefinition = "TEXT")
    private String urlPortada;

    /**
     * URL del archivo de audio almacenado en Cloudinary.
     */
    @Column(name = "url_audio", columnDefinition = "TEXT")
    private String urlAudio;

    /**
     * Contador de reproducciones acumuladas.
     */
    @Column(name = "reproducciones", nullable = false)
    @Builder.Default
    private Long reproducciones = 0L;

    /**
     * Fecha y hora de publicación de la canción.
     */
    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    /**
     * Descripción opcional de la canción.
     */
    @Column(name = "descripcion", length = 1000, columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Relación con álbumes mediante tabla intermedia.
     * Una canción puede estar incluida en múltiples álbumes.
     */
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlbumCancion> albumCanciones = new ArrayList<>();

    /**
     * Items del carrito que referencian esta canción.
     * Al eliminar la canción, se eliminan automáticamente los items del carrito.
     */
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CarritoItem> carritoItems = new ArrayList<>();

    /**
     * Compras que referencian esta canción.
     * Al eliminar la canción, se eliminan automáticamente las compras.
     */
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Compra> compras = new ArrayList<>();

    /**
     * Valoraciones que referencian esta canción.
     * Al eliminar la canción, se eliminan automáticamente las valoraciones.
     */
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Valoracion> valoraciones = new ArrayList<>();

    /**
     * Favoritos que referencian esta canción.
     * Al eliminar la canción, se eliminan automáticamente los favoritos.
     */
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Favorito> favoritos = new ArrayList<>();

    /**
     * Comentarios que referencian esta canción.
     * Al eliminar la canción, se eliminan automáticamente los comentarios.
     */
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comentario> comentarios = new ArrayList<>();

    /**
     * Establece valores por defecto al momento de persistencia.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaPublicacion == null) {
            fechaPublicacion = LocalDateTime.now();
        }
        if (reproducciones == null) {
            reproducciones = 0L;
        }
    }

    /**
     * Incrementa el contador de reproducciones en una unidad.
     */
    public void incrementarReproducciones() {
        this.reproducciones++;
    }

    /**
     * Verifica si la canción es de acceso gratuito.
     *
     * @return true si el precio es 0.00
     */
    public boolean esGratuita() {
        return this.precioCancion != null && this.precioCancion == 0.0;
    }

    /**
     * Verifica si la canción pertenece a un artista específico.
     *
     * @param idArtista identificador del artista
     * @return true si la canción pertenece al artista
     */
    public boolean perteneceArtista(Long idArtista) {
        return this.idArtista != null && this.idArtista.equals(idArtista);
    }
}