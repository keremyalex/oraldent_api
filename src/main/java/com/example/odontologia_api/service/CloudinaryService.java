package com.example.odontologia_api.service;

import com.cloudinary.Cloudinary;
import com.example.odontologia_api.config.CloudinaryProperties;
import com.example.odontologia_api.dto.CloudinaryUploadResult;
import com.example.odontologia_api.exception.CloudinaryStorageException;
import com.example.odontologia_api.exception.ReglaNegocioException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    public CloudinaryService(@Nullable Cloudinary cloudinary, CloudinaryProperties properties) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    public CloudinaryUploadResult replaceImage(MultipartFile archivo, String folder, String publicIdAnterior) {
        validarArchivo(archivo);

        try {
            Map<String, Object> opciones = new HashMap<>();
            opciones.put("folder", folder);
            opciones.put("resource_type", "image");
            opciones.put("overwrite", false);
            opciones.put("unique_filename", true);

            @SuppressWarnings("unchecked")
            Map<String, Object> resultado = cloudinary.uploader().upload(archivo.getBytes(), opciones);

            String secureUrl = (String) resultado.get("secure_url");
            String publicId = (String) resultado.get("public_id");

            if (secureUrl == null || publicId == null) {
                throw new CloudinaryStorageException("Cloudinary no devolvió la información esperada del archivo.");
            }

            if (publicIdAnterior != null && !publicIdAnterior.isBlank() && !publicIdAnterior.equals(publicId)) {
                deleteImage(publicIdAnterior);
            }

            return new CloudinaryUploadResult(secureUrl, publicId);
        } catch (IOException ex) {
            throw new CloudinaryStorageException("No se pudo leer el archivo enviado.", ex);
        } catch (CloudinaryStorageException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new CloudinaryStorageException("No se pudo subir la imagen a Cloudinary.", ex);
        }
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank() || cloudinary == null) {
            return;
        }

        try {
            Map<String, Object> opciones = new HashMap<>();
            opciones.put("resource_type", "image");
            cloudinary.uploader().destroy(publicId, opciones);
        } catch (IOException ex) {
            throw new CloudinaryStorageException("No se pudo eliminar la imagen anterior en Cloudinary.", ex);
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (!properties.isEnabled() || cloudinary == null) {
            throw new ReglaNegocioException("La integración con Cloudinary está deshabilitada.");
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("Debe enviar un archivo de imagen.");
        }
        if (archivo.getSize() > properties.getMaxFileSizeBytes()) {
            throw new ReglaNegocioException("La imagen excede el tamaño máximo permitido.");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ReglaNegocioException("Solo se permiten archivos de imagen.");
        }
    }
}
