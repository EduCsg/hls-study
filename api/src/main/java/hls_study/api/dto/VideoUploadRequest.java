package hls_study.api.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record VideoUploadRequest(@NotNull(message = "Arquivo é obrigatório.") MultipartFile file) {

}