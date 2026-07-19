package hls_study.api.utils;

import hls_study.api.exceptions.InvalidVideoException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class VideoUtils {

	private static final int MAX_FILENAME_LENGTH = 255;

	private static final List<VideoFormat> ALLOWED = List.of( //
			new VideoFormat("video/mp4", ".mp4"), //
			new VideoFormat("video/avi", ".avi"), //
			new VideoFormat("video/mov", ".mov") //
	);

	private VideoUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String listValidVideoTypes() {
		return ALLOWED.stream().map(VideoFormat::contentType).reduce((a, b) -> a + ", " + b).orElse("");
	}

	public static String listValidExtensions() {
		return ALLOWED.stream().map(VideoFormat::extension).reduce((a, b) -> a + ", " + b).orElse("");
	}

	public static boolean isValidVideoType(String contentType) {
		return (contentType != null && !contentType.isEmpty()) && ALLOWED.stream().anyMatch(
				f -> f.contentType().equals(contentType));
	}

	public static boolean isValidExtension(String filename) {
		return filename != null && !filename.isEmpty() && ALLOWED.stream().anyMatch(
				f -> f.extension().equals(getFileExtension(filename)));
	}

	public static String generateVideoId() {
		return UUID.randomUUID().toString();
	}

	public static String getFileExtension(String filename) {
		if (filename == null || filename.isEmpty()) {
			return "";
		}

		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
			return "";
		}

		return "." + filename.substring(lastDotIndex + 1);
	}

	public static void validateVideoFile(MultipartFile videoFile) {
		if (videoFile.isEmpty())
			throw new InvalidVideoException("Arquivo de vídeo vazio. Por favor, envie um arquivo válido.");

		if (!VideoUtils.isValidVideoType(videoFile.getContentType()))
			throw new InvalidVideoException(
					"Formato do arquivo inválido. Formatos válidos: " + VideoUtils.listValidVideoTypes());

		if (!isValidFilename(videoFile.getOriginalFilename()))
			throw new InvalidVideoException(
					"Nome de arquivo inválido. Use até " + MAX_FILENAME_LENGTH + " caracteres, sem barras.");

		if (!VideoUtils.isValidExtension(videoFile.getOriginalFilename()))
			throw new InvalidVideoException(
					"Extensão do arquivo inválida. Extensões válidas: " + VideoUtils.listValidExtensions());

		if (!hasValidVideoSignature(videoFile))
			throw new InvalidVideoException("Conteúdo do arquivo não corresponde a um vídeo válido do tipo declarado.");
	}

	private static boolean isValidFilename(String filename) {
		return filename != null && !filename.isBlank() && filename.length() <= MAX_FILENAME_LENGTH && filename.chars()
																											  .noneMatch(
																													  Character::isISOControl) && !filename.contains(
				"/") && !filename.contains("\\");
	}

	private static boolean hasValidVideoSignature(MultipartFile videoFile) {
		String extension = getFileExtension(Objects.requireNonNull(videoFile.getOriginalFilename()));

		byte[] header = new byte[12];
		try (InputStream in = videoFile.getInputStream()) {
			if (in.readNBytes(header, 0, header.length) < header.length)
				return false;
		} catch (IOException e) {
			return false;
		}

		return switch (extension) {
			case ".mp4", ".mov" -> matchesAscii(header, 4, "ftyp");
			case ".avi" -> matchesAscii(header, 0, "RIFF") && matchesAscii(header, 8, "AVI ");
			default -> false;
		};
	}

	private static boolean matchesAscii(byte[] data, int offset, String expected) {
		byte[] expectedBytes = expected.getBytes(StandardCharsets.US_ASCII);
		for (int i = 0; i < expectedBytes.length; i++) {
			if (data[offset + i] != expectedBytes[i])
				return false;
		}
		return true;
	}

	record VideoFormat(String contentType, String extension) {

	}

}