package hls_study.api.exceptions;

import hls_study.api.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidVideoException.class)
	public ResponseEntity<ErrorResponse> handleException(InvalidVideoException e) {
		log.warn("Requisição inválida: {}", e.getMessage());
		return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleException(MaxUploadSizeExceededException e) {
		log.warn("Upload excedeu o tamanho máximo permitido: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
							 .body(new ErrorResponse("Arquivo excede o tamanho máximo permitido para upload."));
	}

	@ExceptionHandler(VideoUploadException.class)
	public ResponseEntity<ErrorResponse> handleException(VideoUploadException e) {
		log.error("Falha ao processar upload de vídeo", e);
		return ResponseEntity.status(500).body(new ErrorResponse(e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("Erro inesperado", e);
		return ResponseEntity.status(500)
							 .body(new ErrorResponse("An unexpected error occurred. Please try again later."));
	}

}