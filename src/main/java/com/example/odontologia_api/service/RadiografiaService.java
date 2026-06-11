package com.example.odontologia_api.service;

import com.example.odontologia_api.dto.AnalisisRadiografiaRequest;
import com.example.odontologia_api.dto.AnalisisRadiografiaResponse;
import com.example.odontologia_api.dto.CloudinaryUploadResult;
import com.example.odontologia_api.dto.RadiografiaRequest;
import com.example.odontologia_api.dto.RadiografiaResponse;
import com.example.odontologia_api.entity.AnalisisRadiografia;
import com.example.odontologia_api.entity.FichaClinica;
import com.example.odontologia_api.entity.Radiografia;
import com.example.odontologia_api.entity.Usuario;
import com.example.odontologia_api.enums.EstadoAnalisisRadiografia;
import com.example.odontologia_api.exception.RecursoNoEncontradoException;
import com.example.odontologia_api.exception.ReglaNegocioException;
import com.example.odontologia_api.repository.AnalisisRadiografiaRepository;
import com.example.odontologia_api.repository.RadiografiaRepository;
import com.example.odontologia_api.repository.UsuarioRepository;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RadiografiaService {

    private static final String CLOUDINARY_FOLDER = "oraldent/radiografias";

    private final RadiografiaRepository radiografiaRepository;
    private final AnalisisRadiografiaRepository analisisRadiografiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FichaClinicaService fichaClinicaService;
    private final CloudinaryService cloudinaryService;

    public RadiografiaService(
            RadiografiaRepository radiografiaRepository,
            AnalisisRadiografiaRepository analisisRadiografiaRepository,
            UsuarioRepository usuarioRepository,
            FichaClinicaService fichaClinicaService,
            CloudinaryService cloudinaryService
    ) {
        this.radiografiaRepository = radiografiaRepository;
        this.analisisRadiografiaRepository = analisisRadiografiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.fichaClinicaService = fichaClinicaService;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional(readOnly = true)
    public List<RadiografiaResponse> listarPorFicha(Long fichaId) {
        FichaClinica ficha = fichaClinicaService.buscarActiva(fichaId);
        return radiografiaRepository.findByFichaClinicaAndActivoTrueOrderByFechaEstudioDescFechaCreacionDescIdDesc(ficha)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RadiografiaResponse obtener(Long radiografiaId) {
        return toResponse(buscarActiva(radiografiaId));
    }

    @Transactional
    public RadiografiaResponse crear(Long fichaId, RadiografiaRequest request, MultipartFile archivo) {
        FichaClinica ficha = fichaClinicaService.buscarActiva(fichaId);
        ImageMetadata metadata = leerMetadata(archivo);
        CloudinaryUploadResult uploadResult = cloudinaryService.replaceImage(archivo, CLOUDINARY_FOLDER, null);

        Radiografia radiografia = new Radiografia();
        radiografia.setFichaClinica(ficha);
        aplicarDatos(radiografia, request);
        aplicarMetadataImagen(radiografia, archivo, metadata);
        radiografia.setImagenUrl(uploadResult.url());
        radiografia.setImagenPublicId(uploadResult.publicId());
        radiografia.setActivo(true);

        return toResponse(radiografiaRepository.save(radiografia));
    }

    @Transactional
    public RadiografiaResponse actualizar(Long radiografiaId, RadiografiaRequest request) {
        Radiografia radiografia = buscarActiva(radiografiaId);
        aplicarDatos(radiografia, request);
        return toResponse(radiografiaRepository.save(radiografia));
    }

    @Transactional
    public RadiografiaResponse reemplazarImagen(Long radiografiaId, MultipartFile archivo) {
        Radiografia radiografia = buscarActiva(radiografiaId);
        ImageMetadata metadata = leerMetadata(archivo);
        CloudinaryUploadResult uploadResult = cloudinaryService.replaceImage(
                archivo,
                CLOUDINARY_FOLDER,
                radiografia.getImagenPublicId()
        );
        aplicarMetadataImagen(radiografia, archivo, metadata);
        radiografia.setImagenUrl(uploadResult.url());
        radiografia.setImagenPublicId(uploadResult.publicId());
        return toResponse(radiografiaRepository.save(radiografia));
    }

    @Transactional
    public void desactivar(Long radiografiaId) {
        Radiografia radiografia = buscarActiva(radiografiaId);
        radiografia.setActivo(false);
        radiografiaRepository.save(radiografia);
    }

    @Transactional(readOnly = true)
    public List<AnalisisRadiografiaResponse> listarAnalisis(Long radiografiaId) {
        Radiografia radiografia = buscarActiva(radiografiaId);
        return analisisRadiografiaRepository.findByRadiografiaAndActivoTrueOrderByFechaCreacionDescIdDesc(radiografia)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalisisRadiografiaResponse obtenerAnalisis(Long analisisId) {
        return toResponse(buscarAnalisisActivo(analisisId));
    }

    @Transactional
    public AnalisisRadiografiaResponse crearAnalisis(Long radiografiaId, Long usuarioId, AnalisisRadiografiaRequest request) {
        Radiografia radiografia = buscarActiva(radiografiaId);
        AnalisisRadiografia analisis = new AnalisisRadiografia();
        analisis.setRadiografia(radiografia);
        aplicarDatosAnalisis(analisis, usuarioId, request);
        return toResponse(analisisRadiografiaRepository.save(analisis));
    }

    @Transactional
    public AnalisisRadiografiaResponse actualizarAnalisis(Long analisisId, Long usuarioId, AnalisisRadiografiaRequest request) {
        AnalisisRadiografia analisis = buscarAnalisisActivo(analisisId);
        aplicarDatosAnalisis(analisis, usuarioId, request);
        return toResponse(analisisRadiografiaRepository.save(analisis));
    }

    @Transactional
    public void desactivarAnalisis(Long analisisId) {
        AnalisisRadiografia analisis = buscarAnalisisActivo(analisisId);
        analisis.setActivo(false);
        analisisRadiografiaRepository.save(analisis);
    }

    private Radiografia buscarActiva(Long radiografiaId) {
        return radiografiaRepository.findById(radiografiaId)
                .filter(Radiografia::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Radiografia no encontrada."));
    }

    private AnalisisRadiografia buscarAnalisisActivo(Long analisisId) {
        return analisisRadiografiaRepository.findById(analisisId)
                .filter(AnalisisRadiografia::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Analisis de radiografia no encontrado."));
    }

    private void aplicarDatos(Radiografia radiografia, RadiografiaRequest request) {
        String titulo = normalizar(request.titulo());
        if (titulo == null) {
            throw new ReglaNegocioException("El titulo de la radiografia es obligatorio.");
        }
        radiografia.setTitulo(titulo);
        radiografia.setDescripcion(normalizar(request.descripcion()));
        radiografia.setTipo(normalizar(request.tipo()));
        radiografia.setFechaEstudio(request.fechaEstudio() == null ? LocalDate.now() : request.fechaEstudio());
        radiografia.setNumeroFdi(request.numeroFdi());
        radiografia.setZona(normalizar(request.zona()));
        radiografia.setDiagnosticoRadiografico(normalizar(request.diagnosticoRadiografico()));
        radiografia.setPerdidaOseaObservada(Boolean.TRUE.equals(request.perdidaOseaObservada()));
        radiografia.setTipoPerdidaOsea(request.tipoPerdidaOsea());
        radiografia.setSeveridadPerdidaOsea(request.severidadPerdidaOsea());
        radiografia.setPorcentajePerdidaOseaEstimado(request.porcentajePerdidaOseaEstimado());
        radiografia.setNivelCrestaOseaMm(request.nivelCrestaOseaMm());
        radiografia.setObservacionesPeriodontales(normalizar(request.observacionesPeriodontales()));
    }

    private void aplicarDatosAnalisis(AnalisisRadiografia analisis, Long usuarioId, AnalisisRadiografiaRequest request) {
        analisis.setModelo(normalizar(request.modelo()));
        analisis.setEstado(request.estado() == null ? EstadoAnalisisRadiografia.PENDIENTE : request.estado());
        analisis.setPerdidaOseaDetectada(Boolean.TRUE.equals(request.perdidaOseaDetectada()));
        analisis.setTipoPerdidaOsea(request.tipoPerdidaOsea());
        analisis.setSeveridad(request.severidad());
        analisis.setPorcentajePerdidaOsea(request.porcentajePerdidaOsea());
        analisis.setConfianza(request.confianza());
        analisis.setResultadoJson(normalizar(request.resultadoJson()));
        analisis.setRecomendacion(normalizar(request.recomendacion()));
        analisis.setErrorAnalisis(normalizar(request.errorAnalisis()));
        analisis.setComentarioValidacion(normalizar(request.comentarioValidacion()));
        analisis.setSeveridadFinal(request.severidadFinal());
        analisis.setTipoPerdidaOseaFinal(request.tipoPerdidaOseaFinal());

        boolean validado = Boolean.TRUE.equals(request.validado());
        analisis.setValidado(validado);
        if (validado) {
            analisis.setFechaValidacion(LocalDateTime.now());
            analisis.setValidadoPorUsuario(usuarioId == null ? null : buscarUsuario(usuarioId));
        } else {
            analisis.setFechaValidacion(null);
            analisis.setValidadoPorUsuario(null);
        }
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }

    private void aplicarMetadataImagen(Radiografia radiografia, MultipartFile archivo, ImageMetadata metadata) {
        radiografia.setNombreArchivo(normalizar(archivo.getOriginalFilename()));
        radiografia.setFormato(normalizar(archivo.getContentType()));
        radiografia.setTamanoBytes(archivo.getSize());
        radiografia.setAnchoPx(metadata.width());
        radiografia.setAltoPx(metadata.height());
    }

    private ImageMetadata leerMetadata(MultipartFile archivo) {
        try {
            BufferedImage image = ImageIO.read(archivo.getInputStream());
            if (image == null) {
                return new ImageMetadata(null, null);
            }
            return new ImageMetadata(image.getWidth(), image.getHeight());
        } catch (IOException ex) {
            return new ImageMetadata(null, null);
        }
    }

    private String normalizar(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private RadiografiaResponse toResponse(Radiografia radiografia) {
        return new RadiografiaResponse(
                radiografia.getId(),
                radiografia.getFichaClinica().getId(),
                radiografia.getTitulo(),
                radiografia.getDescripcion(),
                radiografia.getTipo(),
                radiografia.getNumeroFdi(),
                radiografia.getZona(),
                radiografia.getImagenUrl(),
                radiografia.getImagenPublicId(),
                radiografia.getNombreArchivo(),
                radiografia.getFormato(),
                radiografia.getTamanoBytes(),
                radiografia.getAnchoPx(),
                radiografia.getAltoPx(),
                radiografia.getFechaEstudio(),
                radiografia.getDiagnosticoRadiografico(),
                radiografia.getPerdidaOseaObservada(),
                radiografia.getTipoPerdidaOsea(),
                radiografia.getSeveridadPerdidaOsea(),
                radiografia.getPorcentajePerdidaOseaEstimado(),
                radiografia.getNivelCrestaOseaMm(),
                radiografia.getObservacionesPeriodontales(),
                radiografia.getActivo(),
                radiografia.getFechaCreacion(),
                radiografia.getFechaActualizacion()
        );
    }

    private AnalisisRadiografiaResponse toResponse(AnalisisRadiografia analisis) {
        return new AnalisisRadiografiaResponse(
                analisis.getId(),
                analisis.getRadiografia().getId(),
                analisis.getModelo(),
                analisis.getEstado(),
                analisis.getPerdidaOseaDetectada(),
                analisis.getTipoPerdidaOsea(),
                analisis.getSeveridad(),
                analisis.getPorcentajePerdidaOsea(),
                analisis.getConfianza(),
                analisis.getResultadoJson(),
                analisis.getRecomendacion(),
                analisis.getErrorAnalisis(),
                analisis.getValidado(),
                analisis.getValidadoPorUsuario() == null ? null : analisis.getValidadoPorUsuario().getId(),
                analisis.getFechaValidacion(),
                analisis.getComentarioValidacion(),
                analisis.getSeveridadFinal(),
                analisis.getTipoPerdidaOseaFinal(),
                analisis.getActivo(),
                analisis.getFechaCreacion(),
                analisis.getFechaActualizacion()
        );
    }

    private record ImageMetadata(Integer width, Integer height) {}
}
