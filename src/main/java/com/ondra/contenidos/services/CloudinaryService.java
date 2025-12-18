package com.ondra.contenidos.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.ondra.contenidos.exceptions.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para gestión de archivos multimedia en Cloudinary.
 *
 * <p>Proporciona funcionalidades de subida, validación y eliminación de archivos
 * de audio y portadas para canciones y álbumes.</p>
 *
 * <p>Configuración de audio: formatos MP3, WAV, FLAC, M4A, OGG con tamaño máximo de 50MB.
 * Configuración de imágenes: formatos JPG, PNG, WEBP con tamaño máximo de 5MB y
 * transformación automática a 1000x1000px.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    @Value("${cloudinary.audio.max-size:52428800}")
    private long maxAudioSize;

    @Value("${cloudinary.image.max-size:5242880}")
    private long maxImageSize;

    /**
     * Lista de URLs protegidas que nunca se eliminarán de Cloudinary.
     * Usadas principalmente para datos de seeding y desarrollo.
     */
    private static final List<String> URLS_PROTEGIDAS = List.of(
            // === URLs DE AUDIO (CANCIONES) ===
            // Aitana - Alpha
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203010/01_losangeles_s2bjre.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203017/02_lasbabys_edvpit.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203019/03_darari_wpzcyl.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203021/04_aqyne_rjazqj.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203023/05_miamor_wwyctx.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203027/06_formentera_tzouxa.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203025/07_enelcoche_hkuwt0.mp3",
            // Aitana - Cuarto Azul
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203421/01_6defebrero_ibw712.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203423/02_segundointento_ejd5yr.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203425/03_cuandohablesconel_wpxmh6.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203428/04_superestrella_ftpprh.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203429/05_conexionpsiquica_sfrxya.mp3",
            // Duki - Ameri
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203641/01_nuevaera_s0v6px.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203642/02_brindis_kfa0tc.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203645/03_hardaway_r6bfrp.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203647/04_ameri_kmxz9b.mp3",
            // Duki - Desde el Fin del Mundo
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203814/01_sudorytrabajo_uaoihn.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203817/02_malbec_cy9hcf.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203822/03_rapido_wixsjz.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203819/04_cascada_nlotlu.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203827/05_pintao_yd37a2.mp3",
            // Duki - Singles
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764203919/antesdeperderte_ukwtus.mp3",
            // Sanguijuelas del Guadiana - Revolá
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204158/01_intro_k9maj8.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204162/02_1000amapolas_ccwn0l.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204172/03_jaribe_rpeu0y.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204175/04_septiembre_k5dei4.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204165/05_llevadmeamiextremadura_ic88ii.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204169/06_revola_kvvjmb.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204179/07_intacto_lrtd1z.mp3",
            // Avicii - True
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204337/01_heybrother_il6jdd.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204335/02_youmakeme_stdl7n.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204331/03_addictedtoyou_tejm7a.mp3",
            // Avicii - Singles
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204428/levels_ds2jmy.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204604/thenights_tglv4l.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204609/wakemeup_cd467f.mp3",
            // Rosalía - El Mal Querer
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205154/01_malamente_xe118k.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205144/02_piensoentumira_k6nb3r.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205149/03_bagdad_dmoqcm.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205157/04_nana_i3usbq.mp3",
            // Rosalía - Singles
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764205163/despecha_sdc2mw.mp3",
            // Daddy Yankee - Prestige
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204656/01_perrossalvajes_oegwli.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204659/02_limbo_slntng.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204662/03_lovumba_cemwnc.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204652/04_pasarela_tedvft.mp3",
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204666/05_elamante_kpz3wq.mp3",
            // Daddy Yankee - Singles
            "https://res.cloudinary.com/dh6w4hrx7/video/upload/v1764204837/gasolina_queplv.mp3",

            // === URLs DE PORTADAS (IMÁGENES) ===
            // Aitana - Álbumes
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996438/alpha_g3u9em.jpg",
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996437/cuartoazul_pulgxz.jpg",
            // Duki - Álbumes
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996291/ameri_kgqmuo.jpg",
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996291/desdeelfindelmundo_yyaz3y.jpg",
            // Duki - Singles
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1765996291/antesdeperderte_vxrxz1.jpg",
            // Sanguijuelas del Guadiana - Álbumes
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204128/cover_rzbuen.jpg",
            // Avicii - Álbumes
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204341/cover_oya6ma.jpg",
            // Avicii - Singles
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204344/cover_pglkku.jpg",
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204349/cover_fo5cqa.jpg",
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204354/cover_yvezjj.jpg",
            // Rosalía - Álbumes
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764205173/cover_wo3lhq.jpg",
            // Rosalía - Singles
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764205168/cover_wpzzdh.jpg",
            // Daddy Yankee - Álbumes
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204670/cover_vgefhq.jpg",
            // Daddy Yankee - Singles
            "https://res.cloudinary.com/dh6w4hrx7/image/upload/v1764204707/cover_ty52br.jpg"
    );

    /**
     * Resultado de subida de audio con metadata extraída.
     */
    @Data
    @AllArgsConstructor
    public static class AudioUploadResult {
        private String url;
        private Integer duracion;
        private String formato;
    }

    /**
     * Sube un archivo de audio a Cloudinary con validación y extracción de metadata.
     *
     * <p>Valida formato y tamaño, extrae la duración del audio y lo convierte automáticamente
     * a formato MP3 durante la subida.</p>
     *
     * @param file archivo de audio a subir
     * @param carpeta subcarpeta de destino dentro del folder principal
     * @return resultado con URL, duración en segundos y formato original
     * @throws NoFileProvidedException si no se proporciona archivo
     * @throws InvalidAudioFormatException si el formato no es válido
     * @throws AudioSizeExceededException si excede el tamaño máximo
     * @throws AudioUploadFailedException si falla la subida
     */
    public AudioUploadResult subirAudio(MultipartFile file, String carpeta) {
        log.debug("🎵 Iniciando subida de audio a carpeta: {}", carpeta);

        if (file == null || file.isEmpty()) {
            log.warn("⚠️ Intento de subir audio sin proporcionar archivo");
            throw new NoFileProvidedException("No se ha proporcionado ningún archivo");
        }

        if (!esAudioValido(file)) {
            log.warn("⚠️ Intento de subir archivo con formato de audio inválido: {}", file.getContentType());
            throw new InvalidAudioFormatException(
                    "El archivo debe ser un audio válido (MP3, WAV, FLAC, M4A, OGG)"
            );
        }

        if (!esTamanoAudioValido(file)) {
            log.warn("⚠️ Intento de subir audio que excede el tamaño máximo: {} bytes", file.getSize());
            throw new AudioSizeExceededException("El archivo de audio no puede superar los 50MB");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("❌ Error al leer el archivo: {}", e.getMessage(), e);
            throw new AudioUploadFailedException("Error al leer el archivo de audio", e);
        }

        Integer duracion = calcularDuracionAudio(fileBytes, file.getOriginalFilename());
        String formato = extraerFormato(file);

        try {
            String publicId = generarPublicId();
            String folderPath = folder + "/" + carpeta;

            log.debug("📤 Subiendo audio con public_id: {} a carpeta: {} - Duración: {}s - Formato: {}",
                    publicId, folderPath, duracion, formato);

            Map uploadResult = cloudinary.uploader().upload(fileBytes,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folderPath,
                            "resource_type", "video",
                            "overwrite", true,
                            "format", "mp3"
                    ));

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("✅ Audio subido a Cloudinary: {} - Duración: {}s", secureUrl, duracion);

            return new AudioUploadResult(secureUrl, duracion, formato);

        } catch (IOException e) {
            log.error("❌ Error al subir audio a Cloudinary: {}", e.getMessage(), e);
            throw new AudioUploadFailedException("Error al subir el audio a Cloudinary", e);
        }
    }

    /**
     * Sube una imagen de portada a Cloudinary con transformación automática.
     *
     * <p>Valida formato y tamaño, y aplica transformación automática a 1000x1000px
     * con recorte tipo fill y calidad automática.</p>
     *
     * @param file archivo de imagen a subir
     * @param carpeta subcarpeta de destino dentro del folder principal
     * @return URL pública de la imagen subida
     * @throws NoFileProvidedException si no se proporciona archivo
     * @throws InvalidImageFormatException si el formato no es válido
     * @throws ImageSizeExceededException si excede el tamaño máximo
     * @throws ImageUploadFailedException si falla la subida
     */
    public String subirPortada(MultipartFile file, String carpeta) {
        log.debug("🖼️ Iniciando subida de portada a carpeta: {}", carpeta);

        if (file == null || file.isEmpty()) {
            log.warn("⚠️ Intento de subir portada sin proporcionar archivo");
            throw new NoFileProvidedException("No se ha proporcionado ningún archivo");
        }

        if (!esImagenValida(file)) {
            log.warn("⚠️ Intento de subir archivo con formato inválido: {}", file.getContentType());
            throw new InvalidImageFormatException(
                    "El archivo debe ser una imagen válida (JPG, PNG, WEBP)"
            );
        }

        if (!esTamanoImagenValido(file)) {
            log.warn("⚠️ Intento de subir imagen que excede el tamaño máximo: {} bytes", file.getSize());
            throw new ImageSizeExceededException("La imagen no puede superar los 5MB");
        }

        try {
            String publicId = generarPublicId();
            String folderPath = folder + "/" + carpeta;

            log.debug("📤 Subiendo portada con public_id: {} a carpeta: {}", publicId, folderPath);

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folderPath,
                            "resource_type", "image",
                            "overwrite", true,
                            "transformation", new com.cloudinary.Transformation()
                                    .width(1000).height(1000)
                                    .crop("fill")
                                    .quality("auto")
                    ));

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("✅ Portada subida a Cloudinary: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("❌ Error al subir portada a Cloudinary: {}", e.getMessage(), e);
            throw new ImageUploadFailedException("Error al subir la portada a Cloudinary", e);
        }
    }

    /**
     * Elimina un archivo multimedia de Cloudinary mediante su URL.
     *
     * <p>Detecta automáticamente el tipo de recurso (audio como video o imagen)
     * basándose en la ruta del archivo.</p>
     *
     * <p>Las URLs protegidas (definidas en URLS_PROTEGIDAS) nunca se eliminarán,
     * útil para preservar archivos de seeding y desarrollo.</p>
     *
     * @param fileUrl URL completa del archivo a eliminar
     * @throws FileDeletionFailedException si falla la eliminación
     */
    public void eliminarArchivo(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("⚠️ Se intentó eliminar un archivo con URL nula o vacía");
            return;
        }

        // Verificar si la URL está protegida
        if (URLS_PROTEGIDAS.contains(fileUrl)) {
            log.info("🔒 URL protegida del seeder, no se eliminará: {}", fileUrl);
            return;
        }

        String publicId = extraerPublicId(fileUrl);
        if (publicId == null) {
            log.warn("⚠️ No se pudo extraer el public_id de la URL: {}", fileUrl);
            return;
        }

        try {
            log.debug("🗑️ Eliminando archivo de Cloudinary con public_id: {}", publicId);

            String resourceType = publicId.contains("/audio/") ? "video" : "image";

            Map result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));
            String resultStatus = (String) result.get("result");

            if ("ok".equals(resultStatus)) {
                log.info("✅ Archivo eliminado de Cloudinary: {}", publicId);
            } else {
                log.warn("⚠️ Resultado inesperado al eliminar archivo: {} - Status: {}",
                        publicId, resultStatus);
            }
        } catch (IOException e) {
            log.error("❌ Error al eliminar archivo de Cloudinary: {}", e.getMessage(), e);
            throw new FileDeletionFailedException("Error al eliminar el archivo de Cloudinary", e);
        }
    }

    /**
     * Elimina todos los archivos de una carpeta específica en Cloudinary.
     *
     * <p>Utilizado principalmente para limpieza de datos de seeding.
     * Esta operación es irreversible.</p>
     *
     * @param carpeta subcarpeta dentro del folder principal a limpiar
     * @param resourceType tipo de recurso: "image" o "video"
     * @return número de archivos eliminados
     */
    public int limpiarCarpeta(String carpeta, String resourceType) {
        String folderPath = folder + "/" + carpeta;
        int archivosEliminados = 0;

        try {
            log.info("🧹 Iniciando limpieza de la carpeta: {} (tipo: {})", folderPath, resourceType);

            ApiResponse result = cloudinary.api().resources(
                    ObjectUtils.asMap(
                            "type", "upload",
                            "prefix", folderPath,
                            "resource_type", resourceType,
                            "max_results", 500
                    ));

            List<Map> resources = (List<Map>) result.get("resources");

            if (resources == null || resources.isEmpty()) {
                log.info("ℹ️ No se encontraron archivos en la carpeta: {}", folderPath);
                return 0;
            }

            log.info("📂 Se encontraron {} archivos para eliminar", resources.size());

            for (Map resource : resources) {
                String publicId = (String) resource.get("public_id");
                try {
                    cloudinary.uploader().destroy(publicId,
                            ObjectUtils.asMap("resource_type", resourceType));
                    archivosEliminados++;
                    log.debug("🗑️ Archivo eliminado: {}", publicId);
                } catch (Exception e) {
                    log.warn("⚠️ No se pudo eliminar el archivo: {} - Error: {}",
                            publicId, e.getMessage());
                }
            }

            log.info("✅ Limpieza completada: {} archivos eliminados de {}",
                    archivosEliminados, folderPath);

        } catch (Exception e) {
            log.error("❌ Error durante la limpieza de la carpeta {}: {}",
                    folderPath, e.getMessage(), e);
        }

        return archivosEliminados;
    }

    /**
     * Valida que el archivo sea de audio con formato permitido.
     *
     * <p>Formatos permitidos: MP3, WAV, FLAC, M4A, OGG.</p>
     *
     * @param file archivo a validar
     * @return true si es un audio válido
     */
    public boolean esAudioValido(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("audio/mpeg") ||
                contentType.equals("audio/mp3") ||
                contentType.equals("audio/wav") ||
                contentType.equals("audio/x-wav") ||
                contentType.equals("audio/flac") ||
                contentType.equals("audio/x-flac") ||
                contentType.equals("audio/m4a") ||
                contentType.equals("audio/x-m4a") ||
                contentType.equals("audio/ogg");
    }

    /**
     * Valida que el tamaño del archivo de audio no exceda el límite configurado.
     *
     * @param file archivo a validar
     * @return true si el tamaño es válido
     */
    public boolean esTamanoAudioValido(MultipartFile file) {
        return file != null && file.getSize() <= maxAudioSize;
    }

    /**
     * Valida que el archivo sea una imagen con formato permitido.
     *
     * <p>Formatos permitidos: JPG, PNG, WEBP.</p>
     *
     * @param file archivo a validar
     * @return true si es una imagen válida
     */
    public boolean esImagenValida(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/webp");
    }

    /**
     * Valida que el tamaño del archivo de imagen no exceda el límite configurado.
     *
     * @param file archivo a validar
     * @return true si el tamaño es válido
     */
    public boolean esTamanoImagenValido(MultipartFile file) {
        return file != null && file.getSize() <= maxImageSize;
    }

    /**
     * Extrae el formato del archivo de audio basándose en su content type.
     *
     * @param file archivo multipart
     * @return formato del audio o "unknown" si no se puede determinar
     */
    private String extraerFormato(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return "unknown";
        }

        if (contentType.contains("mpeg") || contentType.contains("mp3")) return "mp3";
        if (contentType.contains("wav")) return "wav";
        if (contentType.contains("flac")) return "flac";
        if (contentType.contains("m4a")) return "m4a";
        if (contentType.contains("ogg")) return "ogg";

        return "unknown";
    }

    /**
     * Extrae el public_id de una URL de Cloudinary.
     *
     * <p>Ejemplo: https://res.cloudinary.com/demo/image/upload/v1234/media/canciones/audio/abc123.mp3
     * retorna "media/canciones/audio/abc123".</p>
     *
     * @param fileUrl URL completa del archivo
     * @return public_id extraído o null si no se puede extraer
     */
    private String extraerPublicId(String fileUrl) {
        try {
            int uploadIndex = fileUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                log.warn("⚠️ URL no contiene '/upload/': {}", fileUrl);
                return null;
            }

            String afterUpload = fileUrl.substring(uploadIndex + 8);

            int versionEnd = afterUpload.indexOf("/");
            if (versionEnd == -1) {
                log.warn("⚠️ URL no tiene formato de versión correcto: {}", fileUrl);
                return null;
            }

            String pathWithExtension = afterUpload.substring(versionEnd + 1);

            int lastDot = pathWithExtension.lastIndexOf(".");
            String publicId = lastDot != -1
                    ? pathWithExtension.substring(0, lastDot)
                    : pathWithExtension;

            log.debug("🔍 Public ID extraído: {}", publicId);
            return publicId;

        } catch (Exception e) {
            log.error("❌ Error al extraer public_id de la URL: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * Genera un identificador único para el archivo usando UUID.
     *
     * @return identificador único en formato UUID
     */
    private String generarPublicId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Calcula la duración de un archivo de audio en segundos usando JAudioTagger.
     *
     * <p>Crea un archivo temporal para procesar el audio y lo elimina tras la lectura.</p>
     *
     * @param fileBytes bytes del archivo de audio
     * @param filename nombre original del archivo
     * @return duración en segundos, o null si no se puede calcular
     */
    private Integer calcularDuracionAudio(byte[] fileBytes, String filename) {
        Path tempFile = null;
        try {
            String extension = getExtensionFromFilename(filename);
            tempFile = Files.createTempFile("audio_", extension);

            Files.write(tempFile, fileBytes);

            AudioFile audioFile = AudioFileIO.read(tempFile.toFile());
            AudioHeader audioHeader = audioFile.getAudioHeader();

            int duracion = audioHeader.getTrackLength();

            log.debug("⏱️ Duración del audio calculada: {} segundos", duracion);
            return duracion;

        } catch (Exception e) {
            log.warn("⚠️ No se pudo calcular la duración del audio: {}", e.getMessage());
            return null;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("⚠️ No se pudo eliminar el archivo temporal: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Extrae la extensión del nombre del archivo.
     *
     * @param filename nombre del archivo
     * @return extensión con punto o ".tmp" si no se encuentra
     */
    private String getExtensionFromFilename(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".tmp";
    }
}